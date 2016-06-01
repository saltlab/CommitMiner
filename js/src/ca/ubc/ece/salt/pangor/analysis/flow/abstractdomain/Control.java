package ca.ubc.ece.salt.pangor.analysis.flow.abstractdomain;

import java.util.HashMap;
import java.util.Map;

import ca.ubc.ece.salt.pangor.cfg.CFGEdge;
import ca.ubc.ece.salt.pangor.cfg.CFGNode;

/**
 * Stores the state of control flow changes.
 */
public class Control {

	/**
	 * Tracks control flow changes for each branch.
	 */
	public Map<CFGNode, Integer> branchChanges;

	public Control() {
		branchChanges = new HashMap<CFGNode, Integer>();
	}

	public Control(Map<CFGNode, Integer> branchChanges) {
		this.branchChanges = branchChanges;
	}

	@Override
	public Control clone() {
		return new Control(new HashMap<CFGNode, Integer>(branchChanges));
	}

	/**
	 * Updates the state for the branch conditions exiting the CFGNode.
	 * @return The new state (ControlFlowChange) after update.
	 */
	public Control update(CFGNode node) {

		Map<CFGNode, Integer> branchChanges = new HashMap<CFGNode, Integer>(this.branchChanges);
		boolean changed = false;

		/* Is there an updated branch condition? */
		for(CFGEdge edge : node.getEdges()) {
			if(edge.getCondition() != null
				&& Change.convU(edge.getCondition()).le
					== Change.LatticeElement.CHANGED) {
				changed = true;
			}
		}

		/* If a condition was updated, track the branch. */
		if(changed) {
			branchChanges.put(node, node.getEdges().size() - 1);
		}

		return new Control(branchChanges);

	}

	/**
	 * Joins two ControlFlowChanges.
	 * @return The new state (ControlFlowChange) after join.
	 */
	public Control join(Control right) {

		Map<CFGNode, Integer> branchChanges = new HashMap<CFGNode, Integer>(this.branchChanges);

		/* Decrement any nodes that match. */
		for(CFGNode node : right.branchChanges.keySet()) {
			if(branchChanges.containsKey(node)) {
				Integer count = branchChanges.get(node) - 1;
				if(count == 0) branchChanges.remove(node);
				else branchChanges.put(node, count);
			}
			else {
				branchChanges.put(node, right.branchChanges.get(node));
			}
		}

		return new Control(branchChanges);

	}

//	private class BranchChange {
//
//		/** The number of branches from the CFGNode. **/
//		public int branchCount;
//
//		/** Tracks changes to the branch condition leaving CFGNode. **/
//		public Change change;
//
//		public BranchChange(int branchCount, Change change) {
//			this.branchCount = branchCount;
//			this.change = change;
//		}
//
//	}
//

}