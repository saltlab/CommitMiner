package ca.ubc.ece.salt.pangor.analysis.flow.abstractdomain;

import java.util.Set;

import org.mozilla.javascript.ast.Assignment;
import org.mozilla.javascript.ast.AstNode;
import org.mozilla.javascript.ast.EmptyStatement;
import org.mozilla.javascript.ast.ExpressionStatement;
import org.mozilla.javascript.ast.FunctionCall;
import org.mozilla.javascript.ast.VariableDeclaration;
import org.mozilla.javascript.ast.VariableInitializer;

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

		/* Interpret the statement. */
		interpret(statement, selfAddr);

		return this;

	}

	/**
	 * Performs an abstract interpretation on the node.
	 * @return The updated state.
	 */
	private void interpret(AstNode node, Address selfAddr) {

		if(node instanceof EmptyStatement) { /* Skip. */ }
		else if(node instanceof ExpressionStatement) {
			interpret(((ExpressionStatement)node).getExpression(), selfAddr);
		}
		else if(node instanceof VariableDeclaration) {
			interpretVariableDeclaration((VariableDeclaration)node, selfAddr);
		}
		else if(node instanceof FunctionCall) {
			State endState = ExpEval.evalFunctionCall(environment, store, scratchpad, trace, (FunctionCall) node, selfAddr);
			this.store = this.store.join(endState.store);
		}
		else if(node instanceof Assignment) {
			interpretAssignment(selfAddr, (Assignment)node);
		}

	}

	/**
	 * Updates the store based on abstract interpretation of assignments.
	 * @param vd The variable declaration. Variables have already been
	 * lifted into the environment.
	 */
	public void interpretVariableDeclaration(VariableDeclaration vd, Address selfAddr) {
		for(VariableInitializer vi : vd.getVariables()) {
			if(vi.getInitializer() != null) {
				concreteAssignInterpreter(selfAddr, vi.getTarget(), vi.getInitializer());
			}
		}
	}

	/**
	 * Updates the store based on abstract interpretation of assignments.
	 * @param a The assignment.
	 */
	private void interpretAssignment(Address selfAddr, Assignment a) {
		concreteAssignInterpreter(selfAddr, a.getLeft(), a.getRight());
	}

	/**
	 * Helper function since variable initializers and assignments do the same thing.
	 */
	private void concreteAssignInterpreter(Address selfAddr, AstNode lhs, AstNode rhs) {

		/* Resolve the left hand side to a set of addresses. */
		Set<Address> addrs = Helpers.resolve(environment, store, lhs);

		/* Resolve the right hand side to a value. */
		BValue val = ExpEval.eval(environment, store, scratchpad, trace, rhs, selfAddr);

		/* Update the values in the store. */
		// TODO: Is this correct? We should probably only do a strong update if
		//		 there is only one address. Otherwise we don't know which one
		//		 to update.
		for(Address addr : addrs) {
			store = store.strongUpdate(addr, val);
		}

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
