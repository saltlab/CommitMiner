package commitminer.analysis.flow.factories;

import java.util.Map;

import commitminer.analysis.flow.abstractdomain.Address;
import commitminer.analysis.flow.abstractdomain.BValue;
import commitminer.analysis.flow.abstractdomain.Change;
import commitminer.analysis.flow.abstractdomain.Property;
import commitminer.analysis.flow.abstractdomain.Store;

public class Helpers {

	/**
	 * Adds a property to the object and allocates the property's value on the
	 * store.
	 * @param prop The name of the property to add to the object.
	 */
	public static Store addProp(String prop, Integer definerID, BValue propVal, Map<String, Property> ext, Store store) {
		Address propAddr = Address.createBuiltinAddr(prop);
		store = store.alloc(propAddr, propVal);
		ext.put(prop, new Property(definerID, prop, Change.u(), propAddr));
		return store;
	}

}