package ca.ubc.ece.salt.pangor.analysis.flow.builtins;

import java.util.HashMap;
import java.util.Map;

import ca.ubc.ece.salt.pangor.analysis.flow.abstractdomain.Address;
import ca.ubc.ece.salt.pangor.analysis.flow.abstractdomain.BValue;
import ca.ubc.ece.salt.pangor.analysis.flow.abstractdomain.Obj;
import ca.ubc.ece.salt.pangor.analysis.flow.abstractdomain.Store;

/**
 * Initializes the store with builtins.
 * Similar to init/init.scala in JSAI
 *
 * TODO: Add the rest of the builtins (ie., Array, Number, String, etc.)
 */
public class StoreFactory {

	public static long uniqueAddress;

	/* Object abstract addresses. */
	public static final Address Object_Addr = newAbstractAddr();
	public static final Address Object_create_Addr = newAbstractAddr();
	public static final Address Object_defineProperties_Addr = newAbstractAddr();
	public static final Address Object_defineProperty_Addr = newAbstractAddr();
	public static final Address Object_freeze_Addr = newAbstractAddr();
	public static final Address Object_getOwnPropertyDescriptor_Addr = newAbstractAddr();
	public static final Address Object_getOwnPropertyNames_Addr = newAbstractAddr();
	public static final Address Object_getPrototypeOf_Addr = newAbstractAddr();
	public static final Address Object_isExtensible_Addr = newAbstractAddr();
	public static final Address Object_isFrozen_Addr = newAbstractAddr();
	public static final Address Object_isSealed_Addr = newAbstractAddr();
	public static final Address Object_keys_Addr = newAbstractAddr();
	public static final Address Object_preventExtensions_Addr = newAbstractAddr();
	public static final Address Object_seal_Addr = newAbstractAddr();

	/* Object.prototype abstract addresses. */
	public static final Address Object_proto_Addr = newAbstractAddr();
	public static final Address Object_proto_valueOf_Addr = newAbstractAddr();
	public static final Address Object_proto_toString_Addr = newAbstractAddr();
	public static final Address Object_proto_isPrototypeOf_Addr = newAbstractAddr();
	public static final Address Object_proto_propertyIsEnumerable_Addr = newAbstractAddr();
	public static final Address Object_proto_hasOwnProperty_Addr = newAbstractAddr();
	public static final Address Object_proto_toLocaleString_Addr = newAbstractAddr();

	/* Function abstract addresses. */
	public static final Address Function_Addr = newAbstractAddr();

	/* Function.prototype abstract addresses. */
	public static final Address Function_proto_Addr = newAbstractAddr();
	public static final Address Function_proto_toString_Addr = newAbstractAddr();
	public static final Address Function_proto_apply_Addr = newAbstractAddr();
	public static final Address Function_proto_call_Addr = newAbstractAddr();

	/* Needed for internal functions. */
	public static final Address Dummy_Arguments_Addr = newAbstractAddr();

	/**
	 * @return An initial store with builtin objects.
	 */
	public static Store createInitialStore() {
		Map<Address, BValue> bValueStore = new HashMap<Address, BValue>();
		Map<Address, Obj> objectStore = new HashMap<Address, Obj>();

		objectStore.put(Object_Addr, ObjFactory.Object_Obj);
	}

	/**
	 * Initial abstract addresses get negative values Same as in JSAI.
	 */
	private static Address newAbstractAddr() {
		Address address = new Address(uniqueAddress);
		uniqueAddress--;
		return address;
	}
}
