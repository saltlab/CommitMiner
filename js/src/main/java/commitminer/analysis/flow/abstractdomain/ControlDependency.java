package commitminer.analysis.flow.abstractdomain;

import java.util.HashSet;
import java.util.Set;

import org.mozilla.javascript.Token;
import org.mozilla.javascript.ast.AstNode;
import org.mozilla.javascript.ast.ParenthesizedExpression;
import org.mozilla.javascript.ast.UnaryExpression;

import commitminer.analysis.annotation.DependencyIdentifier;
import commitminer.cfg.CFGEdge;
import commitminer.cfg.CFGNode;

/**
 * Tracks the propagation of control dependencies.
 * 
 * This is only used for evaluating MultiDiff against traditional control/data
 * dependency based change impact analysis. It probably doesn't make much sense
 * to use it for code review in practice, unless your goal is to obfuscate the
 * impact of changes and annoy users.
 */
public class ControlDependency implements DependencyIdentifier {
	
	/**
	 * AST node IDs of modified callsites of this method.
	 */
	Set<Integer> callsites;

	/**
	 * Tracks condition changes for each branch. When the negated branch
	 * condition is encountered, the condition is removed from the set.
	 */
	public Set<AstNode> conditions;

	/**
	 * Tracks control flow changes that have been merged and no longer apply.
	 */
	public Set<AstNode> negConditions;
	
	public ControlDependency() {
		this.callsites = new HashSet<Integer>();
		this.conditions = new HashSet<AstNode>();
		this.negConditions = new HashSet<AstNode>();
	}
	
	public ControlDependency(Set<Integer> callsites) {
		this.callsites = callsites;
		this.conditions = new HashSet<AstNode>();
		this.negConditions = new HashSet<AstNode>();
	}

	public ControlDependency(Set<AstNode> conditions, Set<AstNode> negConditions) {
		this.callsites = new HashSet<Integer>();
		this.conditions = conditions;
		this.negConditions = negConditions;
	}
	
	private ControlDependency(Set<Integer> callsites, 
							 Set<AstNode> conditions, 
							 Set<AstNode> negConditions) {
		this.callsites = callsites;
		this.conditions = conditions;
		this.negConditions = negConditions;
	}

	/**
	 * Updates the state for the branch conditions exiting the CFGNode.
	 * @return The new state (ControlFlowChange) after update.
	 */
	public ControlDependency update(CFGEdge edge, CFGNode node) {

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

		return new ControlDependency(conditions, negConditions);

	}
	
	/**
	 * Update the control call domain for a new callsite. We only track 
	 * callsites one level deep.
	 */
	public ControlDependency update(Integer callsite) {
		Set<Integer> callsites = new HashSet<Integer>();
		callsites.add(callsite);
		return new ControlDependency(callsites);
	}

	public ControlDependency join(ControlDependency cc) {
		
		/* Join the callsite sets. */
		Set<Integer> callsites = new HashSet<Integer>(this.callsites);
		callsites.addAll(cc.callsites);
		
		/* Join the condition sets. */
		Set<AstNode> conditions = new HashSet<AstNode>(this.conditions);
		Set<AstNode> negConditions = new HashSet<AstNode>(this.negConditions);

		conditions.addAll(cc.conditions);
		negConditions.addAll(cc.negConditions);

		conditions.removeAll(negConditions); // conditions = conditions - negConditions
		
		return new ControlDependency(callsites, conditions, negConditions);
	}
	
	public boolean isChanged() {
		return !callsites.isEmpty() || !conditions.isEmpty();
	}
	
	@Override
	public boolean equals(Object o) {
		if(!(o instanceof ControlDependency)) return false;
		ControlDependency cc = (ControlDependency)o;
		if(callsites.size() != cc.callsites.size()) return false;
		for(Integer callsite : callsites) {
			if(!cc.callsites.contains(callsite)) return false;
		}
		return true;
	}

	@Override
	public String getAddress() {

		String id = "";
		
		/* Callsite addresses. */
		if(!callsites.isEmpty()) {
			for(Integer callsite : callsites) {
				id += callsite + ",";
			}
		}
		
		/* Condition addresses. */
		if(conditions.isEmpty()) {
			if(id.isEmpty()) return id;
			return id.substring(0, id.length() - 1);
		}
		for(AstNode condition : conditions) {
			/* Get the ID of the non-negated condition. */
			if(condition.getID() == null && condition instanceof UnaryExpression) { 
				UnaryExpression ue = (UnaryExpression)condition;
				if(ue.getOperator() == Token.NOT) id += ((ParenthesizedExpression)ue.getOperand()).getExpression().getID() + ",";
			}
			else {
				id += condition.getID() + ",";
			}
		}

		return id.substring(0, id.length() - 1);

	}

}
