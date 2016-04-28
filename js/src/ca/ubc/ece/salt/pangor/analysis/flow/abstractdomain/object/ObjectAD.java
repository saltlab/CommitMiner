package ca.ubc.ece.salt.pangor.analysis.flow.abstractdomain.object;

import java.util.Map;
import java.util.Set;
import java.util.Stack;

import ca.ubc.ece.salt.pangor.analysis.flow.abstractdomain.AddressAD;
import ca.ubc.ece.salt.pangor.analysis.flow.abstractdomain.BaseValue;
import ca.ubc.ece.salt.pangor.analysis.flow.abstractdomain.Environment;
import ca.ubc.ece.salt.pangor.analysis.flow.abstractdomain.IAbstractDomain;
import ca.ubc.ece.salt.pangor.cfg.CFG;
import ca.ubc.ece.salt.pangor.cfg.CFGEdge;
import ca.ubc.ece.salt.pangor.cfg.CFGNode;

/**
 * The abstract domain for objects. An object is represented as
 * 	(1) a mapping of properties to values
 * 	(2) a list of properties that definitely exist
 * 	(3) a mapping of properties to class-specific values. The mapping is
 * 		String->(BValue# + Class + P(Closure)). For example, a function
 * 		mapping looks like:
 * 		[funct name]->([address] + function + {list of closures})
 */
public class ObjectAD implements IAbstractDomain {

	/** Programmer-visible object properties. **/
	private Map<String, BaseValue> externalProperties;

	/**
	 * Programmer-invisible object properties which are used by the
	 * interpreter */
	private InternalObjectProperties internalProperties;

	/** Tracks properties that definitely exist in the object. **/
	private Set<String> definitelyPresentProperties;

	@Override
	public IAbstractDomain transfer(CFGEdge edge) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public IAbstractDomain transfer(CFGNode node) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public IAbstractDomain join(IAbstractDomain ad) {
		// TODO Auto-generated method stub
		return null;
	}

	/** Internal properties for an abstract (non-function) object. **/
	private class InternalObjectProperties {

		/**
		 * The prototype of the object. This is either the prototype of the
		 * constructor or the prototype of Object (if defined as an object
		 * literal).
		 */
		public AddressAD prototype;

		/** The type of object. This is the "based on the constructor
		 * function's object address" [notJS Concrete Semantics]. **/
		public Class klass;

		public InternalObjectProperties(AddressAD prototype, Class klass) {
			this.prototype = prototype;
			this.klass = klass;
		}

	}

	/**
	 * Internal properties for an abstract Function object.
	 *
	 * The meaning of the prototype is slightly different for functions:
	 * function prototypes are used as prototypes for creating new objects
	 * when the function is a constructor. However, function prototypes point
	 * to the same prototype objects as object prototypes.
	 */
	private class InternalFunctionProperties extends InternalObjectProperties {

		/** The code (cfg) for the function. **/
		public CFG cfg;

		/** The environments in the closure. **/
		public Stack<Environment> closures;

		public InternalFunctionProperties(AddressAD prototype, CFG cfg, Stack<Environment> closures) {
			super(prototype, Class.FUNCTION);
			this.cfg = cfg;
			this.closures = closures;
		}

	}

	/** The possible object classes. **/
	private enum Class {
		FUNCTION,
		ARRAY,
		STRING,
		BOOLEAN,
		NUMBER,
		DATE,
		ERROR,
		REGEXP,
		ARGUMENTS,
		OBJECT,
		OTHER 		// TODO: This is probably not the way to do this. Each
					//		 class should have its own name. There are some
					//		 default classes which need to be pre-defined,
					//		 e.g., String, Number, Boolean, Function etc.

	}

}
