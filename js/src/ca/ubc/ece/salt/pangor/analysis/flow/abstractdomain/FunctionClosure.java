package ca.ubc.ece.salt.pangor.analysis.flow.abstractdomain;

import java.util.Map;

import org.apache.commons.lang3.tuple.Pair;
import org.mozilla.javascript.ast.AstNode;
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
	}

	@Override
	public State run(BValue selfAddr, BValue argArrayAddr, String x,
			Environment env, Store store, Scratchpad scratchpad,
			Trace trace) {

		/* Lift local variables and function declarations into the environment. */
		Pair<Environment, Store> pair = Helpers.lift(env,
				store, (ScriptNode)cfg.getEntryNode().getStatement(), cfgs, trace);
		env = pair.getLeft();
		store = pair.getRight();

		/* Add 'this' to environment (points to caller's object or new object). */
		Address address = trace.makeAddr((int)this.cfg.getEntryNode().getId());
		store = store.alloc(address, selfAddr);
		env = env.strongUpdate("this", new Addresses(address));

		/* Match parameter names with arguments. */
		// TODO

		/* Create the initial state for the function call. */
		State state = new State(store, env, scratchpad, trace);

		/* Run the analysis on the CFG. */
		return Helpers.run(cfg, state);

	}

}