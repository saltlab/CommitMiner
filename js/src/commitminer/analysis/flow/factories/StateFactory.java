package commitminer.analysis.flow.factories;

import java.util.Map;
import java.util.Stack;

import org.apache.commons.lang3.tuple.Pair;
import org.deri.iris.api.basics.IPredicate;
import org.deri.iris.storage.IRelation;
import org.mozilla.javascript.ast.AstNode;
import org.mozilla.javascript.ast.ScriptNode;

import commitminer.analysis.flow.abstractdomain.Address;
import commitminer.analysis.flow.abstractdomain.Control;
import commitminer.analysis.flow.abstractdomain.Environment;
import commitminer.analysis.flow.abstractdomain.Scratchpad;
import commitminer.analysis.flow.abstractdomain.State;
import commitminer.analysis.flow.abstractdomain.Store;
import commitminer.analysis.flow.trace.FSCI;
import commitminer.analysis.flow.trace.Trace;
import commitminer.cfg.CFG;

/**
 * Initializes the state from a JavaScript file AST.
 */
public class StateFactory {

	/**
	 * @param script The file under analysis.
	 * @return The initial state ς ∈ State := ρ x σ
	 */
	public static State createInitialState(Map<IPredicate, IRelation> facts,
		ScriptNode script, Map<AstNode, CFG> cfgs) {
		Trace trace = new FSCI(script.getID());
		Store store = StoreFactory.createInitialStore(cfgs);
		Pair<Environment, Store> lifted =
				EnvironmentFactory.createInitialEnvironment(facts, script, store, cfgs, trace);
		Scratchpad scratchpad = new Scratchpad();
		Control control = new Control();
		return new State(facts, lifted.getRight(), lifted.getLeft(), scratchpad,
						 trace, control, StoreFactory.global_binding_Addr,
						 cfgs, new Stack<Address>());
	}

}