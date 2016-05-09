package ca.ubc.ece.salt.pangor.analysis.flow.factories;

import java.util.Map;

import org.apache.commons.lang3.tuple.Pair;
import org.mozilla.javascript.ast.AstNode;
import org.mozilla.javascript.ast.ScriptNode;

import ca.ubc.ece.salt.pangor.analysis.flow.abstractdomain.Environment;
import ca.ubc.ece.salt.pangor.analysis.flow.abstractdomain.Scratchpad;
import ca.ubc.ece.salt.pangor.analysis.flow.abstractdomain.State;
import ca.ubc.ece.salt.pangor.analysis.flow.abstractdomain.Store;
import ca.ubc.ece.salt.pangor.analysis.flow.trace.FSCI;
import ca.ubc.ece.salt.pangor.analysis.flow.trace.Trace;
import ca.ubc.ece.salt.pangor.cfg.CFG;

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
		Store store = StoreFactory.createInitialStore();
		Pair<Environment, Store> lifted =
				EnvironmentFactory.createInitialEnvironment(script, store, cfgs, trace);
		Scratchpad scratchpad = new Scratchpad();
		return new State(lifted.getRight(), lifted.getLeft(), scratchpad, trace);
	}

}