package ca.ubc.ece.salt.pangor.analysis.flow.abstractdomain;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import ca.ubc.ece.salt.pangor.analysis.flow.abstractdomain.Obj.JSClass;
import ca.ubc.ece.salt.pangor.analysis.flow.factories.StoreFactory;

public class Helpers {

	/**
	 * Creates a regular function from a closure stack.
	 * @param closures The stack of closures (code and environments).
	 * @return The function object.
	 */
	public static Obj createFunctionObj(Closure closure) {

		Set<Closure> closures = new HashSet<Closure>();
		closures.add(closure);

		Map<String, BValue> external = new HashMap<String, BValue>();
		external.put("length", Num.inject(Num.top()));

		InternalFunctionProperties internal = new InternalFunctionProperties(
				Address.inject(StoreFactory.Function_proto_Addr),
				closures,
				JSClass.CFunction);

		return new Obj(external, internal, external.keySet());

	}

}
