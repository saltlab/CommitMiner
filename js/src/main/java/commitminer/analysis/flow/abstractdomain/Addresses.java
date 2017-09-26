package commitminer.analysis.flow.abstractdomain;

import java.util.HashSet;
import java.util.Set;

/**
 * The abstract domain for the possible addresses pointed to by a BValue.
 */
public class Addresses {

	private static final int MAX_SIZE = 10;

	public LatticeElement le;
	public Change change;

	/** Empty if LE = TOP. **/
	public Set<Address> addresses;

	/**
	 * Create the bottom lattice element.
	 */
	public Addresses(LatticeElement le, Change change) {
		this.addresses = new HashSet<Address>();
		this.le = le;
		this.change = change;
	}

	public Addresses(Set<Address> addresses, Change change) {
		this.addresses = addresses;
		this.le = LatticeElement.SET;
		this.change = change;
	}

	public Addresses(Address address, Change change) {
		this.addresses = new HashSet<Address>();
		this.addresses.add(address);
		this.le = LatticeElement.SET;
		this.change = change;
	}

	public Addresses(LatticeElement le, Set<Address> addresses, Change change) {
		this.addresses = addresses;
		this.le = le;
		this.change = change;
	}


	/**
	 * Performs a weak update on the set of addresses.
	 */
	public Addresses weakUpdate(Set<Address> addresses, Change change) {
		return this.join(new Addresses(addresses, change));
	}

	/**
	 * Performs a strong update on the set of addresses.
	 */
	public Addresses strongUpdate(Set<Address> addresses, Change change) {
		if(addresses.size() > MAX_SIZE)
			return new Addresses(LatticeElement.TOP, change);
		return new Addresses(addresses, change);
	}

	/**
	 * Joins this address with another address.
	 * @param a The address to join with.
	 * @return A new address that is the join of the two addresses.
	 */
	public Addresses join(Addresses a) {

		Change jc = this.change.join(a.change);

		if(a.le == LatticeElement.BOTTOM)
			return new Addresses(this.le, new HashSet<Address>(this.addresses), jc);
		if(this.le == LatticeElement.BOTTOM)
			return new Addresses(a.le, new HashSet<Address>(a.addresses), jc);
		if(this.le == LatticeElement.TOP || a.le == LatticeElement.TOP)
			return new Addresses(LatticeElement.TOP, jc);

		if(this.addresses.size() + a.addresses.size() > MAX_SIZE)
			return new Addresses(LatticeElement.TOP, jc);

		/* Join the two address sets. */
		HashSet<Address> newAddressSet = new HashSet<Address>(this.addresses);
		newAddressSet.addAll(a.addresses);
		return new Addresses(LatticeElement.SET, newAddressSet, jc);

	}

	/**
	 * @param addresses The address lattice element to inject.
	 * @return The base value tuple with injected addresses.
	 */
	public static BValue inject(Addresses addresses, Change valChange, Change dependency, DefinerIDs definerIDs) {
		return new BValue(
				Str.bottom(addresses.change),
				Num.bottom(addresses.change),
				Bool.bottom(addresses.change),
				Null.bottom(addresses.change),
				Undefined.bottom(addresses.change),
				addresses,
				valChange,
				dependency,
				definerIDs);
	}

	public static BValue dummy(Change valChange, Change dependency, Change typeChange, DefinerIDs definerIDs) {
		return new BValue(
				Str.top(typeChange),
				Num.top(typeChange),
				Bool.top(typeChange),
				Null.top(typeChange),
				Undefined.top(typeChange),
				Addresses.bottom(typeChange),
				valChange,
				dependency,
				definerIDs);
	}

	/**
	 * @return The top lattice element.
	 */
	public static Addresses top(Change change) {
		return new Addresses(LatticeElement.TOP, change);
	}

	/**
	 * @return The bottom lattice element.
	 */
	public static Addresses bottom(Change change) {
		return new Addresses(LatticeElement.BOTTOM, change);
	}

	/** The lattice elements for the abstract domain. **/
	public enum LatticeElement {
		TOP,	// May point to any address.
		SET,	// Some (limited?) combination of addresses.
		BOTTOM	// Does not point to an address (empty set)
	}

	@Override
	public String toString() {
		if(this.le != LatticeElement.SET) {
			return "Addr:" + this.le.toString();
		}
		else {
			String addrs = "Addrs: {";
			for(Address addr : this.addresses) {
				addrs += "Addr:" + addr + ",";
			}
			return addrs.substring(0, addrs.length() - 1) + "}";
		}
	}
	
	@Override
	public boolean equals(Object o) {
		if(!(o instanceof Addresses)) return false;
		Addresses addrs = (Addresses)o;
		if(this.le != addrs.le || !this.change.equals(addrs.change)) return false;
		if(this.addresses.size() != addrs.addresses.size()) return false;
		for(Address addr : this.addresses) {
			if(!addrs.addresses.contains(addr)) return false;
		}
		return true;
	}

}
