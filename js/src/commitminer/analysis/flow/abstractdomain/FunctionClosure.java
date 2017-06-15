package commitminer.analysis.flow.abstractdomain;

import java.util.Collection;
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

import commitminer.analysis.flow.ScriptFlowAnalysis;
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

	private boolean hasValueChanges(Collection<Address> bvalAddrs, Store store) {

		/* Look for changes to each value. */
		for(Address bvalAddr : bvalAddrs) {

			BValue value = store.apply(bvalAddr);

			/* Check for changes. */
			switch(value.change.le) {
			case CHANGED:
			case TOP:
				return true;
			default:
				break;
			}

			/* Recursively look at objects. */
			for(Address objAddr : value.addressAD.addresses) {
				Obj obj = store.getObj(objAddr);
				boolean recursiveChanged = hasValueChanges(obj.externalProperties.values(), store);
				if(recursiveChanged)
					return true;
			}

		}

		/* No changes found. */
		return false;

	}

	/**
	 * @return true if the analysis needs to be (re-)run on the function
	 */
	private boolean runAnalysis(Control control, Address argArrayAddr, Store store) {
		
		// TODO: Compare the old initial state to the new initial state. We 
		//		 can implement it two ways: (1) compare entire state, (2) 
		//		 compare potential change propagation. #2 risks alias issues...
		//		 less if we consider parameters and ignore the entire state.

		State initState = (State) cfg.getEntryNode().getBeforeState();

		/* Run the analysis on the function if it has not yet run. */
		if(initState == null) return true;

		// Don't do any new analysis if the runtime has exceeded 30 seconds
//		if(ScriptFlowAnalysis.stopWatch.getTime() > 30000) {
//			return false;
//		}
		
		/* Re-run the analysis if there are control changes from the caller
		 * state, but not in the initial state. */
		if(!control.conditions.isEmpty() && initState.control.conditions.isEmpty()) return true;

		/* Re-run the analysis if there are value changes in the arg list. */
		Obj argObj = store.getObj(argArrayAddr);
		return hasValueChanges(argObj.externalProperties.values(), store);
		
	}

	@Override
	public State run(Map<IPredicate, IRelation> facts, 
			Address selfAddr, Address argArrayAddr, Store store,
			Scratchpad scratchpad, Trace trace, Control control,
			Stack<Address> callStack) {

		/* If this has already been analyzed, we can short-circuit. */
		boolean runAnalysis = this.runAnalysis(control, argArrayAddr, store);
		if(!runAnalysis) {

			State exitState = null;

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

		/* Advance the trace. */
		trace = trace.update(environment, store, selfAddr, argArrayAddr,
							 (ScriptNode)cfg.getEntryNode().getStatement());

		/* We'll use this later when executing unanalyzed functions. */
		List<Name> localVarNames = VariableLiftVisitor.getVariableDeclarations((ScriptNode)cfg.getEntryNode().getStatement());
		Set<String> localVars = new HashSet<String>();
		for(Name localVarName : localVarNames) localVars.add(localVarName.toSource());

		/* Lift local variables and function declarations into the environment. */
		Environment env = initEnvironment(facts, selfAddr, argArrayAddr, store, trace);
				
		/* Create the initial state for the function call. */
		State state = new State(facts, store, env, scratchpad, trace, control, selfAddr, cfgs, callStack);

		/* Perform the initial analysis and get the publicly accessible methods. */
		state = Helpers.run(cfg, state);

		/* Analyze the publicly accessible methods that weren't analyzed in
		 * the main analysis. */
		state.env.toString();
		Helpers.analyzeEnvReachable(facts, state, state.env.environment, state.selfAddr, cfgs, new HashSet<Address>(), localVars);

		return state;

	}
	
	/**
	 * @return The environment for the closure, including parameters and {@code this}.
	 */
	private Environment initEnvironment(Map<IPredicate, IRelation> facts, 
			Address selfAddr, Address argArrayAddr, Store store,
			Trace trace) {

		/* Lift local variables and function declarations into the environment. */
		Environment env = this.environment.clone();
		store = Helpers.lift(facts, env, store,
							 (ScriptNode)cfg.getEntryNode().getStatement(),
							 cfgs, trace);

		/* Match parameters with arguments. */
		Obj argObj = store.getObj(argArrayAddr);
		if(this.cfg.getEntryNode().getStatement() instanceof FunctionNode) {
			FunctionNode function = (FunctionNode)this.cfg.getEntryNode().getStatement();
			int i = 0;
			for(AstNode param : function.getParams()) {
				if(param instanceof Name) {
					Name paramName = (Name) param;
					Address argAddr = argObj.externalProperties.get(new Identifier(String.valueOf(i), Change.u()));
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
					// TODO: should be weak update
					env = env.strongUpdate(identity, new Addresses(argAddr, Change.u()));
				}
				i++;
			}
		}
		
		/* Add 'this' to environment (points to caller's object or new object). */
		// TODO: should be weak update
		env = env.strongUpdate(new Identifier("this", Change.u()), new Addresses(selfAddr, Change.u()));
		
		return env;
		
	}

}