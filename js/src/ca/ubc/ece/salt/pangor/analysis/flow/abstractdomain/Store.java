package ca.ubc.ece.salt.pangor.analysis.flow.abstractdomain;

import java.util.HashMap;
import java.util.Map;

import ca.ubc.ece.salt.pangor.analysis.flow.Address;
import ca.ubc.ece.salt.pangor.analysis.flow.IAbstractDomain;

/**
 * The abstract domain for the program's store (memory).
 * i.e. Store# := Address# -> P(BValue# + Object#)
 */
public class Store {

	/** The data structures at each address. **/
	private Map<Address, IAbstractDomain> store;

	/**
	 * Create the initial state for the store. The initial state is an empty
	 * store. However, the environment may initialize the store with the
	 * following objects before the analysis begins:
	 * 	(1) variables declared in the function and raised - which will initially
	 * 		point to the primitive value 'undefined'.
	 * 	(2) functions declared in the function and raised - which will point to
	 * 		their function object.
	 *
	 * Changes to the store are driven by changes to the environment.
	 */
	public Store() {
		this.store = new HashMap<Address, IAbstractDomain>();
	}

}