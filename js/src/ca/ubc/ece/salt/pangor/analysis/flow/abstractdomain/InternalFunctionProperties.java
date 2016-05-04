package ca.ubc.ece.salt.pangor.analysis.flow.abstractdomain;

import java.util.Stack;

import ca.ubc.ece.salt.pangor.analysis.flow.abstractdomain.Obj.JSClass;

/**
 * Internal properties for an abstract Function object.
 *
 * The meaning of the prototype is slightly different for functions:
 * function prototypes are used as prototypes for creating new objects
 * when the function is a constructor. However, function prototypes point
 * to the same prototype objects as object prototypes.
 */
public class InternalFunctionProperties extends InternalObjectProperties {

	/** The function's code and environment. **/
	private Stack<Closure> closures;

	/**
	 * @param prototype The address of the function prototype.
	 * @param closure The control flow graph and environment stack.
	 */
	public InternalFunctionProperties(BValue prototype, Stack<Closure> closures, JSClass jsclass) {
		super(prototype, jsclass);
		this.closures = closures;
	}

	@Override
	public Stack<Closure> getCode() {
		return this.closures;
	}

}