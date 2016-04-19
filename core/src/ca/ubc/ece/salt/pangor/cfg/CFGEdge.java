package ca.ubc.ece.salt.pangor.cfg;

import ca.ubc.ece.salt.gumtree.ast.ClassifiedASTNode;
import ca.ubc.ece.salt.gumtree.ast.ClassifiedASTNode.ChangeType;
import ca.ubc.ece.salt.pangor.analysis.flow.IAbstractState;

/**
 * A labeled, directed edge to another node.
 */
public class CFGEdge {

	/** Unique IDs for nodes. **/
	private static long idGen = 0;

	/** The unique id for this node. **/
	private long id;

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
	 * The lattice element at this point in the program. The LE has not yet
	 * been transfered over the edge. The element will be converted to a fact
	 * about this point in the program once the analysis has finished.
	 */
	private IAbstractState as;

	public CFGEdge(ClassifiedASTNode condition, CFGNode from, CFGNode to) {
		this.condition = condition;
		this.to = to;
		this.from = from;
		this.changeType = ChangeType.UNKNOWN;
		this.id = CFGEdge.getUniqueId();
	}

	public CFGEdge(ClassifiedASTNode condition, CFGNode from, CFGNode to, boolean loopEdge) {
		this.condition = condition;
		this.to = to;
		this.from = from;
		this.changeType = ChangeType.UNKNOWN;
		this.id = CFGEdge.getUniqueId();
	}

	/**
	 * Set the abstract state at this point in the program.
	 * @param as The abstract state.
	 */
	public void setLE(IAbstractState as) {
		this.as = as;
	}

	/**
	 * @return the lattice element at this point in the program.
	 */
	public IAbstractState getLE() {
		return this.as;
	}

	/**
	 * @return a shallow copy of the edge.
	 */
	public CFGEdge copy() {
		return new CFGEdge(this.condition, this.from, this.to);
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
	public long getId() {
		return id;
	}

	/**
	 * @return A unique ID for a new edge
	 */
	private static synchronized long getUniqueId() {
		long id = CFGEdge.idGen;
		CFGEdge.idGen++;
		return id;
	}

	/**
	 * Reset the ID generator value. Needed in between test cases.
	 */
	public static synchronized void resetIdGen() {
		CFGEdge.idGen = 0;
	}

	@Override
	public boolean equals(Object o) {
		if(o instanceof CFGEdge) {
			return ((CFGEdge)o).condition == this.condition;
		}
		return false;
	}

	@Override
	public int hashCode() {
		assert false : "hashCode not designed";
		return 42; // any arbitrary constant will do
	}

	@Override
	public String toString() {
		return this.from.toString() + "-[" + this.condition + "]->" + this.to.toString();
	}

}