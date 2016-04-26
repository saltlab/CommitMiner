package ca.ubc.ece.salt.pangor.analysis.flow;

/**
 * A memory address.
 */
public class Address {

	/** For generating unique addresses for the store. **/
	private static long uniqueAddress = 0;

	/** This address. **/
	private long address;

	public Address() {
		this.address = allocateNewAddress();
	}

	@Override
	public boolean equals(Object o) {
		if(o instanceof Address) {
			Address a = (Address)o;
			return this.address == a.address;
		}
		return false;
	}

	private static synchronized long allocateNewAddress() {
		long address = uniqueAddress;
		uniqueAddress++;
		return address;
	}

}
