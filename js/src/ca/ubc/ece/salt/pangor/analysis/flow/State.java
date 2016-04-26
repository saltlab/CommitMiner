package ca.ubc.ece.salt.pangor.analysis.flow;

import org.mozilla.javascript.ast.ScriptNode;

/**
 * Stores the state of the analysis at a point in the CFG.
 */
public class State {

	/**
	 * Create the initial state for a script or function.
	 * @param function The script of function we are analyzing.
	 */
	public State(ScriptNode function) {
		// TODO: There should be some default abstract domains to initialize...
	}

}
