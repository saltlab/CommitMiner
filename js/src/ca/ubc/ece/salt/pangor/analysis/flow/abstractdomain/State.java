package ca.ubc.ece.salt.pangor.analysis.flow.abstractdomain;

import org.mozilla.javascript.ast.AstNode;

import ca.ubc.ece.salt.pangor.analysis.flow.IState;
import ca.ubc.ece.salt.pangor.analysis.flow.trace.Trace;
import ca.ubc.ece.salt.pangor.cfg.CFGEdge;
import ca.ubc.ece.salt.pangor.cfg.CFGNode;

/**
 * Stores the state of the function analysis at a point in the CFG.
 */
public class State implements IState {

	/* The abstract domains that make up the program state. The abstract
	 * domains have access to each other. */

	public Environment environment;
	public Store store;
	public Scratchpad scratchpad;
	public Trace trace;

	/**
	 * Create a new state after a transfer or join.
	 * @param store The abstract store of the new state.
	 * @param environment The abstract environment of the new state.
	 */
	public State(Store store, Environment environment, Scratchpad scratchpad, Trace trace) {
		this.store = store;
		this.environment = environment;
		this.scratchpad = scratchpad;
		this.trace = trace;
	}

	public State transfer(CFGEdge edge, Address selfAddr) {

		/* Update the trace to the current condition. */
		this.trace = this.trace.update(edge.getId());

		return this;

	}

	public State transfer(CFGNode node, Address selfAddr) {

		/* Update the trace to the current statement. */
		this.trace = this.trace.update(node.getId());

		/* The statement to transfer over. */
		AstNode statement = (AstNode)node.getStatement();

		/* Evaluate the expression and update the abstract domains. */
		ExpEval.eval(environment, store, scratchpad, trace, statement, selfAddr);

		return this;

	}

	/**
	 * We should only join states from the same trace.
	 * @param state The state to join with.
	 * @return A state representing the join of the two states.
	 */
	public State join(State state) {

		if(state == null) return this;

		State joined = new State(
				this.store.join(state.store),
				this.environment.join(state.environment),
				this.scratchpad.join(state.scratchpad),
				this.trace);

		return joined;

	}

}
