package ca.ubc.ece.salt.pangor.analysis.flow.factories;

import java.util.HashMap;
import java.util.Map;
import java.util.Stack;

import ca.ubc.ece.salt.pangor.analysis.flow.abstractdomain.Address;
import ca.ubc.ece.salt.pangor.analysis.flow.abstractdomain.BValue;
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
import ca.ubc.ece.salt.pangor.analysis.flow.trace.Trace;


public class ArgumentsFactory {

	public static final Obj Arguments_Obj;
	static {
		Map<String, BValue> external = new HashMap<String, BValue>();
		external.put("prototype", Address.inject(StoreFactory.Object_proto_Addr));
		external.put("length", Num.inject(Num.top()));

		NativeClosure closure = new NativeClosure() {
				@Override
				public State run(BValue selfAddr, BValue argArrayAddr,
//								 String x, // The address to store selfAddr. Why do we have to do this here?
								 Store store, Scratchpad scratchpad,
								 Trace trace) {
					/* Add self to the store. */
					return new State(store, null, scratchpad, trace);
				}
			};

		Stack<Closure> closures = new Stack<Closure>();
		closures.push(closure);

		InternalObjectProperties internal = new InternalFunctionProperties(closures, JSClass.CArguments);

		Arguments_Obj = new Obj(external, internal, external.keySet());
	}

}