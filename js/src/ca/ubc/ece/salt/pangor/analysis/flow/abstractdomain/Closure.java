package ca.ubc.ece.salt.pangor.analysis.flow.abstractdomain;

import ca.ubc.ece.salt.pangor.analysis.flow.trace.Trace;

/**
 * A native (builtin) function. The abstract interpretation of the function is
 * specified in Java, usually as a lambda expression.
 */
public abstract class Closure extends SmartHash {

	/**
	 * @param selfAddr The address of the 'this' object.
	 * @param argArrayAddr The address of the argument array object.
	 * @param store Main memory.
	 * @param scratchpad Scratchpad memory.
	 * @param trace The execution trace.
	 * @return The new state after executing the function.
	 */
	public abstract State run(BValue selfAddr, Address argArrayAddr,
							  Store store, Scratchpad scratchpad,
							  Trace trace);

}