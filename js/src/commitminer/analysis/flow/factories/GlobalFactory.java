package commitminer.analysis.flow.factories;

import java.util.HashMap;
import java.util.Map;

import commitminer.analysis.flow.abstractdomain.Address;
import commitminer.analysis.flow.abstractdomain.Change;
import commitminer.analysis.flow.abstractdomain.Identifier;
import commitminer.analysis.flow.abstractdomain.InternalObjectProperties;
import commitminer.analysis.flow.abstractdomain.Obj;
import commitminer.analysis.flow.abstractdomain.Store;
import commitminer.analysis.flow.abstractdomain.Undefined;

/**
 * Initializes the global environment with builtins.
 */
public class GlobalFactory {

	Store store;

	public GlobalFactory(Store store) {
		this.store = store;
	}

	public Obj Global_Obj() {
		Map<Identifier, Address> ext = new HashMap<Identifier, Address>();
		store = Helpers.addProp("Object", Address.inject(StoreFactory.Object_Addr, Change.u(), Change.u()), ext, store);
		store = Helpers.addProp("undefined", Undefined.inject(Undefined.top(Change.u()), Change.u()), ext, store);

		InternalObjectProperties internal = new InternalObjectProperties();
		return new Obj(ext, internal);
	}

}