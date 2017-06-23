package commitminer.analysis.flow.factories;

import java.util.HashMap;
import java.util.Map;

import commitminer.analysis.flow.abstractdomain.Address;
import commitminer.analysis.flow.abstractdomain.Change;
import commitminer.analysis.flow.abstractdomain.Property;
import commitminer.analysis.flow.abstractdomain.InternalObjectProperties;
import commitminer.analysis.flow.abstractdomain.Obj;
import commitminer.analysis.flow.abstractdomain.Store;
import commitminer.analysis.flow.abstractdomain.Undefined;

/**
 * Initializes the global environment with builtins.
 */
public class GlobalFactory {
	
	private static final Integer OBJECT_DEFINER_ID = -10;
	private static final Integer UNDEFINED_DEFINER_ID = -11;

	Store store;

	public GlobalFactory(Store store) {
		this.store = store;
	}

	public Obj Global_Obj() {
		Map<String, Property> ext = new HashMap<String, Property>();
		store = Helpers.addProp("Object", OBJECT_DEFINER_ID, Address.inject(StoreFactory.Object_Addr, Change.u(), Change.u()), ext, store);
		store = Helpers.addProp("undefined", UNDEFINED_DEFINER_ID, Undefined.inject(Undefined.top(Change.u()), Change.u()), ext, store);

		InternalObjectProperties internal = new InternalObjectProperties();
		return new Obj(ext, internal);
	}

}