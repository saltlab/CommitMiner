package ca.ubc.ece.salt.pangor.analysis.flow.abstractdomain;

import java.util.Stack;

import ca.ubc.ece.salt.pangor.analysis.flow.abstractdomain.Obj.Klass;
import ca.ubc.ece.salt.pangor.cfg.CFG;

/**
 * Internal properties for an abstract Function object.
 *
 * The meaning of the prototype is slightly different for functions:
 * function prototypes are used as prototypes for creating new objects
 * when the function is a constructor. However, function prototypes point
 * to the same prototype objects as object prototypes.
 */
public class InternalFunctionProperties extends InternalObjectProperties {

	/** The code (cfg) for the function. **/
	public CFG cfg;

	/** The environments in the closure. **/
	public Stack<Environment> closures;

	public InternalFunctionProperties(Address prototype, CFG cfg, Stack<Environment> closures) {
		super(prototype, Klass.FUNCTION);
		this.cfg = cfg;
		this.closures = closures;
	}

}