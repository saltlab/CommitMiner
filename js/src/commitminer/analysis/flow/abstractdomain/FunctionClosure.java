package commitminer.analysis.flow.abstractdomain;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;

import org.deri.iris.api.basics.IPredicate;
import org.deri.iris.storage.IRelation;
import org.mozilla.javascript.ast.AstNode;
import org.mozilla.javascript.ast.FunctionNode;
import org.mozilla.javascript.ast.Name;
import org.mozilla.javascript.ast.ScriptNode;

import commitminer.analysis.flow.factories.StoreFactory;
import commitminer.analysis.flow.trace.Trace;
import commitminer.cfg.CFG;
import commitminer.cfg.CFGNode;

/**
 * The abstract domain for function closures.
 */
public class FunctionClosure extends Closure {

	/** The function. **/
	public CFG cfg;

	/** The closure environment? **/
	public Environment environment;

	/** The CFGs in the script. **/
	public Map<AstNode, CFG> cfgs;

	/**
	 * @param cfg The control flow graph for the function.
	 * @param environment The environment of the parent closure. Does not yet
	 * 					  contain local variables of this function.
	 * @param cfgs All control flow graphs in the program.
	 */
	public FunctionClosure(CFG cfg, Environment environment, Map<AstNode, CFG> cfgs) {
		this.cfg = cfg;
		this.environment = environment;
		this.cfgs = cfgs;
	}

	@Override
	public State run(Map<IPredicate, IRelation> facts, 
			Address selfAddr, Store store,
			Scratchpad scratchpad, Trace trace, Control control,
			Stack<Address> callStack) {

		/* Advance the trace. */
		trace = trace.update(environment, store, selfAddr, 
							 (ScriptNode)cfg.getEntryNode().getStatement());
		
		/* Create the initial state if needed. */
		State newState = null;
		State oldState = (State) cfg.getEntryNode().getBeforeState();
		State primeState = initState(facts, selfAddr, store, scratchpad, trace, control, callStack);
		State exitState = null;
		
		if(oldState == null) {
			/* Create the initial state for the function call by lifting local 
			 * vars and functions into the environment. */
			newState = primeState;
		}
		else {
			/* If newState does not change initState, we do not need to re-analyze the function. */
			newState = oldState.join(primeState);
			
			if(equalState(oldState, newState)) {

				exitState = null;

				for(CFGNode exitNode : cfg.getExitNodes()) {
					/* Merge all exit states, because we can only return one. */
					State s = (State)exitNode.getAfterState();
					if(exitState == null) exitState = s;
					else if(s != null) exitState = exitState.join(s);
				}

				if(exitState != null) {
					/* Finally, merge the store from the exit state with the
					 * store from the entry state. */
					exitState.store = exitState.store.join(store);
					return exitState;
				}
				
			}
			else {
				/* We have a new initial state for the function. */
				cfg.getEntryNode().setBeforeState(newState);
			}
			
		}
		
		System.out.println("Analyzing " + ((AstNode)this.cfg.getEntryNode().getStatement()).toSource());

		/* We'll use this later when executing unanalyzed functions. */
		List<Name> localVarNames = VariableLiftVisitor.getVariableDeclarations((ScriptNode)cfg.getEntryNode().getStatement());
		Set<String> localVars = new HashSet<String>();
		for(Name localVarName : localVarNames) localVars.add(localVarName.toSource());

		/* Perform the initial analysis and get the publicly accessible methods. */
		exitState = Helpers.run(cfg, newState);

		/* Analyze the publicly accessible methods that weren't analyzed in
		 * the main analysis. */
		Helpers.analyzeEnvReachable(facts, exitState, exitState.env.environment, exitState.selfAddr, cfgs, new HashSet<Address>(), localVars);

		return exitState;

	}
	
	/**
	 * Lift local variables and function declarations into the environment and 
	 * create the initial state for the function call.
	 * @return The environment for the closure, including parameters and {@code this}.
	 */
	private State initState(Map<IPredicate, IRelation> facts, 
			Address selfAddr, Store store,
			Scratchpad scratchpad, Trace trace, Control control,
			Stack<Address> callStack) {

		/* Lift local variables and function declarations into the environment. */
		Environment env = this.environment.clone();
		store = Helpers.lift(facts, env, store,
							 (ScriptNode)cfg.getEntryNode().getStatement(),
							 cfgs, trace);

		/* Match parameters with arguments. */
		if(this.cfg.getEntryNode().getStatement() instanceof FunctionNode) {
			FunctionNode function = (FunctionNode)this.cfg.getEntryNode().getStatement();

			/* Create the arguments object. */
			Map<Identifier, Address> ext = new HashMap<Identifier, Address>();
			int i = 0;
			for(BValue argVal : scratchpad.applyArgs()) {

				store = Helpers.addProp(function.getID(), String.valueOf(i), argVal,
								ext, store, trace);
				i++;
			}

			InternalObjectProperties internal = new InternalObjectProperties(
					Address.inject(StoreFactory.Arguments_Addr, Change.u(), Change.u()), JSClass.CObject);
			Obj argObj = new Obj(ext, internal);

			/* Put the argument object on the store. */
			Address argAddr = trace.makeAddr(function.getID(), "");
			store = store.alloc(argAddr, argObj);

			i = 0;
			for(AstNode param : function.getParams()) {
				if(param instanceof Name) {
					Name paramName = (Name) param;
					argAddr = argObj.externalProperties.get(new Identifier(String.valueOf(i), Change.u()));
					if(argAddr == null) {

						/* No argument was given for this parameter. Create a
						 * dummy value. */

						/* Add the argument address to the argument object. */
						BValue argVal = BValue.top(Change.convU(param), Change.u());
						store = Helpers.addProp(function.getID(), String.valueOf(i), argVal,
										  argObj.externalProperties, store, trace);

						/* Add or update the argument object to the store. */
						argAddr = trace.makeAddr(function.getID(), String.valueOf(i));
						store = store.alloc(argAddr, argObj);

					}
					Identifier identity = new Identifier(paramName.toSource(), Change.conv(paramName));
					env = env.strongUpdate(identity, new Addresses(argAddr, Change.u()));
				}
				i++;
			}
		}
		
		/* Add 'this' to environment (points to caller's object or new object). */
		env = env.strongUpdate(new Identifier("this", Change.u()), new Addresses(selfAddr, Change.u()));
		
		/* Create the initial state for the function call. */
		return new State(facts, store, env, scratchpad, trace, control, selfAddr, cfgs, callStack);
		
	}
	
	/**
	 * @param s1
	 * @param s2
	 * @return true if the states are the equivalent with respect to environment and store.
	 */
	private static boolean equalState(State s1, State s2) {

		System.out.println(s1.env);
		System.out.println(s2.env);
		
		Set<Address> visited = new HashSet<Address>();
		
		/* Check the initial environment. */
		if(!s1.env.equals(s2.env))
			return false;
		
		/* Check the reachable values in the store. */
		for(Addresses addrs : s1.env.environment.values()) {
			for(Address addr : addrs.addresses) {

				/* Check the values are the same. */
				if(!equalVal(s1, s2, addr, visited)) return false;

			}
		}
		
		/* Control change AD is not equal if: 
		 * 	there are NO control changes in the old state
		 * 	AND 
		 * there are control changes in the new state. */
		if(s1.control.conditions.isEmpty() && !s2.control.conditions.isEmpty()) return false;
		
		return true;

	}
	
	private static boolean equalVal(State s1, State s2, Address addr, Set<Address> visited) {

		if(visited.contains(addr)) return true;
		
		/* Don't re-visit this address. */
		visited.add(addr);
		
		/* Check that the values are the same. */
		BValue b1 = s1.store.apply(addr);
		BValue b2 = s2.store.apply(addr);
		if(!b1.equals(b2)) 
			return false;
		
		/* Check that the objects are the same. */
		for(Address objAddr : b1.addressAD.addresses) {
			Obj s1Obj = s1.store.getObj(objAddr);
			Obj s2Obj = s2.store.getObj(objAddr);
			if(!s1Obj.equals(s2Obj)) return false;
			
			/* Check that the object properties are the same. */
			for(Address propAddr : s1Obj.externalProperties.values()) {
				if(!equalVal(s1, s2, propAddr, visited)) return false;
			}
		}
		
		return true;

	}
	
}