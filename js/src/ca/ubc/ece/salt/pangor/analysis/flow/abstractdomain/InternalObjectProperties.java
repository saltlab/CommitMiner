package ca.ubc.ece.salt.pangor.analysis.flow.abstractdomain;

import java.util.Stack;

import ca.ubc.ece.salt.pangor.analysis.flow.abstractdomain.Obj.JSClass;

/** Internal properties for an abstract (non-function) object. **/
public class InternalObjectProperties {

	/**
	 * The prototype of the object. This is either the prototype of the
	 * constructor or the prototype of Object (if defined as an object
	 * literal).
	 */
	public BValue prototype;

	/** The type of object. This is the "based on the constructor
	 * function's object address" [notJS Concrete Semantics]. **/
	public JSClass klass;

	public InternalObjectProperties(BValue prototype, JSClass klass) {
		this.prototype = prototype;
		this.klass = klass;
	}

	/**
	 * @return The set of closures. Empty if this object is not a function.
	 */
	public Stack<Closure> getCode() {
		return new Stack<Closure>();
	}

}