package commitminer.analysis.flow.statecomparator;

import java.util.HashSet;
import java.util.Set;

import commitminer.analysis.flow.abstractdomain.Address;
import commitminer.analysis.flow.abstractdomain.Addresses;
import commitminer.analysis.flow.abstractdomain.BValue;
import commitminer.analysis.flow.abstractdomain.Obj;
import commitminer.analysis.flow.abstractdomain.State;

public class StandardStateComparator extends StateComparator {
	
	Set<Address> visited;

	public StandardStateComparator(State s1, State s2) {
		super(s1, s2);
	}

	@Override
	public boolean isEqual() {
		visited = new HashSet<Address>();
		return equalState();
	}

	/**
	 * @return true if the states are equivalent with respect to environment and store.
	 */
	private boolean equalState() {

		/* Check the initial environment. */
		if(!s1.env.equals(s2.env))
			return false;
		
		/* Check the reachable values in the store. */
		for(Addresses addrs : s1.env.environment.values()) {
			for(Address addr : addrs.addresses) {

				/* Check the values are the same. */
				if(!equalVal(addr)) return false;

			}
		}
		
		/* Control change AD is not equal if: 
		 * 	there are NO control changes in the old state
		 * 	AND 
		 * there are control changes in the new state. */
		if(s1.control.conditions.isEmpty() && !s2.control.conditions.isEmpty()) return false;
		
		return true;

	}
	
	/**
	 * Compare the values of both states at the address.
	 * @param addr The address of the value.
	 * @return {@code true} if the values are equivalent.
	 */
	private boolean equalVal(Address addr) {

		if(visited.contains(addr)) return true;
		
		/* Don't re-visit this address. */
		visited.add(addr);
		
		/* Check that the values are the same. */
		BValue b1 = s1.store.apply(addr);
		BValue b2 = s2.store.apply(addr);
		if(!b1.equals(b2)) 
			return false;
		
		/* Check that the objects are the same. */
		for(Address objAddr : b1.addressAD.addresses) {
			Obj s1Obj = s1.store.getObj(objAddr);
			Obj s2Obj = s2.store.getObj(objAddr);
			if(!s1Obj.equals(s2Obj)) return false;
			
			/* Check that the object properties are the same. */
			for(Address propAddr : s1Obj.externalProperties.values()) {
				if(!equalVal(propAddr)) return false;
			}
		}
		
		return true;

	}
	

}
