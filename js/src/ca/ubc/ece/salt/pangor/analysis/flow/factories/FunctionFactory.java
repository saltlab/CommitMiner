package ca.ubc.ece.salt.pangor.analysis.flow.factories;

import java.util.HashMap;
import java.util.Map;
import java.util.Stack;

import org.mozilla.javascript.ast.AstNode;

import ca.ubc.ece.salt.pangor.analysis.flow.abstractdomain.Address;
import ca.ubc.ece.salt.pangor.analysis.flow.abstractdomain.BValue;
import ca.ubc.ece.salt.pangor.analysis.flow.abstractdomain.Change;
import ca.ubc.ece.salt.pangor.analysis.flow.abstractdomain.Closure;
import ca.ubc.ece.salt.pangor.analysis.flow.abstractdomain.Control;
import ca.ubc.ece.salt.pangor.analysis.flow.abstractdomain.Environment;
import ca.ubc.ece.salt.pangor.analysis.flow.abstractdomain.Identifier;
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
import ca.ubc.ece.salt.pangor.cfg.CFG;

public class FunctionFactory {

	public Store store;

	Map<AstNode, CFG> cfgs;

	public FunctionFactory(Store store, Map<AstNode, CFG> cfgs) {
		this.store = store;
		this.cfgs = cfgs;
	}

	public Obj Function_proto_Obj() {
		Map<Identifier, Address> ext = new HashMap<Identifier, Address>();
		store = Helpers.addProp("external", Num.inject(Num.top(Change.u()), Change.u()), ext, store);
		store = Helpers.addProp("apply", Address.inject(StoreFactory.Function_proto_apply_Addr, Change.u(), Change.u()), ext, store);
		store = Helpers.addProp("call", Address.inject(StoreFactory.Function_proto_call_Addr, Change.u(), Change.u()), ext, store);
		store = Helpers.addProp("toString", Address.inject(StoreFactory.Function_proto_toString_Addr, Change.u(), Change.u()), ext, store);

		InternalObjectProperties internal = new InternalObjectProperties(
				Address.inject(StoreFactory.Function_proto_Addr, Change.u(), Change.u()),
				JSClass.CFunction_prototype_Obj);

		return new Obj(ext, internal);
	}

	// TODO: apply and call need native closures because their behaviour
	//		 affects the analysis.
	public Obj Function_proto_toString_Obj() { return constFunctionObj(Str.inject(Str.top(Change.u()), Change.u())); }
	public Obj Function_proto_apply_Obj() { return constFunctionObj(BValue.top(Change.u(), Change.u())); }
	public Obj Function_proto_call_Obj() { return constFunctionObj(BValue.top(Change.u(), Change.u())); }

	/**
	 * Approximate a function which is not modeled.
	 * @return A function which has no side effects that that returns the
	 * 		   BValue lattice element top.
	 */
	public Obj constFunctionObj(BValue retVal) {

		Map<Identifier, Address> external = new HashMap<Identifier, Address>();

		Closure closure = new NativeClosure() {
				@Override
				public State run(Address selfAddr, Address argArrayAddr,
								 Store store, Scratchpad scratchpad,
								 Trace trace, Control control,
								 Stack<Address> callStack) {
					scratchpad.strongUpdate(Scratch.RETVAL, retVal);
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
