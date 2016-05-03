package ca.ubc.ece.salt.pangor.analysis.flow.abstractdomain;

import ca.ubc.ece.salt.pangor.analysis.flow.abstractdomain.Obj.Klass;

/** Internal properties for an abstract (non-function) object. **/
public class InternalObjectProperties {

	/**
	 * The prototype of the object. This is either the prototype of the
	 * constructor or the prototype of Object (if defined as an object
	 * literal).
	 */
	public Address prototype;

	/** The type of object. This is the "based on the constructor
	 * function's object address" [notJS Concrete Semantics]. **/
	public Klass klass;

	public InternalObjectProperties(Address prototype, Klass klass) {
		this.prototype = prototype;
		this.klass = klass;
	}

}