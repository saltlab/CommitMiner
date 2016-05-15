package ca.ubc.ece.salt.pangor.cfg;

import ca.ubc.ece.salt.gumtree.ast.ClassifiedASTNode;
import ca.ubc.ece.salt.gumtree.ast.ClassifiedASTNode.ChangeType;
import ca.ubc.ece.salt.pangor.analysis.flow.IState;

/**
 * A labeled, directed edge to another node.
 */
public class CFGEdge {

	/** The unique id for this node. **/
	private int id;

	/** The condition in which this edge is traversed. If null then the edge
	 * is always traversed. **/
	private ClassifiedASTNode condition;

	/** The node that this edge exits. */
	private CFGNode from;

	/** The node that this edge points to. */
	private CFGNode to;

	/** The change operation applied to the edge from source to destination. **/
	public ChangeType changeType;

	/**
	 * The state of the environment and store. Note that at this point the
	 * state has not yet been transfered over the term (statement) in this
	 * node. The state is language dependent.
	 */
	private IState state;

	public CFGEdge(ClassifiedASTNode condition, CFGNode from, CFGNode to, int id) {
		this.condition = condition;
		this.to = to;
		this.from = from;
		this.changeType = ChangeType.UNKNOWN;
		this.id = id;
	}

	public CFGEdge(ClassifiedASTNode condition, CFGNode from, CFGNode to, boolean loopEdge, int id) {
		this.condition = condition;
		this.to = to;
		this.from = from;
		this.changeType = ChangeType.UNKNOWN;
		this.id = id;
	}

	/**
	 * Accepts and runs a visitor.
	 */
	public void accept(ICFGVisitor visitor) {
		visitor.visit(this);
	}

	/**
	 * Set the abstract state at this point in the program.
	 * @param as The abstract state.
	 */
	public void setState(IState state) {
		this.state = state;
	}

	/**
	 * @return the lattice element at this point in the program.
	 */
	public IState getState() {
		return this.state;
	}

	/**
	 * @return a shallow copy of the edge.
	 */
	public CFGEdge copy() {
		return new CFGEdge(this.condition, this.from, this.to, this.id);
	}

	/**
	 * @param to the node this edge enters.
	 */
	public void setTo(CFGNode to) {
		this.to = to;
	}

	/**
	 * @return the node this edge enters.
	 */
	public CFGNode getTo() {
		return to;
	}

	/**
	 * @param from the node this edge exits.
	 */
	public void setFrom(CFGNode from) {
		this.from = from;
	}

	/**
	 * @return the node this edge exits.
	 */
	public CFGNode getFrom() {
		return from;
	}

	/**
	 * @param condition the condition for which this edge is traversed.
	 */
	public void setCondition(ClassifiedASTNode condition) {
		this.condition = condition;
	}

	/**
	 * @return the condition for which this edge is traversed.
	 */
	public ClassifiedASTNode getCondition() {
		return condition;
	}

	/**
	 * @return the unique ID for the edge.
	 */
	public int getId() {
		return id;
	}

//	@Override
//	public boolean equals(Object o) {
//		if(o instanceof CFGEdge) {
//			return ((CFGEdge)o).condition == this.condition;
//		}
//		return false;
//	}
//
//	@Override
//	public int hashCode() {
//		return new Long(this.id).hashCode();
//	}

	@Override
	public String toString() {
		return this.from.toString() + "-[" + this.condition + "]->" + this.to.toString();
	}

}