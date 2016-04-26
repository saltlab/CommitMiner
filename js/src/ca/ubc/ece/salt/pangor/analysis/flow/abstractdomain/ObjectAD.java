package ca.ubc.ece.salt.pangor.analysis.flow.abstractdomain;

import java.util.Map;
import java.util.Set;
import java.util.Stack;

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

	private Map<String, BaseValue> values;

	private Set<String> properties;

	private Map<String, ClassSpecificValue> classSpecificValues;

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

	/** Values specific to a class. **/
	private class ClassSpecificValue {

		/** The value. **/
		public BaseValue value;

		/** The class where it comes from. */
		public Class klass;

		/**
		 * The closure stack assoicated with the function if the class is a
		 * function.
		 */
		public Stack<Environment> closures;

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
		OBJECT
	}

}
