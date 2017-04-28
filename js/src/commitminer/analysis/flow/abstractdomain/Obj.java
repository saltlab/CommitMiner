package commitminer.analysis.flow.abstractdomain;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

/**
 * The abstract domain for objects. An object is represented as
 * 	(1) a map of programmer-accessible properties
 * 	(2) a set of interpreter-only properties
 * 	(3) a list of properties that are definitely present.
 */
public class Obj {

	/** Programmer-visible object properties. **/
	public Map<Identifier, Address> externalProperties;

	/** Interpreter-only properties which are invisible to the programmer. **/
	public InternalObjectProperties internalProperties;

	/**
	 * Initialize the object. The initial store will contain a number of
	 * build-in objects which will be initialized from a template. The
	 * remainder of objects will be defined by the program and instantiated
	 * during the analysis.
	 * @param ext Programmer-visible object properties.
	 * @param internalProperties Interpreter-only properties which are
	 * 							 invisible to the programmer.
	 * @param definitelyPresentProperties Properties which are definitely
	 * 									  present in the object.
	 */
	public Obj(Map<Identifier, Address> ext,
					InternalObjectProperties internalProperties) {
		this.externalProperties = ext;
		this.internalProperties = internalProperties;
	}

//	/**
//	 * Initialize the function.
//	 * @param externalProperties Programmer-visible object properties.
//	 * @param internalProperties Interpreter-only properties which are
//	 * 							 invisible to the programmer.
//	 * @param definitelyPresentProperties Properties which are definitely
//	 * 									  present in the object.
//	 */
//	public Obj(HashMap<Identifier, Address> externalProperties,
//				InternalFunctionProperties internalProperties) {
//		this.externalProperties = externalProperties;
//		this.internalProperties = internalProperties;
//	}

	/**
	 * @param property The property to look up.
	 * @return true if the property is definitely not present in this object.
	 */
	public boolean definitelyNotProperty(String property) {
		return this.externalProperties.containsKey(property);
	}

	/**
	 * NOTE: Not complete semantics. Must also go up this object's prototype
	 * 		 chain if the property is not found.
	 * @param property The name of the property to retrieve.
	 * @return A property or null if it does not exist.
	 */
	public Address apply(String property) {
		return this.externalProperties.get(property);
	}

	/**
	 * Computes ο u ο.
	 * @param right The object to merge with.
	 * @param bValueMap The BValue map from the store.
	 * @return A new Obj with this Obj joined with the Obj parameter.
	 */
	public Obj join(Obj right, Map<Address, BValue> bValueMap) {
//		if(this.internalProperties.klass != right.internalProperties.klass) TODO: Ignore this for diff analysis. We dynamically create objects and type does not matter.
//			throw new Error("Cannot join classes of different types.");

		Map<Identifier, Address> ext = new HashMap<Identifier, Address>(this.externalProperties);

		for(Entry<Identifier, Address> entry : right.externalProperties.entrySet()) {
			if(!ext.containsKey(entry.getKey()))
				ext.put(entry.getKey(), entry.getValue());
			else {
				Address lAddr = ext.get(entry.getKey());
				Address rAddr = entry.getValue();

				BValue bvl = bValueMap.get(lAddr);
				BValue bvr = bValueMap.get(rAddr);

				if(!lAddr.equals(rAddr)) {

					/* Uh oh... we have two addresses for the same property.
					 * We need to merge the properties in the store. This is only
					 * ok if we only execute loops once. A better solution would be
					 * to refactor to store a set of addresses. */

					bValueMap.put(lAddr, bvl.join(bvr));

				}
			}
		}

		return new Obj(ext, this.internalProperties);

	}

	@Override
	public String toString() {
		String extProp = "";
		for(Identifier prop : this.externalProperties.keySet())
			extProp += prop.name + "|";
		return "Obj:" + this.externalProperties.size() + "|" + extProp;
	}

}