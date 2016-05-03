package ca.ubc.ece.salt.pangor.analysis.flow.abstractdomain;

import java.util.Map;
import java.util.Set;

import ca.ubc.ece.salt.pangor.cfg.CFGEdge;
import ca.ubc.ece.salt.pangor.cfg.CFGNode;

/**
 * The abstract domain for objects. An object is represented as
 * 	(1) a map of programmer-accessible properties
 * 	(2) a set of interpreter-only properties
 * 	(3) a list of properties that are definitely present.
 */
public class Obj {

	/** Programmer-visible object properties. **/
	private Map<String, BValue> externalProperties;

	/** Interpreter-only properties which are invisible to the programmer. **/
	private InternalObjectProperties internalProperties;

	/** Tracks properties that definitely exist in the object. **/
	private Set<String> definitelyPresentProperties;

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

	public Obj transfer(CFGEdge edge) {
		// TODO Auto-generated method stub
		return null;
	}

	public Obj transfer(CFGNode node) {
		// TODO Auto-generated method stub
		return null;
	}

	public Obj join(Obj ad) {
		// TODO Auto-generated method stub
		return null;
	}

	/** The possible object classes. **/
	public enum Klass {
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
