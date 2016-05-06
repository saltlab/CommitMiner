package ca.ubc.ece.salt.pangor.analysis.flow.abstractdomain;

import java.util.HashSet;
import java.util.Set;


/**
 * The abstract domain for an address.
 */
public class Address extends SmartHash {

	/** A unique address for builtins. **/
	private static long builtinAddress = -1;

	/** The address. **/
	private long address;

	/**
	 * @param addr The abstract address.
	 */
	public Address(long addr) {
		this.address = addr;
	}

	@Override
	public boolean equals(Object o) {
		if(o instanceof Address) {
			Address a = (Address)o;
			if(this.address == a.address) return true;
		}
		return false;
	}

	/**
	 * @param address The address lattice element to inject.
	 * @return The base value tuple with injected address.
	 */
	public static BValue inject(Address address) {
		Set<Address> addresses = new HashSet<Address>();
		addresses.add(address);
		return new BValue(
				Str.bottom(),
				Num.bottom(),
				Bool.bottom(),
				Null.bottom(),
				Undefined.bottom(),
				new Addresses(addresses));
	}

	/**
	 * Builtin abstract addresses get negative values Same as in JSAI.
	 */
	public static synchronized Address createBuiltinAddr() {
		Address address = new Address(builtinAddress);
		builtinAddress--;
		return address;
	}

}