package commitminer.analysis.flow.statecomparator;

import commitminer.analysis.flow.abstractdomain.State;

public class StateComparatorFactory {

	public static StateComparator instance(State s1, State s2) {
		return new StandardStateComparator(s1, s2);
	}

}
