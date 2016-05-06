package ca.ubc.ece.salt.pangor.analysis.flow.abstractdomain;

import java.util.Set;
import java.util.Stack;

import ca.ubc.ece.salt.pangor.analysis.flow.abstractdomain.Obj.JSClass;
import ca.ubc.ece.salt.pangor.analysis.flow.factories.StoreFactory;

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
	private Set<Closure> closures;

	/**
	 * @param prototype The address of the function prototype.
	 * @param closure The control flow graph and environment stack.
	 * @param jsclass The type of object being created.
	 */
	public InternalFunctionProperties(BValue prototype, Set<Closure> closures, JSClass jsclass) {
		super(prototype, jsclass);
		this.closures = closures;
	}

	/**
	 * Prototype defaults to Function_proto_Addr.
	 * @param closure The control flow graph and environment stack.
	 * @param jsclass The type of object being created.
	 */
	public InternalFunctionProperties(Stack<Closure> closures, JSClass jsclass) {
		super(Address.inject(StoreFactory.Function_proto_Addr), jsclass);
	}

	@Override
	public Set<Closure> getCode() {
		return this.closures;
	}

}