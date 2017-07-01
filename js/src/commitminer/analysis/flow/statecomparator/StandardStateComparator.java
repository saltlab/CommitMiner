package commitminer.analysis.flow.statecomparator;

import java.util.HashSet;
import java.util.Set;

import commitminer.analysis.flow.abstractdomain.Address;
import commitminer.analysis.flow.abstractdomain.BValue;
import commitminer.analysis.flow.abstractdomain.Obj;
import commitminer.analysis.flow.abstractdomain.Property;
import commitminer.analysis.flow.abstractdomain.State;
import commitminer.analysis.flow.abstractdomain.Variable;

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
		for(Variable var : s1.env.environment.values()) {
			for(Address addr : var.addresses.addresses) {

				/* Check the values are the same. */
				if(!equalVal(addr)) return false;

			}
		}
		
		/* Check if there is a control change to propagate. */
		if(!s1.control.equals(s2.control))
			return false;
		
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
			for(Property prop : s1Obj.externalProperties.values()) {
				if(!equalVal(prop.address)) return false;
			}
		}
		
		return true;

	}
	

}
