package ca.ubc.ece.salt.pangor.analysis.flow.abstractdomain;

import ca.ubc.ece.salt.pangor.cfg.CFG;

/**
 * The abstract domain for function closures.
 */
public class Closure {

	/** The control flow graph for the function. **/
	public CFG cfg;

	/**
	 * The environment for the function (i.e., the parameters and local
	 * variables).
	 */
	public Environment environment;

}