package ca.ubc.ece.salt.pangor.analysis.flow.abstractdomain;

import java.util.Map;
import java.util.Stack;

import org.apache.commons.lang3.tuple.Pair;
import org.mozilla.javascript.ast.AstNode;
import org.mozilla.javascript.ast.FunctionNode;
import org.mozilla.javascript.ast.Name;
import org.mozilla.javascript.ast.ScriptNode;

import ca.ubc.ece.salt.pangor.analysis.flow.trace.Trace;
import ca.ubc.ece.salt.pangor.cfg.CFG;
import ca.ubc.ece.salt.pangor.cfg.CFGNode;

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

	public FunctionClosure(CFG cfg, Environment environment, Map<AstNode, CFG> cfgs) {
		this.cfg = cfg;
		this.environment = environment;
		this.cfgs = cfgs;
	}

	/**
	 * @return true if the analysis needs to be (re-)run on the function
	 */
	private boolean runAnalysis(Control control, Address argArrayAddr, Store store) {

		State initState = (State) cfg.getEntryNode().getBeforeState();

		/* Run the analysis on the function if it has not yet run. */
		if(initState == null) return true;

		/* Re-run the analysis if there are control changes from the caller
		 * state, but not in the initial state. */
		if(!control.conditions.isEmpty() && initState.control.conditions.isEmpty()) return true;

		/* Re-run the analysis if there are value changes in the arg list. */
		Obj argObj = store.getObj(argArrayAddr);
		for(Address address : argObj.externalProperties.values()) {
			BValue value = store.apply(address);
			switch(value.change.le) {
			case CHANGED:
			case TOP:
				return true;
			default:
				continue;
			}
		}

		return false;

	}

	@Override
	public State run(Address selfAddr, Address argArrayAddr, Store store,
			Scratchpad scratchpad, Trace trace, Control control,
			Stack<Address> callStack) {

		/* If this has already been analyzed, we can short-circuit. */
		boolean runAnalysis = this.runAnalysis(control, argArrayAddr, store);
		if(!runAnalysis) { //!cfg.getExitNodes().isEmpty()) {

			State exitState = null;

			for(CFGNode exitNode : cfg.getExitNodes()) {
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

		/* Lift local variables and function declarations into the environment. */
		Pair<Environment, Store> pair = Helpers.lift(this.environment,
				store, (ScriptNode)cfg.getEntryNode().getStatement(), cfgs, trace);
		Environment env = pair.getLeft();
		store = pair.getRight();

		/* Match parameters with arguments. */
		Obj argObj = store.getObj(argArrayAddr);
		if(this.cfg.getEntryNode().getStatement() instanceof FunctionNode) {
			FunctionNode function = (FunctionNode)this.cfg.getEntryNode().getStatement();
			int i = 0;
			for(AstNode param : function.getParams()) {
				if(param instanceof Name) {
					Name paramName = (Name) param;
					Address argAddr = argObj.externalProperties.get(new Identifier(String.valueOf(i), Change.u()));
					Identifier identity = new Identifier(paramName.toSource(), Change.conv(paramName));
					env = env.strongUpdate(identity, argAddr);
				}
				i++;
			}
		}

		/* Add 'this' to environment (points to caller's object or new object). */
		env = env.strongUpdate(new Identifier("this", Change.u()), selfAddr);

		/* Create the initial state for the function call. */
		State state = new State(store, env, scratchpad, trace, control, selfAddr, cfgs, callStack);

		/* Run the analysis on the CFG. */
		return Helpers.run(cfg, state);
	}

}