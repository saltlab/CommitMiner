package ca.ubc.ece.salt.pangor.analysis.flow.abstractdomain;

import ca.ubc.ece.salt.pangor.cfg.CFG;

/**
 * The abstract domain for function closures.
 */
public class FunctionClosure extends Closure {

	/** The function. **/
	public CFG cfg;

	/** The closure environment? **/
	public Environment environment;

	public FunctionClosure(CFG cfg, Environment environment) {
		this.cfg = cfg;
		this.environment = environment;
	}

	@Override
	public State run(BValue selfAddr, BValue argArrayAddr, String x,
			Environment environment, Store store, Scratchpad scratchpad) {
		// TODO Auto-generated method stub
		return null;
	}


}