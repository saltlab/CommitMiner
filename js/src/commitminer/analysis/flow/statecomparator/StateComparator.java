package commitminer.analysis.flow.statecomparator;

import commitminer.analysis.flow.abstractdomain.State;

/**
 * Compare two states. Used to decide whether or not a function needs to be
 * re-analyzed.
 */
public abstract class StateComparator {
	
	protected State s1;
	protected State s2;
	
	public StateComparator(State s1, State s2) {
		this.s1 = s1;
		this.s2 = s2;
	}

	public abstract boolean isEqual();
	
}
