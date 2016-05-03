package ca.ubc.ece.salt.pangor.analysis.flow.abstractdomain;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.Stack;

import ca.ubc.ece.salt.pangor.analysis.flow.abstractdomain.Obj.Klass;
import ca.ubc.ece.salt.pangor.cfg.CFG;

/**
 * The abstract domain for the program's store (memory).
 * i.e. Store# := Address# -> P(BValue# + Object#)
 */
public class Store {

	/** The store for {@code BValue}s. **/
	private Map<Address, BValue> bValueStore;

	/** The store for {@code Object}s. **/
	private Map<Address, Obj> objectStore;

	/** The addresses of the builtin objects. **/
	public Map<Klass, Address> builtins;

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

		/* Initialize Object. */
		Map<String, BValue> externalProperties = new HashMap<String, BValue>();
		InternalObjectProperties internalProperties = new InternalObjectProperties(null, Klass.OBJECT);
		Set<String> definitelyPresent = new HashSet<String>();
		Obj object = new Obj(externalProperties, internalProperties, definitelyPresent);
		Address address = new Address();
		this.objectStore.put(address, object);

		/* Initialize the builtins. */
		this.builtins = new HashMap<Klass, Address>();
		this.builtins.put(Klass.OBJECT, address);
	}

	private Store(Map<Address, BValue> bValueStore,
				  Map<Address, Obj> objectStore,
				  Map<Klass, Address> builtins) {
		this.bValueStore = bValueStore;
		this.objectStore = objectStore;
		this.builtins = builtins;
	}

	/**
	 * @return a new Store which is this Store joined with the store parameter.
	 */
	public Store join(Store state) {

		/* Just copy the values into a new hash map. The values are essentially
		 * immutable since any transfer or join will produce a new value. */
		Map<Address, BValue> bValueStore = new HashMap<Address, BValue>(this.bValueStore);
		Map<Address, Obj> objectStore = new HashMap<Address, Obj>(this.objectStore);

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

		return new Store(bValueStore, objectStore, this.builtins);

	}

	/**
	 * Allocates space in the store for a {@code BValue}.
	 * @param value The value to place in the store.
	 * @return The address of the value in the store.
	 */
	public Address alloc(BValue value) {
		Address address = new Address();
		this.bValueStore.put(address, value);
		return address;
	}

	/**
	 * Allocates space in the store for a {@code Closure} (function).
	 * @param protoAddress The address of the function prototype.
	 * @param value The address of the function prototype?
	 * @return The address of the function in the store.
	 */
	public Address allocFun(CFG cfg, Stack<Environment> closures) {

		/* Create the function object. */
		Address protoAddress = this.builtins.get(Klass.OBJECT);
		Map<String, BValue> externalProperties = new HashMap<String, BValue>();
		InternalFunctionProperties internalProperties = new InternalFunctionProperties(protoAddress, cfg, closures);
		Set<String> definitelyPresentProperties = new HashSet<String>();
		Obj function = new Obj(externalProperties, internalProperties, definitelyPresentProperties);

		/* Allocate the function to the store. */
		Address address = new Address();
		this.objectStore.put(address, function);

		return address;

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