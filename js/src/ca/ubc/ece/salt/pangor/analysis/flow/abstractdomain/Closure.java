package ca.ubc.ece.salt.pangor.analysis.flow.abstractdomain;

import ca.ubc.ece.salt.pangor.analysis.flow.trace.Trace;

/**
 * A native (builtin) function. The abstract interpretation of the function is
 * specified in Java, usually as a lambda expression.
 */
public abstract class Closure {

	/**
	 * @param selfAddr The value of the 'this' variable (a set of objects).
	 * @param argArrayAddr The address of the argument array object.
	 * @param store Main memory.
	 * @param scratchpad Scratchpad memory.
	 * @param trace The execution trace.
	 * @return The new state after executing the function.
	 */
	public abstract State run(Address selfAddr, Address argArrayAddr,
							  Store store, Scratchpad scratchpad,
							  Trace trace);

}