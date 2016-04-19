package ca.ubc.ece.salt.pangor.analysis.flow;

import ca.ubc.ece.salt.pangor.cfg.CFGEdge;
import ca.ubc.ece.salt.pangor.cfg.CFGNode;

public interface IAbstractState {

	/**
	 * Transfer over an edge.
	 */
	public<AS extends IAbstractState> AS transfer(CFGEdge edge);

	/**
	 * Transfer over a node.
	 */
	public<AS extends IAbstractState> AS transfer(CFGNode node);

	/**
	 * Joins two {@code AbstractLatticeElement}s with LUB
	 * @return The joined lattice element.
	 */
	public<AS extends IAbstractState> AS join(AS as);

}
