package commitminer.analysis.flow.factories;

import java.util.Map;
import java.util.Set;
import java.util.Stack;

import org.apache.commons.lang3.tuple.Pair;
import org.mozilla.javascript.ast.AstNode;
import org.mozilla.javascript.ast.ScriptNode;

import commitminer.analysis.flow.abstractdomain.Address;
import commitminer.analysis.flow.abstractdomain.Addresses;
import commitminer.analysis.flow.abstractdomain.BValue;
import commitminer.analysis.flow.abstractdomain.Change;
import commitminer.analysis.flow.abstractdomain.Control;
import commitminer.analysis.flow.abstractdomain.DefinerIDs;
import commitminer.analysis.flow.abstractdomain.Environment;
import commitminer.analysis.flow.abstractdomain.Scratchpad;
import commitminer.analysis.flow.abstractdomain.State;
import commitminer.analysis.flow.abstractdomain.Store;
import commitminer.analysis.flow.abstractdomain.Undefined;
import commitminer.analysis.flow.abstractdomain.Variable;
import commitminer.analysis.flow.trace.FSCI;
import commitminer.analysis.flow.trace.Trace;
import commitminer.cfg.CFG;
import commitminer.js.analysis.scope.GlobalVisitor;

/**
 * Initializes the state from a JavaScript file AST.
 */
public class StateFactory {

	/**
	 * @param script The file under analysis.
	 * @return The initial state ς ∈ State := ρ x σ
	 */
	public static State createInitialState(ScriptNode script, Map<AstNode, CFG> cfgs) {
		Trace trace = new FSCI(script.getID());
		Store store = StoreFactory.createInitialStore(cfgs);
		Pair<Environment, Store> lifted =
				EnvironmentFactory.createInitialEnvironment(script, store, cfgs, trace);
		Scratchpad scratchpad = new Scratchpad();
		Control control = new Control();
		Environment env = lifted.getLeft();
		store = lifted.getRight();
		store = liftGlobals(script, trace, env, store);
		return new State(store, env, scratchpad,
						 trace, control, StoreFactory.global_binding_Addr,
						 cfgs, new Stack<Address>());
	}
	
	private static Store liftGlobals(ScriptNode script, Trace trace, Environment env, Store store) {

		/* Lift global variables into the environment and initialize to undefined. */
		Set<String> globals = GlobalVisitor.getGlobals(script);
		int i = -1000;
		for(String global : globals) {
			Address address = trace.makeAddr(i, "");
			env.strongUpdateNoCopy(global, new Variable(i, global, Change.bottom(), new Addresses(address, Change.u())));
			store = store.alloc(address, BValue.top(Change.u(), Change.u(), Change.u()));
			i--;
		}
		
		return store;
		
	}

}