package ca.ubc.ece.salt.pangor.analysis.flow.abstractdomain;

import java.util.HashMap;
import java.util.Map;

import ca.ubc.ece.salt.pangor.analysis.flow.Address;

/**
 * The abstract domain for the program's store (memory).
 * i.e. Store# := Address# -> P(BValue# + Object#)
 */
public class Store {

	/** The store for {@code BValue}s. **/
	private Map<Address, BValue> bValueStore;

	/** The store for {@code Object}s. **/
	private Map<Address, ObjectAD> objectStore;

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
		this.bValueStore = new HashMap<Address, BValue>();
	}

	private Store(Map<Address, BValue> bValueStore,
				  Map<Address, ObjectAD> objectStore) {
		this.bValueStore = bValueStore;
		this.objectStore = objectStore;
	}

	/**
	 * @return a new Store which is this Store joined with the store parameter.
	 */
	public Store join(Store state) {

		/* Just copy the values into a new hash map. The values are essentially
		 * immutable since any transfer or join will produce a new value. */
		Map<Address, BValue> bValueStore = new HashMap<Address, BValue>(this.bValueStore);
		Map<Address, ObjectAD> objectStore = new HashMap<Address, ObjectAD>(this.objectStore);

		/* Join the new abstract domain with the new map. New lattice elements
		 * are created for each join. */
		for(Address address : state.bValueStore.keySet()) {
			if(bValueStore.containsKey(address)) {
				bValueStore.put(address, bValueStore.get(address).join(state.bValueStore.get(address)));
			}
			else {
				bValueStore.put(address, bValueStore.get(address));
			}
		}

		for(Address address : state.objectStore.keySet()) {
			if(objectStore.containsKey(address)) {
				objectStore.put(address, objectStore.get(address).join(state.objectStore.get(address)));
			}
			else {
				objectStore.put(address, objectStore.get(address));
			}
		}

		return new Store(bValueStore, objectStore);

	}

	/**
	 * Allocates space in the store for a {@code BValue}.
	 * @param value The value to place in the store.
	 * @return The address of the value in the store.
	 */
	public Address alloc(BValue value) {

		/* Allocate a new address. */
		Address address = new Address();

		this.bValueStore.put(address, value);

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