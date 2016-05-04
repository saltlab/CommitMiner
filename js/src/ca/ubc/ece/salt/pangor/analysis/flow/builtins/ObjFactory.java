package ca.ubc.ece.salt.pangor.analysis.flow.builtins;

import java.util.HashMap;
import java.util.Map;
import java.util.Stack;

import ca.ubc.ece.salt.pangor.analysis.flow.abstractdomain.Address;
import ca.ubc.ece.salt.pangor.analysis.flow.abstractdomain.BValue;
import ca.ubc.ece.salt.pangor.analysis.flow.abstractdomain.Closure;
import ca.ubc.ece.salt.pangor.analysis.flow.abstractdomain.Environment;
import ca.ubc.ece.salt.pangor.analysis.flow.abstractdomain.InternalFunctionProperties;
import ca.ubc.ece.salt.pangor.analysis.flow.abstractdomain.NativeClosure;
import ca.ubc.ece.salt.pangor.analysis.flow.abstractdomain.Num;
import ca.ubc.ece.salt.pangor.analysis.flow.abstractdomain.Obj;
import ca.ubc.ece.salt.pangor.analysis.flow.abstractdomain.Obj.JSClass;
import ca.ubc.ece.salt.pangor.analysis.flow.abstractdomain.Scratchpad;
import ca.ubc.ece.salt.pangor.analysis.flow.abstractdomain.State;
import ca.ubc.ece.salt.pangor.analysis.flow.abstractdomain.Store;


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

//		InternalObjectProperties internal = null;

		Object_Obj = createInitFunctionObj(
			new NativeClosure() {
				@Override
				public State run(BValue selfAddr, BValue argArrayAddr, String x,
								 Environment environment, Store store,
								 Scratchpad scratchpad) {
					return null;
				}
			},
			external,
//			internal,
			JSClass.CObject_Obj
			);
	}

	/**
	 * Instantiates builtin objects.
	 * @param clo The native closure.
	 * @param external The external properties.
	 * @param jsclass The object type.
	 * @return An instance of the new object.
	 */
	public static Obj createInitFunctionObj(NativeClosure clo,
											Map<String, BValue> external,
//											InternalObjectProperties internal,
											JSClass jsclass) {

		Stack<Closure> closures = new Stack<Closure>();
		closures.add(clo);

		InternalFunctionProperties internal = new InternalFunctionProperties(
				Address.inject(StoreFactory.Function_proto_Addr),
				closures,
				jsclass);

		// JSAI separates numeric and string fields through the string domain.
//		createInitObjectCore(external, internal);

		return new Obj(external, internal, external.keySet());
	}

}