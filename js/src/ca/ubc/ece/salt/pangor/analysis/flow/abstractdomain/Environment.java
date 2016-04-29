package ca.ubc.ece.salt.pangor.analysis.flow.abstractdomain;

import java.util.List;
import java.util.Map;

import org.mozilla.javascript.ast.ScriptNode;

import ca.ubc.ece.salt.pangor.analysis.flow.Address;
import ca.ubc.ece.salt.pangor.analysis.flow.IAbstractDomain;
import ca.ubc.ece.salt.pangor.cfg.CFGEdge;
import ca.ubc.ece.salt.pangor.cfg.CFGNode;

/**
 * The abstract domain for storing mappings from identifiers to addresses.
 * i.e. Environment# := String#->P(BValue# | Address#)
 *
 * Identifiers may be
 */
public class Environment implements IAbstractDomain {

	/** The possible memory address for each identifier. **/
	private Map<String, List<Address>> environment;

	/** The abstract domain for the program's store. **/
	private Store store;

	/**
	 * Create the initial state for the environment. The initial sate includes
	 * variables and functions that are declared within the function and
	 * therefore raised before the function is executed.
	 * @param function The script of function we are analyzing.
	 * @param store The memory store for the analysis.
	 */
	public Environment(ScriptNode function, Store store) {
		// TODO: Visit all the statements in the function and raise functions
		//		 and variables declared within the function.
	}

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

}
