package ca.ubc.ece.salt.pangor.analysis.flow.factories;

import java.util.Map;

import ca.ubc.ece.salt.pangor.analysis.flow.abstractdomain.Address;
import ca.ubc.ece.salt.pangor.analysis.flow.abstractdomain.BValue;
import ca.ubc.ece.salt.pangor.analysis.flow.abstractdomain.Store;

public class Helpers {

	/**
	 * Adds a property to the object and allocates the property's value on the
	 * store.
	 * @param prop The name of the property to add to the object.
	 */
	public static void addProp(String prop, BValue propVal, Map<String, Address> ext, Store store) {
		Address propAddr = Address.createBuiltinAddr(prop);
		store.alloc(propAddr, propVal);
		ext.put(prop, propAddr);
	}

}