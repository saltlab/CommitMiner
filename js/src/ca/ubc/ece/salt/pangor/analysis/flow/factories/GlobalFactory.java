package ca.ubc.ece.salt.pangor.analysis.flow.factories;

import java.util.HashMap;
import java.util.Map;

import ca.ubc.ece.salt.pangor.analysis.flow.abstractdomain.Address;
import ca.ubc.ece.salt.pangor.analysis.flow.abstractdomain.InternalObjectProperties;
import ca.ubc.ece.salt.pangor.analysis.flow.abstractdomain.Obj;
import ca.ubc.ece.salt.pangor.analysis.flow.abstractdomain.Store;
import ca.ubc.ece.salt.pangor.analysis.flow.abstractdomain.Undefined;

/**
 * Initializes the global environment with builtins.
 */
public class GlobalFactory {

	Store store;

	public GlobalFactory(Store store) {
		this.store = store;
	}

	public Obj Global_Obj() {
		Map<String, Address> ext = new HashMap<String, Address>();
		Helpers.addProp("Object", Address.inject(StoreFactory.Object_Addr), ext, store);
		Helpers.addProp("undefined", Undefined.inject(Undefined.top()), ext, store);

		InternalObjectProperties internal = new InternalObjectProperties();
		return new Obj(ext, internal, ext.keySet());
	}

}