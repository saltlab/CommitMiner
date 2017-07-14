package commitminer.analysis.flow.factories;

import java.util.HashMap;
import java.util.Map;

import org.mozilla.javascript.ast.AstNode;

import commitminer.analysis.flow.abstractdomain.Address;
import commitminer.analysis.flow.abstractdomain.BValue;
import commitminer.analysis.flow.abstractdomain.Change;
import commitminer.analysis.flow.abstractdomain.DefinerIDs;
import commitminer.analysis.flow.abstractdomain.Obj;
import commitminer.analysis.flow.abstractdomain.Store;
import commitminer.cfg.CFG;

/**
 * Initializes the store with builtins.
 *
 * TODO: Add the rest of the builtins (ie., Array, Number, String, etc.)
 */
public class StoreFactory {

	/* Global. */
	public static final Address global_binding_Addr = Address.createBuiltinAddr();
	public static final Address global_Addr = Address.createBuiltinAddr();

	/* Arguments. */
	public static final Address Arguments_Addr = Address.createBuiltinAddr();

	/* Object abstract addresses. */
	public static final Address Object_binding_Addr = Address.createBuiltinAddr();
	public static final Address Object_Addr = Address.createBuiltinAddr();
	public static final Address Object_create_Addr = Address.createBuiltinAddr();
	public static final Address Object_defineProperties_Addr = Address.createBuiltinAddr();
	public static final Address Object_defineProperty_Addr = Address.createBuiltinAddr();
	public static final Address Object_freeze_Addr = Address.createBuiltinAddr();
	public static final Address Object_getOwnPropertyDescriptor_Addr = Address.createBuiltinAddr();
	public static final Address Object_getOwnPropertyNames_Addr = Address.createBuiltinAddr();
	public static final Address Object_getPrototypeOf_Addr = Address.createBuiltinAddr();
	public static final Address Object_isExtensible_Addr = Address.createBuiltinAddr();
	public static final Address Object_isFrozen_Addr = Address.createBuiltinAddr();
	public static final Address Object_isSealed_Addr = Address.createBuiltinAddr();
	public static final Address Object_keys_Addr = Address.createBuiltinAddr();
	public static final Address Object_preventExtensions_Addr = Address.createBuiltinAddr();
	public static final Address Object_seal_Addr = Address.createBuiltinAddr();

	/* Object.prototype abstract addresses. */
	public static final Address Object_proto_Addr = Address.createBuiltinAddr();
	public static final Address Object_proto_valueOf_Addr = Address.createBuiltinAddr();
	public static final Address Object_proto_toString_Addr = Address.createBuiltinAddr();
	public static final Address Object_proto_isPrototypeOf_Addr = Address.createBuiltinAddr();
	public static final Address Object_proto_propertyIsEnumerable_Addr = Address.createBuiltinAddr();
	public static final Address Object_proto_hasOwnProperty_Addr = Address.createBuiltinAddr();
	public static final Address Object_proto_toLocaleString_Addr = Address.createBuiltinAddr();

	/* Function abstract addresses. */
	public static final Address Function_Addr = Address.createBuiltinAddr();

	/* Function.prototype abstract addresses. */
	public static final Address Function_proto_Addr = Address.createBuiltinAddr();
	public static final Address Function_proto_toString_Addr = Address.createBuiltinAddr();
	public static final Address Function_proto_apply_Addr = Address.createBuiltinAddr();
	public static final Address Function_proto_call_Addr = Address.createBuiltinAddr();

	/* Needed for internal functions. */
	public static final Address Dummy_Arguments_Addr = Address.createBuiltinAddr();

	/* Used to pass the arguments object. */
	public static final Address Dummy_Addr = Address.createBuiltinAddr();

	/**
	 * @return An initial store with builtin objects.
	 */
	public static Store createInitialStore(Map<AstNode, CFG> cfgs) {

		Map<Address, BValue> bValueStore = new HashMap<Address, BValue>();
		Map<Address, Obj> objectStore = new HashMap<Address, Obj>();

		Store store = new Store(bValueStore, objectStore);

		bValueStore.put(global_binding_Addr, Address.inject(global_Addr, Change.u(), Change.u(), Change.u(), DefinerIDs.bottom()));

		GlobalFactory gf = new GlobalFactory(store);
		objectStore.put(global_Addr, gf.Global_Obj());
		store = gf.store;

		ArgumentsFactory af = new ArgumentsFactory(store, cfgs);
		objectStore.put(Arguments_Addr, af.Arguments_Obj());
		store = af.store;

		ObjFactory of = new ObjFactory(store, cfgs);
		objectStore.put(Object_Addr, of.Object_Obj());
		objectStore.put(Object_create_Addr, of.Object_create_Obj());
		objectStore.put(Object_defineProperties_Addr, of.Object_defineProperties_Obj());
		objectStore.put(Object_defineProperty_Addr, of.Object_defineProperty_Obj());
		objectStore.put(Object_freeze_Addr, of.Object_freeze_Obj());
		objectStore.put(Object_getOwnPropertyDescriptor_Addr, of.Object_getOwnPropertyDescriptor_Obj());
		objectStore.put(Object_getOwnPropertyNames_Addr, of.Object_getOwnPropertyNames_Obj());
		objectStore.put(Object_getPrototypeOf_Addr, of.Object_getPrototypeOf_Obj());
		objectStore.put(Object_isExtensible_Addr, of.Object_isExtensible_Obj());
		objectStore.put(Object_isFrozen_Addr, of.Object_isFrozen_Obj());
		objectStore.put(Object_isSealed_Addr, of.Object_isSealed_Obj());
		objectStore.put(Object_keys_Addr, of.Object_keys_Obj());
		objectStore.put(Object_preventExtensions_Addr, of.Object_preventExtensions_Obj());
		objectStore.put(Object_seal_Addr, of.Object_seal_Obj());
		objectStore.put(Object_proto_Addr, of.Object_proto_Obj());
		objectStore.put(Object_proto_toString_Addr, of.Object_proto_toString_Obj());
		objectStore.put(Object_proto_toLocaleString_Addr, of.Object_proto_toLocaleString_Obj());
		objectStore.put(Object_proto_hasOwnProperty_Addr, of.Object_proto_hasOwnProperty_Obj());
		objectStore.put(Object_proto_isPrototypeOf_Addr, of.Object_proto_isPrototypeOf_Obj());
		objectStore.put(Object_proto_propertyIsEnumerable_Addr, of.Object_proto_propertyIsEnumerable_Obj());
		objectStore.put(Object_proto_valueOf_Addr, of.Object_proto_valueOf_Obj());
		store = of.store;

		FunctionFactory ff = new FunctionFactory(store, cfgs);
		objectStore.put(Function_proto_Addr, ff.Function_proto_Obj());
		objectStore.put(Function_proto_toString_Addr, ff.Function_proto_toString_Obj());
		objectStore.put(Function_proto_apply_Addr, ff.Function_proto_apply_Obj());
		objectStore.put(Function_proto_call_Addr, ff.Function_proto_call_Obj());
		store = ff.store;

//		objectStore.put(Dummy_Arguments_Addr, of.Dummy_Arguments_Obj); TODO
//		objectStore.put(Dummy_Addr, of.Dummy_Obj); TODO

		return store;
	}

}
