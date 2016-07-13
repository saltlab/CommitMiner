package ca.ubc.ece.salt.pangor.cfg;

import java.util.LinkedList;
import java.util.List;

import ca.ubc.ece.salt.gumtree.ast.ClassifiedASTNode;
import ca.ubc.ece.salt.pangor.analysis.flow.IState;

/**
 * A control flow graph node and abstract state for flow analysis.
 */
public class CFGNode {

	/** The unique id for this node. **/
	private int id;

	/** Optional name for this node. **/
	private String name;

	/** The AST Statement which contains the actions this node performs. From
	 * org.mozilla.javascript.Token. **/
	private ClassifiedASTNode statement;

	/** The edges leaving this node. **/
	private List<CFGEdge> edges;

	/** The corresponding source or destination CFGNode. */
	private CFGNode mappedNode;

	/** The number of edges that point to this node. **/
	private int incommingEdges;

	/**
	 * A semaphore for tracking how many times this node has been visited
	 * relative to the number of incomming edges.
	 */
	private int visitedSemaphore;

	/**
	 * The state of the environment and store before transferring over the
	 * term (statement). The state is language dependent.
	 */
	private IState beforeState;

	/**
	 * The state of the environment and store after transferring over the
	 * term (statement). The state is language dependent.
	 */
	private IState afterState;

	/**
	 * @param statement The statement that is executed when this node is
	 * 		  			reached.
	 */
	public CFGNode(ClassifiedASTNode statement, int id) {
		this.edges = new LinkedList<CFGEdge>();
		this.statement = statement;
		this.id = id;
		this.name = null;
		this.setMappedNode(null);
		this.beforeState = null;
		this.afterState = null;
		this.incommingEdges = 0;
	}

	/**
	 * @param statement The statement that is executed when this node is
	 * 		  			reached.
	 * @param name The name for this node (nice for printing and debugging).
	 */
	public CFGNode(ClassifiedASTNode statement, String name, int id) {
		this.edges = new LinkedList<CFGEdge>();
		this.statement = statement;
		this.id = id;
		this.name = name;
		this.beforeState = null;
		this.afterState = null;
		this.incommingEdges = 0;
	}

	/**
	 * Accepts and runs a visitor.
	 */
	public void accept(ICFGVisitor visitor) {
		visitor.visit(this);
	}

	/**
	 * Set the lattice element at this point in the program.
	 * @param as The abstract state.
	 */
	public void setBeforeState(IState state) {
		this.beforeState = state;
	}

	/**
	 * @return the abstract state at this point in the program.
	 */
	public IState getBeforeState() {
		return this.beforeState;
	}

	/**
	 * Set the lattice element at this point in the program.
	 * @param as The abstract state.
	 */
	public void setAfterState(IState state) {
		this.afterState = state;
	}

	/**
	 * @return the abstract state at this point in the program.
	 */
	public IState getAfterState() {
		return this.afterState;
	}

	/**
	 * Increments the number of edges that point to this node by one.
	 */
	public void incrementIncommingEdges() {
		this.incommingEdges++;
	}

	/**
	 * @return The number of edges that point to this node.
	 */
	public int getIncommingEdges() {
		return this.incommingEdges;
	}

	/**
	 * Resets the semaphore for tracking how many times the node has been
	 * visited.
	 */
	public void resetVisited() {
		this.visitedSemaphore = this.incommingEdges;
	}

	/**
	 * Decrements the visited semaphore by one. Indicates that this node has
	 * been visited through a new edge.
	 */
	public void visited() {
		this.visitedSemaphore--;
	}


	/**
	 * @return true if this node has been visited through all incoming edges.
	 * 				If true, this means the CFG traversal can move on to this
	 * 				node's outgoing nodes.
	 */
	public boolean isVisited() {
		if(this.visitedSemaphore <= 0) return true;
		return false;
	}

	/**
	 * @return true if this node already contains an edge with this condition.
	 */
	private CFGEdge getEdge(ClassifiedASTNode condition) {
		for(CFGEdge existing : this.edges) {
			if(condition == existing.getCondition()) return existing;
		}
		return null;
	}

	/**
	 * Add an edge to this node. If an edge with the same condition already
	 * exists, that edge will be overwritten.
	 * @param condition The condition for which we traverse the edge.
	 * @param node The node at the other end of this edge.
	 */
	public void addEdge(ClassifiedASTNode condition, CFGNode node, int id) {
		CFGEdge edge = this.getEdge(condition);

		if(edge != null)  {
			edge.setTo(node);
		}
		else {
			edge = new CFGEdge(condition, this, node, id);
            this.edges.add(new CFGEdge(condition, this, node, id));
		}
	}

	/**
	 * Add an edge to this node. If an edge with the same condition already
	 * exists, that edge will be overwritten.
	 * @param condition The condition for which we traverse the edge.
	 * @param node The node at the other end of this edge.
	 */
	public void addEdge(ClassifiedASTNode condition, CFGNode node, boolean loopEdge, int id) {
		CFGEdge edge = this.getEdge(condition);

		if(edge != null)  {
			edge.setTo(node);
		}
		else {
			edge = new CFGEdge(condition, this, node, id);
            this.edges.add(new CFGEdge(condition, this, node, loopEdge, id));
		}
	}

	/**
	 * Add an edge to this node. If an edge with the same condition already
	 * exists, that edge will be overwritten.
	 * @param edge The edge to add.
	 */
	public void addEdge(CFGEdge edge) {
		CFGEdge existing = this.getEdge(edge.getCondition());

		if(existing != null)  {
			existing.setTo(edge.getTo());
		}
		else {
            this.edges.add(edge);
		}
	}

	/**
	 * @return The nodes pointed to by this node.
	 */
	public List<CFGNode> getAdjacentNodes() {
		List<CFGNode> nodes = new LinkedList<CFGNode>();
		for(CFGEdge edge : this.edges) nodes.add(edge.getTo());
		return nodes;
	}

	/**
	 * @return The edges leaving this node.
	 */
	public List<CFGEdge> getEdges() {
		return this.edges;
	}

	/**
	 * @param edges The new edges for the node.
	 */
	public void setEdges(List<CFGEdge> edges) {
		this.edges = edges;
	}

	/**
	 * @return The AST Statement which contains the actions this node performs.
	 */
	public ClassifiedASTNode getStatement() {
		return statement;
	}

	/**
	 * @param statement The AST Statement which contains the actions this node
	 * 					performs.
	 */
	public void setStatement(ClassifiedASTNode statement) {
		this.statement = statement;
	}

	/**
	 * @return The unique ID for this node.
	 */
	public int getId() {
		return id;
	}

	/**
	 * @return the corresponding node in the source or destination CFG.
	 */
	public CFGNode getMappedNode() {
		return mappedNode;
	}

	/**
	 * @param mappedNode the corresponding node in the source or destination CFG.
	 */
	public void setMappedNode(CFGNode mappedNode) {
		this.mappedNode = mappedNode;
	}

	public String getName() {

		if(this.name != null) return this.name;

		return this.statement.getASTNodeType();

	}

	/**
	 * Make a copy of the given node.
	 * @param node The node to copy.
	 * @return A shallow copy of the node. The condition AST and edge CFGs will
	 * 		   be the same as the original.
	 */
	public static CFGNode copy(CFGNode node) {
        CFGNode newNode = new CFGNode(node.getStatement(), node.getId());
        for(CFGEdge edge : node.getEdges()) newNode.addEdge(edge.getCondition(), edge.getTo(), edge.getId());
        return newNode;
	}

	@Override
	public String toString() {
		return this.id + "_" + this.getName();
	}

}
