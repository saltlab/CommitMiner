package ca.ubc.ece.salt.pangor.analysis.flow.abstractdomain;

/**
 * A native (builtin) function. The abstract interpretation of the function is
 * specified in Java, usually as a lambda expression.
 */
public abstract class Closure extends SmartHash {

	/**
	 * @param selfAddr The address of the 'this' object.
	 * @param argArrayAddr The address of the argument array object.
	 * @param x The variable for the return value.
	 * @param environment The environment (JS closure).
	 * @param store Main memory.
	 * @param scratchpad Scratchpad memory.
	 * @return The new state after executing the function.
	 */
	public abstract State run(BValue selfAddr, BValue argArrayAddr, String x,
			  				  Environment environment, Store store,
			  				  Scratchpad scratchpad);

}