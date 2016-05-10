package ca.ubc.ece.salt.pangor.analysis.flow.abstractdomain;

import org.mozilla.javascript.ast.AstNode;
import org.mozilla.javascript.ast.ExpressionStatement;
import org.mozilla.javascript.ast.FunctionCall;

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

	public State transfer(CFGEdge edge) {
		// TODO Auto-generated method stub
		return this;
	}

	public State transfer(CFGNode node) {

		AstNode statement = (AstNode)node.getStatement();

		/* Test out a function call. */
		if(statement instanceof ExpressionStatement) {
			ExpressionStatement exs = (ExpressionStatement) statement;
			AstNode ex = exs.getExpression();

			if(ex instanceof FunctionCall) {
				FunctionCall fc = (FunctionCall) ex;

				/* Attempt to resolve the function. */
				BValue fun = Helpers.resolve(this.environment, this.store, fc.getTarget());

				/* Call the function and get a join of the new states. */
				State state = Helpers.applyClosure(fun, self, null/*TODO:args*/, this.store,
												   this.scratchpad, this.trace);
			}

		}

		return this;
	}

	/**
	 * We should only join states from the same trace.
	 * @param state The state to join with.
	 * @return A state representing the join of the two states.
	 */
	public State join(State state) {

		if(state == null) return this;
		if(this.trace != state.trace) throw new Error("Cannot join states with different traces.");

		State joined = new State(
				this.store.join(state.store),
				this.environment.join(state.environment),
				this.scratchpad.join(state.scratchpad),
				this.trace);

		return joined;

	}

}
