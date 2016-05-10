package ca.ubc.ece.salt.pangor.analysis.flow.abstractdomain;

import org.mozilla.javascript.ast.AstNode;
import org.mozilla.javascript.ast.ExpressionStatement;
import org.mozilla.javascript.ast.FunctionCall;

import ca.ubc.ece.salt.pangor.analysis.flow.IState;
import ca.ubc.ece.salt.pangor.analysis.flow.abstractdomain.Scratchpad.Scratch;
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

	public State transfer(CFGEdge edge, BValue self) {
		// TODO Auto-generated method stub
		return this;
	}

	public State transfer(CFGNode node, BValue self) {

		AstNode statement = (AstNode)node.getStatement();

		/* Test out a function call. */
		if(statement instanceof ExpressionStatement) {
			ExpressionStatement exs = (ExpressionStatement) statement;
			AstNode ex = exs.getExpression();

			if(ex instanceof FunctionCall) {
				FunctionCall fc = (FunctionCall) ex;

				/* Attempt to resolve the function and it's parent object. */
				BValue fun = Helpers.resolve(this.environment, this.store, fc.getTarget());
				BValue obj = Helpers.resolveSelf(this.environment, this.store, fc.getTarget());

				if(obj == null) obj = self;

				if(fun == null) {
					/* If the function was not resolved, we assume the (local)
					 * state is unchanged, but add BValue.TOP as the return value. */
					Scratchpad scratchpad = new Scratchpad(Scratch.RETVAL, BValue.top());
					return new State(this.store, this.environment, scratchpad, this.trace);
				}
				else {
					/* Call the function and get a join of the new states. */
					// TODO args (3rd argument)
					return Helpers.applyClosure(fun, obj, null, this.store,
												this.scratchpad, this.trace);
				}
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
