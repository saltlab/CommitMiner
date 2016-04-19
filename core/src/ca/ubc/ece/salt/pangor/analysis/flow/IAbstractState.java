package ca.ubc.ece.salt.pangor.analysis.flow;

import ca.ubc.ece.salt.pangor.cfg.CFGEdge;
import ca.ubc.ece.salt.pangor.cfg.CFGNode;

/**
 * Stores the state of the abstract interpretation at a given point of the
 * program. This could be a lattice element, or it could store many lattice
 * elements (e.g., one for each variable in a dataflow analysis).
 */
public interface IAbstractState {

	/**
	 * Transfer over an edge.
	 */
	public IAbstractState transfer(CFGEdge edge);

	/**
	 * Transfer over a node.
	 */
	public IAbstractState transfer(CFGNode node);

	/**
	 * Joins two {@code AbstractLatticeElement}s with LUB
	 * @return The joined lattice element.
	 */
	public IAbstractState join(IAbstractState as);

}
