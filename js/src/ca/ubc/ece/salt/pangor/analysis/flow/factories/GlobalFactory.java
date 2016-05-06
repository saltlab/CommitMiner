package ca.ubc.ece.salt.pangor.analysis.flow.factories;

import java.util.HashMap;
import java.util.Map;

import ca.ubc.ece.salt.pangor.analysis.flow.abstractdomain.Address;
import ca.ubc.ece.salt.pangor.analysis.flow.abstractdomain.BValue;
import ca.ubc.ece.salt.pangor.analysis.flow.abstractdomain.InternalObjectProperties;
import ca.ubc.ece.salt.pangor.analysis.flow.abstractdomain.Obj;
import ca.ubc.ece.salt.pangor.analysis.flow.abstractdomain.Undefined;

/**
 * Initializes the global environment with builtins.
 */
public class GlobalFactory {

	public static final Obj Global_Obj;
	static {
		Map<String, BValue> external = new HashMap<String, BValue>();
		external.put("Object", Address.inject(StoreFactory.Object_Addr));
		external.put("undefined", Undefined.inject(Undefined.top()));

		InternalObjectProperties internal = new InternalObjectProperties();
		Global_Obj = new Obj(external, internal, external.keySet());
	}

}