package ca.ubc.ece.salt.pangor.analysis.flow.factories;

import java.util.HashMap;
import java.util.Map;
import java.util.Stack;

import org.mozilla.javascript.ast.AstNode;

import ca.ubc.ece.salt.pangor.analysis.flow.abstractdomain.Address;
import ca.ubc.ece.salt.pangor.analysis.flow.abstractdomain.Change;
import ca.ubc.ece.salt.pangor.analysis.flow.abstractdomain.Closure;
import ca.ubc.ece.salt.pangor.analysis.flow.abstractdomain.Control;
import ca.ubc.ece.salt.pangor.analysis.flow.abstractdomain.Identifier;
import ca.ubc.ece.salt.pangor.analysis.flow.abstractdomain.InternalFunctionProperties;
import ca.ubc.ece.salt.pangor.analysis.flow.abstractdomain.InternalObjectProperties;
import ca.ubc.ece.salt.pangor.analysis.flow.abstractdomain.JSClass;
import ca.ubc.ece.salt.pangor.analysis.flow.abstractdomain.NativeClosure;
import ca.ubc.ece.salt.pangor.analysis.flow.abstractdomain.Num;
import ca.ubc.ece.salt.pangor.analysis.flow.abstractdomain.Obj;
import ca.ubc.ece.salt.pangor.analysis.flow.abstractdomain.Scratchpad;
import ca.ubc.ece.salt.pangor.analysis.flow.abstractdomain.State;
import ca.ubc.ece.salt.pangor.analysis.flow.abstractdomain.Store;
import ca.ubc.ece.salt.pangor.analysis.flow.trace.Trace;
import ca.ubc.ece.salt.pangor.cfg.CFG;


public class ArgumentsFactory {

	Store store;
	Map<AstNode, CFG> cfgs;

	public ArgumentsFactory(Store store, Map<AstNode, CFG> cfgs) {
		this.store = store;
		this.cfgs = cfgs;
	}

	public Obj Arguments_Obj() {
		Map<Identifier, Address> ext = new HashMap<Identifier, Address>();
		store = Helpers.addProp("prototype", Address.inject(StoreFactory.Object_proto_Addr, Change.u(), Change.u()), ext, store);
		store = Helpers.addProp("length", Num.inject(Num.top(Change.u()), Change.u()), ext, store);

		NativeClosure closure = new NativeClosure() {
				@Override
				public State run(Address selfAddr, Address argArrayAddr,
								 Store store, Scratchpad scratchpad,
								 Trace trace, Control control,
								 Stack<Address> callStack) {
					return new State(store, null, scratchpad, trace, control,
									 selfAddr, cfgs, callStack);
				}
			};

		Stack<Closure> closures = new Stack<Closure>();
		closures.push(closure);

		InternalObjectProperties internal = new InternalFunctionProperties(closures, JSClass.CArguments);

		return new Obj(ext, internal);
	}

}