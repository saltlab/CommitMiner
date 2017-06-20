package commitminer.analysis.flow.statecomparator;

import java.util.HashSet;
import java.util.Set;

import commitminer.analysis.flow.abstractdomain.Address;
import commitminer.analysis.flow.abstractdomain.State;

public class ChangeStateComparator extends StateComparator {
	
	Set<Address> visited;

	public ChangeStateComparator(State s1, State s2) {
		super(s1, s2);
	}

	@Override
	public boolean isEqual() {
		visited = new HashSet<Address>();
		return changeEquivalentState();
	}
	
	/**
	 * @return true if the states are equivalent with respect to changes
	 */
	private boolean changeEquivalentState() {
		
		
		return false;
	}

}
