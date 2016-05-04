package ca.ubc.ece.salt.pangor.analysis.flow.abstractdomain;

import java.util.HashSet;
import java.util.Set;

/**
 * The abstract domain for the possible addresses pointed to by a BValue.
 */
public class Addresses {

	public LatticeElement le;

	/** null if LE = TOP. **/
	public Set<Address> addresses;

	/**
	 * Create the bottom lattice element.
	 */
	public Addresses(LatticeElement le) {
		this.addresses = new HashSet<Address>();
		this.le = le;
	}

	public Addresses(Set<Address> addresses) {
		this.addresses = addresses;
		this.le = LatticeElement.SET;
	}

	public Addresses(LatticeElement le, Set<Address> addresses) {
		this.addresses = new HashSet<Address>();
		this.le = le;
	}

	/**
	 * Joins this address with another address.
	 * @param a The address to join with.
	 * @return A new address that is the join of the two addresses.
	 */
	public Addresses join(Addresses a) {

		if(a.le == LatticeElement.BOTTOM)
			return new Addresses(this.le, new HashSet<Address>(this.addresses));
		if(this.le == LatticeElement.BOTTOM)
			return new Addresses(a.le, new HashSet<Address>(a.addresses));
		if(this.le == LatticeElement.TOP || a.le == LatticeElement.TOP)
			return new Addresses(LatticeElement.TOP, null);

		/* We set a limit on the number of addresses so that there is a finite
		 * address space. This is in lieu of abstracting the abstract machine. */
		if(this.addresses.size() + a.addresses.size() > 3)
			return new Addresses(LatticeElement.TOP, null);

		/* Join the two address sets. */
		HashSet<Address> newAddressSet = new HashSet<Address>(this.addresses);
		newAddressSet.addAll(a.addresses);
		return new Addresses(LatticeElement.SET, newAddressSet);

	}

	/**
	 * @param addresses The address lattice element to inject.
	 * @return The base value tuple with injected addresses.
	 */
	public BValue inject(Addresses addresses) {
		return new BValue(
				Str.bottom(),
				Num.bottom(),
				Bool.bottom(),
				Null.bottom(),
				Undefined.bottom(),
				addresses);
	}

	/**
	 * @return The top lattice element.
	 */
	public static Addresses top() {
		return new Addresses(LatticeElement.TOP);
	}

	/**
	 * @return The bottom lattice element.
	 */
	public static Addresses bottom() {
		return new Addresses(LatticeElement.BOTTOM);
	}

	/** The lattice elements for the abstract domain. **/
	public enum LatticeElement {
		TOP,	// May point to any address.
		SET,	// Some (limited?) combination of addresses.
		BOTTOM	// Does not point to an address (empty set)
	}

}
