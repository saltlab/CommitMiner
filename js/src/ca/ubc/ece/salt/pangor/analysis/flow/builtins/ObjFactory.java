package ca.ubc.ece.salt.pangor.analysis.flow.builtins;

import java.util.HashMap;
import java.util.Map;
import java.util.Stack;

import ca.ubc.ece.salt.pangor.analysis.flow.abstractdomain.Address;
import ca.ubc.ece.salt.pangor.analysis.flow.abstractdomain.Addresses;
import ca.ubc.ece.salt.pangor.analysis.flow.abstractdomain.BValue;
import ca.ubc.ece.salt.pangor.analysis.flow.abstractdomain.Bool;
import ca.ubc.ece.salt.pangor.analysis.flow.abstractdomain.Closure;
import ca.ubc.ece.salt.pangor.analysis.flow.abstractdomain.Environment;
import ca.ubc.ece.salt.pangor.analysis.flow.abstractdomain.InternalFunctionProperties;
import ca.ubc.ece.salt.pangor.analysis.flow.abstractdomain.InternalObjectProperties;
import ca.ubc.ece.salt.pangor.analysis.flow.abstractdomain.NativeClosure;
import ca.ubc.ece.salt.pangor.analysis.flow.abstractdomain.Num;
import ca.ubc.ece.salt.pangor.analysis.flow.abstractdomain.Obj;
import ca.ubc.ece.salt.pangor.analysis.flow.abstractdomain.Obj.JSClass;
import ca.ubc.ece.salt.pangor.analysis.flow.abstractdomain.Scratchpad;
import ca.ubc.ece.salt.pangor.analysis.flow.abstractdomain.State;
import ca.ubc.ece.salt.pangor.analysis.flow.abstractdomain.Store;
import ca.ubc.ece.salt.pangor.analysis.flow.abstractdomain.Str;


public class ObjFactory {

	public static final Obj Object_Obj;
	static {
		Map<String, BValue> external = new HashMap<String, BValue>();
		external.put("prototype", Address.inject(StoreFactory.Object_proto_Addr));
		external.put("number", Num.inject(Num.top()));
		external.put("create", Address.inject(StoreFactory.Object_create_Addr));
		external.put("defineProperties", Address.inject(StoreFactory.Object_defineProperties_Addr));
		external.put("defineProperty", Address.inject(StoreFactory.Object_defineProperty_Addr));
		external.put("freeze", Address.inject(StoreFactory.Object_freeze_Addr));
		external.put("getOwnPropertyDescriptor", Address.inject(StoreFactory.Object_getOwnPropertyDescriptor_Addr));
		external.put("getOwnPropertyNames", Address.inject(StoreFactory.Object_getOwnPropertyNames_Addr));
		external.put("getPrototypeOf", Address.inject(StoreFactory.Object_getPrototypeOf_Addr));
		external.put("isExtensible", Address.inject(StoreFactory.Object_isExtensible_Addr));
		external.put("isFrozen", Address.inject(StoreFactory.Object_isFrozen_Addr));
		external.put("isSealed", Address.inject(StoreFactory.Object_isSealed_Addr));
		external.put("keys", Address.inject(StoreFactory.Object_keys_Addr));
		external.put("preventExtensions", Address.inject(StoreFactory.Object_preventExtensions_Addr));
		external.put("seal", Address.inject(StoreFactory.Object_seal_Addr));

		NativeClosure closure = new NativeClosure() {
				@Override
				public State run(BValue selfAddr, BValue argArrayAddr, String x,
								 Environment environment, Store store,
								 Scratchpad scratchpad) {
					// TODO: Update the state
					return null;
				}
			};

		Stack<Closure> closures = new Stack<Closure>();
		closures.push(closure);

		InternalObjectProperties internal = new InternalFunctionProperties(closures, JSClass.CObject_Obj);

		Object_Obj = new Obj(external, internal, external.keySet());
	}

	// TODO: We can be more precise with these.
	public static final Obj Object_create_Obj = constFunctionObj(BValue.top());
	public static final Obj Object_defineProperties_Obj = constFunctionObj(BValue.top());
	public static final Obj Object_defineProperty_Obj = constFunctionObj(BValue.top());
	public static final Obj Object_freeze_Obj = constFunctionObj(BValue.top());
	public static final Obj Object_getOwnPropertyDescriptor_Obj = constFunctionObj(BValue.top());
	public static final Obj Object_getOwnPropertyNames_Obj = constFunctionObj(BValue.top());
	public static final Obj Object_getPrototypeOf_Obj = constFunctionObj(BValue.top());
	public static final Obj Object_isExtensible_Obj = constFunctionObj(BValue.top());
	public static final Obj Object_isFrozen_Obj = constFunctionObj(BValue.top());
	public static final Obj Object_isSealed_Obj = constFunctionObj(BValue.top());
	public static final Obj Object_keys_Obj = constFunctionObj(BValue.top());
	public static final Obj Object_preventExtensions_Obj = constFunctionObj(BValue.top());
	public static final Obj Object_seal_Obj = constFunctionObj(BValue.top());

	public static final Obj Object_proto_Obj;
	static {
		Map<String, BValue> external = new HashMap<String, BValue>();
		external.put("constructor", Address.inject(StoreFactory.Object_Addr));
		external.put("toString", Address.inject(StoreFactory.Object_proto_toString_Addr));
		external.put("toLocaleString", Address.inject(StoreFactory.Object_proto_toLocaleString_Addr));
		external.put("valueOf", Address.inject(StoreFactory.Object_proto_valueOf_Addr));
		external.put("hasOwnPrpoerty", Address.inject(StoreFactory.Object_proto_hasOwnProperty_Addr));
		external.put("isPrototypeOf", Address.inject(StoreFactory.Object_proto_isPrototypeOf_Addr));
		external.put("propertyIsEnumerable", Address.inject(StoreFactory.Object_proto_propertyIsEnumerable_Addr));

		InternalObjectProperties internal = new InternalObjectProperties();

		Object_proto_Obj = new Obj(external, internal, external.keySet());
	}

	public static final Obj Object_proto_toString_Obj = constFunctionObj(Str.inject(Str.top()));
	public static final Obj Object_proto_toLocaleString_Obj = constFunctionObj(Str.inject(Str.top()));
	public static final Obj Object_proto_hasOwnProperty_Obj = constFunctionObj(Bool.inject(Bool.top()));
	public static final Obj Object_proto_isPrototypeOf_Obj = constFunctionObj(Bool.inject(Bool.top()));
	public static final Obj Object_proto_propertyIsEnumerable_Obj = constFunctionObj(Bool.inject(Bool.top()));
	public static final Obj Object_proto_valueOf_Obj = constFunctionObj(BValue.primitive());

	/**
	 * Approximate a function which is not modeled.
	 * @return A function which has no side effects that that returns the
	 * 		   BValue lattice element top.
	 */
	private static Obj constFunctionObj(BValue value) {

		Map<String, BValue> external = new HashMap<String, BValue>();

		Closure closure = new NativeClosure() {
				@Override
				public State run(BValue selfAddr, BValue argArrayAddr, String x,
								 Environment environment, Store store,
								 Scratchpad scratchpad) {
					BValue retVal = value;
					Address address = new Address();

					store = store.alloc(address, retVal);
					environment = environment.strongUpdate(x, new Addresses(address));

					return new State(store, environment);
				}
			};

		Stack<Closure> closures = new Stack<Closure>();
		closures.push(closure);

		InternalObjectProperties internal = new InternalFunctionProperties(closures, JSClass.CFunction);

		return new Obj(external, internal, external.keySet());

	}



}