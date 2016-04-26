package ca.ubc.ece.salt.pangor.analysis.flow.abstractdomain;

import java.util.HashSet;
import java.util.Set;

import ca.ubc.ece.salt.pangor.analysis.flow.Address;
import ca.ubc.ece.salt.pangor.cfg.CFGEdge;
import ca.ubc.ece.salt.pangor.cfg.CFGNode;

/**
 * Stores the state for the address abstract domain. The lattice is the
 * powerset of all possible addresses.
 */
public class AddressAD implements IAbstractDomain {

	/** The possible addresses this points to. **/
	Set<Address> addresses;

	public AddressAD(Set<Address> addresses) {
		this.addresses = addresses;
	}

	@Override
	public IAbstractDomain transfer(CFGEdge edge) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public IAbstractDomain transfer(CFGNode node) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public IAbstractDomain join(IAbstractDomain istate) {
		if(!(istate instanceof AddressAD)) throw new IllegalArgumentException("Attempted to join " + istate.getClass().getName() + " with " + AddressAD.class.getName());
		AddressAD state = (AddressAD) istate;

		HashSet<Address> addresses = new HashSet<Address>();
		addresses.addAll(this.addresses);
		addresses.addAll(state.addresses);

		return new AddressAD(addresses);
	}

}
