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
	public State run(BValue selfAddr, BValue argArrayAddr, Store store,
			Scratchpad scratchpad, Trace trace) {

		/* Lift local variables and function declarations into the environment. */
		Pair<Environment, Store> pair = Helpers.lift(this.environment,
				store, (ScriptNode)cfg.getEntryNode().getStatement(), cfgs, trace);
		Environment env = pair.getLeft();
		store = pair.getRight();

		/* Match parameters with arguments. */
		Obj argObj;
		for(Address addr : argArrayAddr.addressAD.addresses) {
			Obj tmp = store.getObj(addr);
			argObj = argObj == null ? tmp : tmp != null ? argObj.join(tmp) : null;
		}
		if(this.cfg.getEntryNode().getStatement() instanceof FunctionNode) {
			FunctionNode function = (FunctionNode)this.cfg.getEntryNode().getStatement();
			int i = 0;
			for(AstNode arg : function.getParams()) {
				if(arg instanceof Name) {
					Name name = (Name) arg;
					BValue argInStore = store.apply(argObj.externalProperties.get(String.valueOf(i)).addressAD);
					store.alloc(env.apply(name.toSource()), argInStore); // TODO: This should be a strong update.
				}
				i++;
			}
		}

		/* Add 'this' to environment (points to caller's object or new object). */
		Address address = trace.makeAddr((int)this.cfg.getEntryNode().getId());
		store = store.alloc(address, selfAddr);
		env = env.strongUpdate("this", address);

		/* Match parameter names with arguments. */
		// TODO

		/* Create the initial state for the function call. */
		State state = new State(store, env, scratchpad, trace);

		/* Run the analysis on the CFG. */
		return Helpers.run(cfg, state, selfAddr);

	}

}