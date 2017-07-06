package commitminer.analysis.flow.factories;

import java.util.HashMap;
import java.util.Map;
import java.util.Stack;

import org.mozilla.javascript.ast.AstNode;

import commitminer.analysis.flow.abstractdomain.Address;
import commitminer.analysis.flow.abstractdomain.BValue;
import commitminer.analysis.flow.abstractdomain.Bool;
import commitminer.analysis.flow.abstractdomain.Change;
import commitminer.analysis.flow.abstractdomain.Closure;
import commitminer.analysis.flow.abstractdomain.Control;
import commitminer.analysis.flow.abstractdomain.DefinerIDs;
import commitminer.analysis.flow.abstractdomain.Property;
import commitminer.analysis.flow.abstractdomain.InternalFunctionProperties;
import commitminer.analysis.flow.abstractdomain.InternalObjectProperties;
import commitminer.analysis.flow.abstractdomain.JSClass;
import commitminer.analysis.flow.abstractdomain.NativeClosure;
import commitminer.analysis.flow.abstractdomain.Num;
import commitminer.analysis.flow.abstractdomain.Obj;
import commitminer.analysis.flow.abstractdomain.Scratchpad;
import commitminer.analysis.flow.abstractdomain.State;
import commitminer.analysis.flow.abstractdomain.Store;
import commitminer.analysis.flow.abstractdomain.Str;
import commitminer.analysis.flow.trace.Trace;
import commitminer.cfg.CFG;

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
		Map<String, Property> ext = new HashMap<String, Property>();
		store = Helpers.addProp("prototype", -21, Address.inject(StoreFactory.Object_proto_Addr, Change.u(), Change.u(), DefinerIDs.bottom()), ext, store);
		store = Helpers.addProp("number", -22, Num.inject(Num.top(Change.u()), Change.u(), DefinerIDs.bottom()), ext, store);
		store = Helpers.addProp("create", -23, Address.inject(StoreFactory.Object_create_Addr, Change.u(), Change.u(), DefinerIDs.bottom()), ext, store);
		store = Helpers.addProp("defineProperties", -24, Address.inject(StoreFactory.Object_defineProperties_Addr, Change.u(), Change.u(), DefinerIDs.bottom()), ext, store);
		store = Helpers.addProp("defineProperty", -25, Address.inject(StoreFactory.Object_defineProperty_Addr, Change.u(), Change.u(), DefinerIDs.bottom()), ext, store);
		store = Helpers.addProp("freeze", -26, Address.inject(StoreFactory.Object_freeze_Addr, Change.u(), Change.u(), DefinerIDs.bottom()), ext, store);
		store = Helpers.addProp("getOwnPropertyDescriptor", -27, Address.inject(StoreFactory.Object_getOwnPropertyDescriptor_Addr, Change.u(), Change.u(), DefinerIDs.bottom()), ext, store);
		store = Helpers.addProp("getOwnPropertyNames", -28, Address.inject(StoreFactory.Object_getOwnPropertyNames_Addr, Change.u(), Change.u(), DefinerIDs.bottom()), ext, store);
		store = Helpers.addProp("getPrototypeOf", -29, Address.inject(StoreFactory.Object_getPrototypeOf_Addr, Change.u(), Change.u(), DefinerIDs.bottom()), ext, store);
		store = Helpers.addProp("isExtensible", -30, Address.inject(StoreFactory.Object_isExtensible_Addr, Change.u(), Change.u(), DefinerIDs.bottom()), ext, store);
		store = Helpers.addProp("isFrozen", -31, Address.inject(StoreFactory.Object_isFrozen_Addr, Change.u(), Change.u(), DefinerIDs.bottom()), ext, store);
		store = Helpers.addProp("isSealed", -32, Address.inject(StoreFactory.Object_isSealed_Addr, Change.u(), Change.u(), DefinerIDs.bottom()), ext, store);
		store = Helpers.addProp("keys", -33, Address.inject(StoreFactory.Object_keys_Addr, Change.u(), Change.u(), DefinerIDs.bottom()), ext, store);
		store = Helpers.addProp("preventExtensions", -34, Address.inject(StoreFactory.Object_preventExtensions_Addr, Change.u(), Change.u(), DefinerIDs.bottom()), ext, store);
		store = Helpers.addProp("seal", -35, Address.inject(StoreFactory.Object_seal_Addr, Change.u(), Change.u(), DefinerIDs.bottom()), ext, store);

		NativeClosure closure = new NativeClosure() {
				@Override
				public State run(Address selfAddr, 
								 Store store, Scratchpad scratchpad,
								 Trace trace, Control control,
								 Stack<Address> callStack) {
					// TODO: Update the state
					return null;
				}
			};

		Stack<Closure> closures = new Stack<Closure>();
		closures.push(closure);

		InternalObjectProperties internal = new InternalFunctionProperties(closures, JSClass.CObject_Obj);

		return new Obj(ext, internal);
	}

	// TODO: We can be more precise with these.
	public Obj Object_create_Obj() { return ff.constFunctionObj(BValue.top(Change.u(), Change.u())); }
	public Obj Object_defineProperties_Obj() { return ff.constFunctionObj(BValue.top(Change.u(), Change.u())); }
	public Obj Object_defineProperty_Obj() { return ff.constFunctionObj(BValue.top(Change.u(), Change.u())); }
	public Obj Object_freeze_Obj() { return ff.constFunctionObj(BValue.top(Change.u(), Change.u())); }
	public Obj Object_getOwnPropertyDescriptor_Obj() { return ff.constFunctionObj(BValue.top(Change.u(), Change.u())); }
	public Obj Object_getOwnPropertyNames_Obj() { return ff.constFunctionObj(BValue.top(Change.u(), Change.u())); }
	public Obj Object_getPrototypeOf_Obj() { return ff.constFunctionObj(BValue.top(Change.u(), Change.u())); }
	public Obj Object_isExtensible_Obj() { return ff.constFunctionObj(BValue.top(Change.u(), Change.u())); }
	public Obj Object_isFrozen_Obj() { return ff.constFunctionObj(BValue.top(Change.u(), Change.u())); }
	public Obj Object_isSealed_Obj() { return ff.constFunctionObj(BValue.top(Change.u(), Change.u())); }
	public Obj Object_keys_Obj() { return ff.constFunctionObj(BValue.top(Change.u(), Change.u())); }
	public Obj Object_preventExtensions_Obj() { return ff.constFunctionObj(BValue.top(Change.u(), Change.u())); }
	public Obj Object_seal_Obj() { return ff.constFunctionObj(BValue.top(Change.u(), Change.u())); }

	public Obj Object_proto_Obj() {
		Map<String, Property> ext = new HashMap<String, Property>();
		store = Helpers.addProp("toString", -11, Address.inject(StoreFactory.Object_proto_toString_Addr, Change.u(), Change.u(), DefinerIDs.bottom()), ext, store);
		store = Helpers.addProp("toLocaleString", -12, Address.inject(StoreFactory.Object_proto_toLocaleString_Addr, Change.u(), Change.u(), DefinerIDs.bottom()), ext, store);
		store = Helpers.addProp("valueOf", -13, Address.inject(StoreFactory.Object_proto_valueOf_Addr, Change.u(), Change.u(), DefinerIDs.bottom()), ext, store);
		store = Helpers.addProp("hasOwnPrpoerty", -14, Address.inject(StoreFactory.Object_proto_hasOwnProperty_Addr, Change.u(), Change.u(), DefinerIDs.bottom()), ext, store);
		store = Helpers.addProp("isPrototypeOf", -15, Address.inject(StoreFactory.Object_proto_isPrototypeOf_Addr, Change.u(), Change.u(), DefinerIDs.bottom()), ext, store);
		store = Helpers.addProp("propertyIsEnumerable", -16, Address.inject(StoreFactory.Object_proto_propertyIsEnumerable_Addr, Change.u(), Change.u(), DefinerIDs.bottom()), ext, store);

		InternalObjectProperties internal = new InternalObjectProperties();

		return new Obj(ext, internal);
	}

	public Obj Object_proto_toString_Obj() { return ff.constFunctionObj(Str.inject(Str.top(Change.u()), Change.u(), DefinerIDs.bottom())); }
	public Obj Object_proto_toLocaleString_Obj() { return ff.constFunctionObj(Str.inject(Str.top(Change.u()), Change.u(), DefinerIDs.bottom())); }
	public Obj Object_proto_hasOwnProperty_Obj() { return ff.constFunctionObj(Bool.inject(Bool.top(Change.u()), Change.u(), DefinerIDs.bottom())); }
	public Obj Object_proto_isPrototypeOf_Obj() { return ff.constFunctionObj(Bool.inject(Bool.top(Change.u()), Change.u(), DefinerIDs.bottom())); }
	public Obj Object_proto_propertyIsEnumerable_Obj() { return ff.constFunctionObj(Bool.inject(Bool.top(Change.u()), Change.u(), DefinerIDs.bottom())); }
	public Obj Object_proto_valueOf_Obj() { return ff.constFunctionObj(BValue.primitive(Change.u(), Change.u())); }


}