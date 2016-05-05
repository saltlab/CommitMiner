package ca.ubc.ece.salt.pangor.analysis.flow.abstractdomain;

import org.mozilla.javascript.ast.ScriptNode;

import ca.ubc.ece.salt.pangor.analysis.flow.IAbstractDomain;
import ca.ubc.ece.salt.pangor.cfg.CFGEdge;
import ca.ubc.ece.salt.pangor.cfg.CFGNode;

/**
 * Stores the state of the function analysis at a point in the CFG.
 */
public class State implements IAbstractDomain {

	/* The abstract domains that make up the program state. The abstract
	 * domains have access to each other. */

	public Store store;
	public Environment environment;

	/**
	 * Create the initial state for a script or function.
	 * @param function The script of function we are analyzing.
	 */
	public State(ScriptNode function) {

		/* Initialize the default abstract domains. */
		this.store = new Store();
		this.environment = new Environment(function);

	}

	/**
	 * Create a new state after a transfer or join.
	 * @param store The abstract store of the new state.
	 * @param environment The abstract environment of the new state.
	 */
	public State(Store store, Environment environment) {
		this.store = store;
		this.environment = environment;
	}

	@Override
	public State transfer(CFGEdge edge) {
		//this.environment = (Environment) this.environment.transfer(edge);
		return null;
	}

	@Override
	public State transfer(CFGNode node) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public State join(IAbstractDomain istate) {

		if(!(istate instanceof State)) throw new IllegalArgumentException("Attempted to join " + istate.getClass().getName() + " with " + State.class.getName());
		State state = (State) istate;

		State joined = new State(
				this.store.join(state.store),
				this.environment.join(state.environment));

		return joined;

	}

}
