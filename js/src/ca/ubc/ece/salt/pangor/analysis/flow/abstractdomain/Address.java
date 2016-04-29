package ca.ubc.ece.salt.pangor.analysis.flow.abstractdomain;


/**
 * The abstract domain for an address.
 */
public class Address {

	private static Long uniqueAddress = new Long(0);

	/** The address. **/
	private Long address;

	private Address() {
		this.address = Address.getUniqueAddress();
	}

	@Override
	public boolean equals(Object o) {
		if(o instanceof Address) {
			Address a = (Address)o;
			if(this.address == a.address) return true;
		}
		return false;
	}

	private static synchronized Long getUniqueAddress() {
		Long address = Address.uniqueAddress;
		Address.uniqueAddress = Address.uniqueAddress + 1;
		return address;
	}

}