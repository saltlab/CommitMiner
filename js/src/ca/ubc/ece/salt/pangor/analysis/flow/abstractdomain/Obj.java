package ca.ubc.ece.salt.pangor.analysis.flow.abstractdomain;

import java.util.Map;
import java.util.Set;

/**
 * The abstract domain for objects. An object is represented as
 * 	(1) a map of programmer-accessible properties
 * 	(2) a set of interpreter-only properties
 * 	(3) a list of properties that are definitely present.
 */
public class Obj extends SmartHash {

	/** Programmer-visible object properties. **/
	public Map<String, BValue> externalProperties;

	/** Interpreter-only properties which are invisible to the programmer. **/
	public InternalObjectProperties internalProperties;

	/** Tracks properties that definitely exist in the object. **/
	public Set<String> definitelyPresentProperties;

	/** The type of class. **/
	public JSClass jsClass;

	/** This class' prototype. **/
	public BValue protoAddr;

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
	public Obj(Map<String, BValue> externalProperties,
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
	public Obj(Map<String, BValue> externalProperties,
				InternalFunctionProperties internalProperties,
				Set<String> definitelyPresentProperties) {

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
	 * NOTE: Not complete semantics. Must also go up this object's pototype
	 * 		 chain if the property is not found.
	 * @param property The name of the property to retrieve.
	 * @return A property or null if it does not exist.
	 */
	public BValue apply(String property) {
		return this.externalProperties.get(property);
	}

	public Obj inject() { return null; }

	/**
	 * Performs a strong update of the object's external properties.
	 * @return the new object after the update.
	 */
	public Obj strongUpdate(Map<String, BValue> eps) { return null; }

	/**
	 * Performs a weak update of the object's external properties.
	 * @return the new object after the update.
	 */
	public Obj weakUpdate(Map<String, BValue> eps) { return null; }


	public Obj join(Obj ad) {
		if(this.jsClass != ad.jsClass) throw new Error("Cannot join classes of different types.");
		// TODO Auto-generated method stub
		return null;
	}

	/** The possible object types. **/
	public enum JSClass {
		FUNCTION,
		ARRAY,
		STRING,
		BOOLEAN,
		NUMBER,
		DATE,
		ERROR,
		REGEXP,
		ARGUMENTS,
		CObject_Obj
	}

}
