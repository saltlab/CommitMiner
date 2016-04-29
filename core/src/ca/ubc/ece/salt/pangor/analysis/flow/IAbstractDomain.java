package ca.ubc.ece.salt.pangor.analysis.flow;

import ca.ubc.ece.salt.pangor.cfg.CFGEdge;
import ca.ubc.ece.salt.pangor.cfg.CFGNode;

/**
 * Stores the state of an abstract domain at some point in the control flow
 * graph. Typically this will be one or many lattice elements, but could also
 * be more complicated structures like the environment or store.
 */
public interface IAbstractDomain {

	/**
	 * Transfer over an edge.
	 * @return the state of the abstract domain after the transfer.
	 */
	public IAbstractDomain transfer(CFGEdge edge);

	/**
	 * Transfer over a node.
	 * @return the state of the abstract domain after the transfer.
	 */
	public IAbstractDomain transfer(CFGNode node);

	/**
	 * Joins two states from the same abstract domain.
	 * @param The state to join with. May be null.
	 * @return The state of the abstract domain after the join.
	 */
	public IAbstractDomain join(IAbstractDomain ad);

}