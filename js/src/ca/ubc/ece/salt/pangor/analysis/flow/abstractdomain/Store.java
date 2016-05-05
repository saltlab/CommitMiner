package ca.ubc.ece.salt.pangor.analysis.flow.abstractdomain;

import java.util.HashMap;
import java.util.Map;

/**
 * The abstract domain for the program's store (memory).
 * i.e. Store# := Address# -> P(BValue# + Object#)
 */
public class Store {

	/** The store for {@code BValue}s. **/
	private Map<Address, BValue> bValueStore;

	/** The store for {@code Object}s. **/
	private Map<Address, Obj> objectStore;

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
		/* Right now we only have an empty store.
		 * TODO: initialize the store with built-in objects. */
		this.bValueStore = new HashMap<Address, BValue>();
	}

	/**
	 * Create a new store from an existing store.
	 * @param bValueStore
	 * @param objectStore
	 */
	public Store(Map<Address, BValue> bValueStore,
				  Map<Address, Obj> objectStore) {
		this.bValueStore = bValueStore;
		this.objectStore = objectStore;
	}

	/**
	 * Computes σ1 u σ2.
	 * @return a new Store which is this Store joined with the store parameter.
	 */
	public Store join(Store store) {

		/* Just copy the values into a new hash map. The values are essentially
		 * immutable since any transfer or join will produce a new value. */

		Map<Address, BValue> bValueStore = new HashMap<Address, BValue>(this.bValueStore);
		Map<Address, Obj> objectStore = new HashMap<Address, Obj>(this.objectStore);

		/* Join the new abstract domain with the new map. New lattice elements
		 * are created for each join. */

		for(Map.Entry<Address, BValue>entries : store.bValueStore.entrySet()) {
			Address address = entries.getKey();
			BValue right = entries.getValue();
			BValue left = bValueStore.get(entries.getKey());

			if(left == null) bValueStore.put(address, right);
			else bValueStore.put(address, left.join(right));
		}

		for(Map.Entry<Address, Obj>entries : store.objectStore.entrySet()) {
			Address address = entries.getKey();
			Obj right = entries.getValue();
			Obj left = objectStore.get(entries.getKey());

			if(left == null) objectStore.put(address, right);
			else objectStore.put(address, left.join(right));
		}

		return new Store(bValueStore, objectStore);

	}

	/**
	 * Allocates a primitive value on the store. If the address already has a
	 * value, performs a weak update.
	 * @param address The address to allocate, generated in {@code Trace}.
	 * @param value The value to place in the store.
	 * @return The Store after allocation.
	 */
	public Store alloc(Address address, BValue value) {
		Map<Address, BValue> bValueStore = new HashMap<Address, BValue>(this.bValueStore);
		BValue left = bValueStore.get(address);
		if(left == null) this.bValueStore.put(address, value);
		else this.bValueStore.put(address, left.join(value));
		return new Store(bValueStore, this.objectStore);
	}

	/**
	 * Allocates an object on the store. If the address already has a value,
	 * performs a weak update.
	 * @param address The address to allocate, generated in {@code Trace}.
	 * @param object The object to place in the store.
	 * @return The Store after allocation.
	 */
	public Store alloc(Address address, Obj object) {
		Map<Address, Obj> objectStore = new HashMap<Address, Obj>(this.objectStore);
		Obj left = objectStore.get(address);
		if(left == null) this.objectStore.put(address, object);
		else this.objectStore.put(address,  left.join(object));
		return new Store(this.bValueStore, objectStore);
	}

}