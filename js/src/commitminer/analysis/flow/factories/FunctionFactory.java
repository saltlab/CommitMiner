package commitminer.analysis.flow.factories;

import java.util.HashMap;
import java.util.Map;
import java.util.Stack;

import org.mozilla.javascript.ast.AstNode;

import commitminer.analysis.flow.abstractdomain.Address;
import commitminer.analysis.flow.abstractdomain.BValue;
import commitminer.analysis.flow.abstractdomain.Change;
import commitminer.analysis.flow.abstractdomain.Closure;
import commitminer.analysis.flow.abstractdomain.Control;
import commitminer.analysis.flow.abstractdomain.DefinerIDs;
import commitminer.analysis.flow.abstractdomain.Environment;
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

public class FunctionFactory {

	public Store store;

	Map<AstNode, CFG> cfgs;

	public FunctionFactory(Store store, Map<AstNode, CFG> cfgs) {
		this.store = store;
		this.cfgs = cfgs;
	}

	public Obj Function_proto_Obj() {
		Map<String, Property> ext = new HashMap<String, Property>();
		store = Helpers.addProp("external", -41, Num.inject(Num.top(Change.u()), Change.u(), Change.u(), DefinerIDs.bottom()), ext, store);
		store = Helpers.addProp("apply", -42, Address.inject(StoreFactory.Function_proto_apply_Addr, Change.u(), Change.u(), Change.u(), DefinerIDs.bottom()), ext, store);
		store = Helpers.addProp("call", -43, Address.inject(StoreFactory.Function_proto_call_Addr, Change.u(), Change.u(), Change.u(), DefinerIDs.bottom()), ext, store);
		store = Helpers.addProp("toString", -44, Address.inject(StoreFactory.Function_proto_toString_Addr, Change.u(), Change.u(), Change.u(), DefinerIDs.bottom()), ext, store);

		InternalObjectProperties internal = new InternalObjectProperties(
				Address.inject(StoreFactory.Function_proto_Addr, Change.u(), Change.u(), Change.u(), DefinerIDs.bottom()),
				JSClass.CFunction_prototype_Obj);

		return new Obj(ext, internal);
	}

	// TODO: apply and call need native closures because their behaviour
	//		 affects the analysis.
	public Obj Function_proto_toString_Obj() { return constFunctionObj(Str.inject(Str.top(Change.u()), Change.u(), Change.u(), DefinerIDs.bottom())); }
	public Obj Function_proto_apply_Obj() { return constFunctionObj(BValue.top(Change.u(), Change.u(), Change.u())); }
	public Obj Function_proto_call_Obj() { return constFunctionObj(BValue.top(Change.u(), Change.u(), Change.u())); }

	/**
	 * Approximate a function which is not modeled.
	 * @return A function which has no side effects that that returns the
	 * 		   BValue lattice element top.
	 */
	public Obj constFunctionObj(BValue retVal) {

		Map<String, Property> external = new HashMap<String, Property>();

		Closure closure = new NativeClosure() {
				@Override
				public State run(Address selfAddr, 
								 Store store, Scratchpad scratchpad,
								 Trace trace, Control control,
								 Stack<Address> callStack) {
					scratchpad.strongUpdate(retVal, null);
					return new State(store, new Environment(), scratchpad,
									 trace, control, selfAddr, cfgs, callStack);
				}
			};

		Stack<Closure> closures = new Stack<Closure>();
		closures.push(closure);

		InternalObjectProperties internal = new InternalFunctionProperties(closures, JSClass.CFunction);

		return new Obj(external, internal);

	}

}
