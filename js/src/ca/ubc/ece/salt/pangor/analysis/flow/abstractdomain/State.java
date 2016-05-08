package ca.ubc.ece.salt.pangor.analysis.flow.abstractdomain;

import ca.ubc.ece.salt.pangor.analysis.flow.trace.Trace;
import ca.ubc.ece.salt.pangor.cfg.CFGEdge;
import ca.ubc.ece.salt.pangor.cfg.CFGNode;

/**
 * Stores the state of the function analysis at a point in the CFG.
 */
public class State {

	/* The abstract domains that make up the program state. The abstract
	 * domains have access to each other. */

	public Environment environment;
	public Store store;
	public Trace trace;

	/**
	 * Create a new state after a transfer or join.
	 * @param store The abstract store of the new state.
	 * @param environment The abstract environment of the new state.
	 */
	public State(Store store, Environment environment, Trace trace) {
		this.store = store;
		this.environment = environment;
		this.trace = trace;
	}

	public State transfer(CFGEdge edge) {
		//this.environment = (Environment) this.environment.transfer(edge);
		return null;
	}

	public State transfer(CFGNode node) {
		// TODO Auto-generated method stub
		return null;
	}

	/**
	 * We should only join states from the same trace.
	 * @param state The state to join with.
	 * @return A state representing the join of the two states.
	 */
	public State join(State state) {

		if(this.trace != state.trace) throw new Error("Cannot join states with different traces.");

		State joined = new State(
				this.store.join(state.store),
				this.environment.join(state.environment),
				this.trace);

		return joined;

	}

}
