package commitminer.analysis.flow.abstractdomain;

import java.util.HashSet;
import java.util.Set;

import org.mozilla.javascript.ast.AstNode;

import commitminer.analysis.annotation.DependencyIdentifier;
import commitminer.cfg.CFGEdge;
import commitminer.cfg.CFGNode;

/**
 * Stores the state of control flow changes due to changes in branch conditions.
 */
public class ControlCondition implements DependencyIdentifier {

	/**
	 * Tracks condition changes for each branch. When the negated branch
	 * condition is encountered, the condition is removed from the set.
	 */
	public Set<AstNode> conditions;

	/**
	 * Tracks control flow changes that have been merged and no longer apply.
	 */
	public Set<AstNode> negConditions;

	public ControlCondition() {
		conditions = new HashSet<AstNode>();
		negConditions = new HashSet<AstNode>();
	}

	public ControlCondition(Set<AstNode> conditions, Set<AstNode> negConditions) {
		this.conditions = conditions;
		this.negConditions = negConditions;
	}

	@Override
	public ControlCondition clone() {
		return new ControlCondition(new HashSet<AstNode>(conditions),
						   new HashSet<AstNode>(negConditions));
	}

	/**
	 * Updates the state for the branch conditions exiting the CFGNode.
	 * @return The new state (ControlFlowChange) after update.
	 */
	public ControlCondition update(CFGEdge edge, CFGNode node) {

		Set<AstNode> conditions = new HashSet<AstNode>(this.conditions);
		Set<AstNode> negConditions = new HashSet<AstNode>(this.negConditions);

		/* Put the current branch condition in the 'conditions' set and all other
		 * conditions in the 'neg' set since they must be false. */

		if(edge.getCondition() != null
				&& Change.convU(edge.getCondition()).le ==
								Change.LatticeElement.CHANGED) {

			conditions.add((AstNode)edge.getCondition());

			for(CFGEdge child : node.getEdges()) {
				if(child != edge && child.getCondition() != null) {
					negConditions.add((AstNode)child.getCondition());
				}
			}

		}

		/* Check the siblings for neg conditions. */
		for(CFGEdge child : node.getEdges()) {
			if(child != edge
					&& child.getCondition() != null
					&& Change.convU(child.getCondition()).le ==
									Change.LatticeElement.CHANGED) {
				negConditions.add((AstNode)child.getCondition());
			}
		}

		return new ControlCondition(conditions, negConditions);

	}

	/**
	 * Joins two ControlFlowChanges.
	 * @return The new state (ControlFlowChange) after join.
	 */
	public ControlCondition join(ControlCondition right) {

		/* Join the sets. */
		Set<AstNode> conditions = new HashSet<AstNode>(this.conditions);
		Set<AstNode> negConditions = new HashSet<AstNode>(this.negConditions);

		conditions.addAll(right.conditions);
		negConditions.addAll(right.negConditions);

		/* conditions = conditions - negConditions */
		conditions.removeAll(negConditions);

		return new ControlCondition(conditions, negConditions);

	}
	
	public boolean isChanged() {
		return !conditions.isEmpty();
	}

	@Override
	public String getAddress() {
		String id = "";
		if(conditions.isEmpty()) return "";
		for(AstNode condition : conditions) {
			id += condition.getID() + ",";
		}
		return id.substring(0, id.length() - 1);
	}

}
