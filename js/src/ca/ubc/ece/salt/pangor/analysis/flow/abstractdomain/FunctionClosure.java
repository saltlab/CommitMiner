package ca.ubc.ece.salt.pangor.analysis.flow.abstractdomain;

import java.util.Map;

import org.apache.commons.lang3.tuple.Pair;
import org.mozilla.javascript.ast.AstNode;
import org.mozilla.javascript.ast.FunctionNode;
import org.mozilla.javascript.ast.Name;
import org.mozilla.javascript.ast.ScriptNode;

import ca.ubc.ece.salt.pangor.analysis.flow.trace.Trace;
import ca.ubc.ece.salt.pangor.cfg.CFG;

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

	@Override
	public State run(Address selfAddr, Address argArrayAddr, Store store,
			Scratchpad scratchpad, Trace trace) {

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
					Address argAddr = argObj.externalProperties.get(String.valueOf(i));
					env = env.strongUpdate(paramName.toSource(), argAddr);
				}
				i++;
			}
		}

		/* Add 'this' to environment (points to caller's object or new object). */
		env = env.strongUpdate("this", selfAddr);

		/* Create the initial state for the function call. */
		State state = new State(store, env, scratchpad, trace, cfgs);

		/* Run the analysis on the CFG. */
		return Helpers.run(cfg, state, selfAddr);
	}

}