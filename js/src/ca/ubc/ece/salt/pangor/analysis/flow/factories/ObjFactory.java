package ca.ubc.ece.salt.pangor.analysis.flow.factories;

import java.util.HashMap;
import java.util.Map;
import java.util.Stack;

import org.mozilla.javascript.ast.AstNode;

import ca.ubc.ece.salt.pangor.analysis.flow.abstractdomain.Address;
import ca.ubc.ece.salt.pangor.analysis.flow.abstractdomain.BValue;
import ca.ubc.ece.salt.pangor.analysis.flow.abstractdomain.Bool;
import ca.ubc.ece.salt.pangor.analysis.flow.abstractdomain.Closure;
import ca.ubc.ece.salt.pangor.analysis.flow.abstractdomain.InternalFunctionProperties;
import ca.ubc.ece.salt.pangor.analysis.flow.abstractdomain.InternalObjectProperties;
import ca.ubc.ece.salt.pangor.analysis.flow.abstractdomain.JSClass;
import ca.ubc.ece.salt.pangor.analysis.flow.abstractdomain.NativeClosure;
import ca.ubc.ece.salt.pangor.analysis.flow.abstractdomain.Num;
import ca.ubc.ece.salt.pangor.analysis.flow.abstractdomain.Obj;
import ca.ubc.ece.salt.pangor.analysis.flow.abstractdomain.Scratchpad;
import ca.ubc.ece.salt.pangor.analysis.flow.abstractdomain.State;
import ca.ubc.ece.salt.pangor.analysis.flow.abstractdomain.Store;
import ca.ubc.ece.salt.pangor.analysis.flow.abstractdomain.Str;
import ca.ubc.ece.salt.pangor.analysis.flow.trace.Trace;
import ca.ubc.ece.salt.pangor.cfg.CFG;

public class ObjFactory {

	public Store store;
	Map<AstNode, CFG> cfgs;
	FunctionFactory ff;

	public ObjFactory(Store store, Map<AstNode, CFG> cfgs) {
		this.store = store;
		this.cfgs = cfgs;
		ff = new FunctionFactory(store, cfgs);
	}

	public Obj Object_Obj() {
		Map<String, Address> ext = new HashMap<String, Address>();
		store = Helpers.addProp("prototype", Address.inject(StoreFactory.Object_proto_Addr), ext, store);
		store = Helpers.addProp("number", Num.inject(Num.top()), ext, store);
		store = Helpers.addProp("create", Address.inject(StoreFactory.Object_create_Addr), ext, store);
		store = Helpers.addProp("defineProperties", Address.inject(StoreFactory.Object_defineProperties_Addr), ext, store);
		store = Helpers.addProp("defineProperty", Address.inject(StoreFactory.Object_defineProperty_Addr), ext, store);
		store = Helpers.addProp("freeze", Address.inject(StoreFactory.Object_freeze_Addr), ext, store);
		store = Helpers.addProp("getOwnPropertyDescriptor", Address.inject(StoreFactory.Object_getOwnPropertyDescriptor_Addr), ext, store);
		store = Helpers.addProp("getOwnPropertyNames", Address.inject(StoreFactory.Object_getOwnPropertyNames_Addr), ext, store);
		store = Helpers.addProp("getPrototypeOf", Address.inject(StoreFactory.Object_getPrototypeOf_Addr), ext, store);
		store = Helpers.addProp("isExtensible", Address.inject(StoreFactory.Object_isExtensible_Addr), ext, store);
		store = Helpers.addProp("isFrozen", Address.inject(StoreFactory.Object_isFrozen_Addr), ext, store);
		store = Helpers.addProp("isSealed", Address.inject(StoreFactory.Object_isSealed_Addr), ext, store);
		store = Helpers.addProp("keys", Address.inject(StoreFactory.Object_keys_Addr), ext, store);
		store = Helpers.addProp("preventExtensions", Address.inject(StoreFactory.Object_preventExtensions_Addr), ext, store);
		store = Helpers.addProp("seal", Address.inject(StoreFactory.Object_seal_Addr), ext, store);

		NativeClosure closure = new NativeClosure() {
				@Override
				public State run(Address selfAddr, Address argArrayAddr,
								 Store store, Scratchpad scratchpad,
								 Trace trace) {
					// TODO: Update the state
					return null;
				}
			};

		Stack<Closure> closures = new Stack<Closure>();
		closures.push(closure);

		InternalObjectProperties internal = new InternalFunctionProperties(closures, JSClass.CObject_Obj);

		return new Obj(ext, internal, ext.keySet());
	}

	// TODO: We can be more precise with these.
	public Obj Object_create_Obj() { return ff.constFunctionObj(BValue.top()); }
	public Obj Object_defineProperties_Obj() { return ff.constFunctionObj(BValue.top()); }
	public Obj Object_defineProperty_Obj() { return ff.constFunctionObj(BValue.top()); }
	public Obj Object_freeze_Obj() { return ff.constFunctionObj(BValue.top()); }
	public Obj Object_getOwnPropertyDescriptor_Obj() { return ff.constFunctionObj(BValue.top()); }
	public Obj Object_getOwnPropertyNames_Obj() { return ff.constFunctionObj(BValue.top()); }
	public Obj Object_getPrototypeOf_Obj() { return ff.constFunctionObj(BValue.top()); }
	public Obj Object_isExtensible_Obj() { return ff.constFunctionObj(BValue.top()); }
	public Obj Object_isFrozen_Obj() { return ff.constFunctionObj(BValue.top()); }
	public Obj Object_isSealed_Obj() { return ff.constFunctionObj(BValue.top()); }
	public Obj Object_keys_Obj() { return ff.constFunctionObj(BValue.top()); }
	public Obj Object_preventExtensions_Obj() { return ff.constFunctionObj(BValue.top()); }
	public Obj Object_seal_Obj() { return ff.constFunctionObj(BValue.top()); }

	public Obj Object_proto_Obj() {
		Map<String, Address> ext = new HashMap<String, Address>();
		store = Helpers.addProp("toString", Address.inject(StoreFactory.Object_proto_toString_Addr), ext, store);
		store = Helpers.addProp("toLocaleString", Address.inject(StoreFactory.Object_proto_toLocaleString_Addr), ext, store);
		store = Helpers.addProp("valueOf", Address.inject(StoreFactory.Object_proto_valueOf_Addr), ext, store);
		store = Helpers.addProp("hasOwnPrpoerty", Address.inject(StoreFactory.Object_proto_hasOwnProperty_Addr), ext, store);
		store = Helpers.addProp("isPrototypeOf", Address.inject(StoreFactory.Object_proto_isPrototypeOf_Addr), ext, store);
		store = Helpers.addProp("propertyIsEnumerable", Address.inject(StoreFactory.Object_proto_propertyIsEnumerable_Addr), ext, store);

		InternalObjectProperties internal = new InternalObjectProperties();

		return new Obj(ext, internal, ext.keySet());
	}

	public Obj Object_proto_toString_Obj() { return ff.constFunctionObj(Str.inject(Str.top())); }
	public Obj Object_proto_toLocaleString_Obj() { return ff.constFunctionObj(Str.inject(Str.top())); }
	public Obj Object_proto_hasOwnProperty_Obj() { return ff.constFunctionObj(Bool.inject(Bool.top())); }
	public Obj Object_proto_isPrototypeOf_Obj() { return ff.constFunctionObj(Bool.inject(Bool.top())); }
	public Obj Object_proto_propertyIsEnumerable_Obj() { return ff.constFunctionObj(Bool.inject(Bool.top())); }
	public Obj Object_proto_valueOf_Obj() { return ff.constFunctionObj(BValue.primitive()); }


}