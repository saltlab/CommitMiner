package ca.ubc.ece.salt.pangor.analysis.flow.factories;

import java.util.HashMap;
import java.util.Map;
import java.util.Stack;

import ca.ubc.ece.salt.pangor.analysis.flow.abstractdomain.Address;
import ca.ubc.ece.salt.pangor.analysis.flow.abstractdomain.BValue;
import ca.ubc.ece.salt.pangor.analysis.flow.abstractdomain.Closure;
import ca.ubc.ece.salt.pangor.analysis.flow.abstractdomain.Environment;
import ca.ubc.ece.salt.pangor.analysis.flow.abstractdomain.InternalFunctionProperties;
import ca.ubc.ece.salt.pangor.analysis.flow.abstractdomain.InternalObjectProperties;
import ca.ubc.ece.salt.pangor.analysis.flow.abstractdomain.JSClass;
import ca.ubc.ece.salt.pangor.analysis.flow.abstractdomain.NativeClosure;
import ca.ubc.ece.salt.pangor.analysis.flow.abstractdomain.Num;
import ca.ubc.ece.salt.pangor.analysis.flow.abstractdomain.Obj;
import ca.ubc.ece.salt.pangor.analysis.flow.abstractdomain.Scratchpad;
import ca.ubc.ece.salt.pangor.analysis.flow.abstractdomain.Scratchpad.Scratch;
import ca.ubc.ece.salt.pangor.analysis.flow.abstractdomain.State;
import ca.ubc.ece.salt.pangor.analysis.flow.abstractdomain.Store;
import ca.ubc.ece.salt.pangor.analysis.flow.abstractdomain.Str;
import ca.ubc.ece.salt.pangor.analysis.flow.trace.Trace;

public class FunctionFactory {

	public static final Obj Function_proto_Obj;
	static {
		Map<String, BValue> external = new HashMap<String, BValue>();
		external.put("external", Num.inject(Num.top()));
		external.put("apply", Address.inject(StoreFactory.Function_proto_apply_Addr));
		external.put("call", Address.inject(StoreFactory.Function_proto_call_Addr));
		external.put("toString", Address.inject(StoreFactory.Function_proto_toString_Addr));

		InternalObjectProperties internal = new InternalObjectProperties(
				Address.inject(StoreFactory.Function_proto_Addr),
				JSClass.CFunction_prototype_Obj);

		Function_proto_Obj = new Obj(external, internal, external.keySet());
	}

	// TODO: apply and call need native closures becuase their behaviour
	//		 affects the analysis.
	public static final Obj Function_proto_toString_Obj = FunctionFactory.constFunctionObj(Str.inject(Str.top()));
	public static final Obj Function_proto_apply_Obj = FunctionFactory.constFunctionObj(BValue.top());
	public static final Obj Function_proto_call_Obj = FunctionFactory.constFunctionObj(BValue.top());

	/**
	 * Approximate a function which is not modeled.
	 * @return A function which has no side effects that that returns the
	 * 		   BValue lattice element top.
	 */
	public static Obj constFunctionObj(BValue value) {

		Map<String, BValue> external = new HashMap<String, BValue>();

		Closure closure = new NativeClosure() {
				@Override
				public State run(BValue selfAddr, BValue argArrayAddr,
								 Store store, Scratchpad scratchpad,
								 Trace trace) {
					BValue retVal = value;
					Address address = Address.createBuiltinAddr();

					store = store.alloc(address, retVal);
					scratchpad.strongUpdate(Scratch.RETVAL, Address.inject(address));

					return new State(store, new Environment(), scratchpad, trace);
				}
			};

		Stack<Closure> closures = new Stack<Closure>();
		closures.push(closure);

		InternalObjectProperties internal = new InternalFunctionProperties(closures, JSClass.CFunction);

		return new Obj(external, internal, external.keySet());

	}

}
