package ca.ubc.ece.salt.pangor.analysis.flow.abstractdomain;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

/**
 * The abstract domain for objects. An object is represented as
 * 	(1) a map of programmer-accessible properties
 * 	(2) a set of interpreter-only properties
 * 	(3) a list of properties that are definitely present.
 */
public class Obj extends SmartHash {

	/** Programmer-visible object properties. **/
	public Map<String, Address> externalProperties;

	/** Interpreter-only properties which are invisible to the programmer. **/
	public InternalObjectProperties internalProperties;

	/** Tracks properties that definitely exist in the object. **/
	public Set<String> definitelyPresentProperties;

	/**
	 * Initialize the object. The initial store will contain a number of
	 * build-in objects which will be initialized from a template. The
	 * remainder of objects will be defined by the program and instantiated
	 * during the analysis.
	 * @param externalProperties Programmer-visible object properties.
	 * @param internalProperties Interpreter-only properties which are
	 * 							 invisible to the programmer.
	 * @param definitelyPresentProperties Properties which are definitely
	 * 									  present in the object.
	 */
	public Obj(Map<String, Address> externalProperties,
					InternalObjectProperties internalProperties,
					Set<String> definitelyPresentProperties) {
		this.externalProperties = externalProperties;
		this.internalProperties = internalProperties;
		this.definitelyPresentProperties = definitelyPresentProperties;
	}

	/**
	 * Initialize the function.
	 * @param externalProperties Programmer-visible object properties.
	 * @param internalProperties Interpreter-only properties which are
	 * 							 invisible to the programmer.
	 * @param definitelyPresentProperties Properties which are definitely
	 * 									  present in the object.
	 */
	public Obj(Map<String, Address> externalProperties,
				InternalFunctionProperties internalProperties,
				Set<String> definitelyPresentProperties) {
		this.externalProperties = externalProperties;
		this.internalProperties = internalProperties;
		this.definitelyPresentProperties = definitelyPresentProperties;
	}

	/**
	 * @param property The property to look up.
	 * @return true if the property is definitely present in this object.
	 */
	public boolean definitelyProperty(String property) {
		return this.definitelyPresentProperties.contains(property);
	}

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
		if(this.internalProperties.klass != right.internalProperties.klass)
			throw new Error("Cannot join classes of different types.");

		Map<String, Address> ext = new HashMap<String, Address>(this.externalProperties);

		for(Entry<String, Address> entry : right.externalProperties.entrySet()) {
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

		return new Obj(ext, this.internalProperties, ext.keySet());

	}

	@Override
	public String toString() {
		String extProp = "";
		for(String prop : this.externalProperties.keySet())
			extProp += prop + "|";
		return "Obj:" + this.externalProperties.size() + "|" + extProp;
	}

}