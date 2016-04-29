package ca.ubc.ece.salt.pangor.analysis.flow.abstractdomain;

import java.util.HashMap;
import java.util.Map;

import ca.ubc.ece.salt.pangor.analysis.flow.Address;
import ca.ubc.ece.salt.pangor.analysis.flow.IAbstractDomain;

/**
 * The abstract domain for the program's store (memory).
 * i.e. Store# := Address# -> P(BValue# + Object#)
 */
public class Store {

	/** The data structures at each address. **/
	private Map<Address, IAbstractDomain> store;

	/**
	 * Create the initial state for the store. The initial state is an empty
	 * store. However, the environment may initialize the store with the
	 * following objects before the analysis begins:
	 * 	(1) variables declared in the function and raised - which will initially
	 * 		point to the primitive value 'undefined'.
	 * 	(2) functions declared in the function and raised - which will point to
	 * 		their function object.
	 *
	 * Changes to the store are driven by changes to the environment.
	 */
	public Store() {
		/* Right now we only have an empty store. Future work is to initialize
		 * the store with built-in objects. */
		this.store = new HashMap<Address, IAbstractDomain>();
	}

	private Store(Map<Address, IAbstractDomain> store) {
		this.store = store;
	}

	/**
	 * @return a new Store which is this Store joined with the store parameter.
	 */
	public Store join(Store state) {

		/* Just copy the values into a new hash map. The values are essentially
		 * immutable since any transfer or join will produce a new value. */
		Map<Address, IAbstractDomain> joined = new HashMap<Address, IAbstractDomain>(this.store);

		/* Join the new abstract domain with the new map. New lattice elements
		 * are created for each join. */
		for(Address address : state.store.keySet()) {
			if(joined.containsKey(address)) {
				joined.put(address, joined.get(address).join(state.store.get(address)));
			}
			else {
				joined.put(address, joined.get(address).join(new BValue()));
			}
		}

		return new Store(joined);

	}

	/**
	 * Allocates space in the store for a {@code BValue}.
	 * @param value The value to place in the store.
	 * @return The address of the value in the store.
	 */
	public Address alloc(BValue value) {
		// TODO: Template
		return null;
	}

	/**
	 * Allocates space in the store for a {@code Closure} (function).
	 * @param closure The function to allocate.
	 * @param value The address of the function prototype?
	 * @return The address of the function in the store.
	 */
	public Address allocFun(Closure closure, BValue value) {
		// TODO: Template
		return null;
	}

	/**
	 * Allocates space in the store for an object.
	 * @param constructor The address of the object's constructor.
	 * @return The address of the object in the store.
	 */
	public Address allocObj(BValue constructor) {
		// TODO: Template
		return null;
	}


}