package ca.ubc.ece.salt.pangor.test.cfg;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Set;

import org.apache.commons.lang3.tuple.Pair;

import ca.ubc.ece.salt.pangor.cfg.CFG;
import ca.ubc.ece.salt.pangor.cfg.CFGEdge;
import ca.ubc.ece.salt.pangor.cfg.CFGNode;

/**
 * Use for asserting two CFGs (the actual and the expected) are equivalent in
 * {@code CFGFactory} test cases.
 */
public class MockCFG {

	private MockCFGNode entryNode;
	private boolean print;

	public MockCFG(MockCFGNode entryNode, boolean print) {
		this.entryNode = entryNode;
		this.print = print;
	}

	public MockCFGNode getEntryNode() {
		return this.entryNode;
	}

	@Override
	public boolean equals(Object o) {

		if(o instanceof CFG) {
			CFG cfg = (CFG)o;
			return isEquivalent(cfg, this);
		}

		return false;

	}

	/**
	 * @param actualCFG The actual CFG produced by the CFGFactory.
	 * @param expectedCFG The expected CFG defined by the test case.
	 * @return {@code true} if the CFGs are equivalent.
	 */
	public boolean isEquivalent(CFG actualCFG, MockCFG expectedCFG) {

		/* Breadth-first search to check equivalence. */
		Queue<Pair<CFGNode, MockCFGNode>> queue = new LinkedList<Pair<CFGNode, MockCFGNode>>();
		Set<CFGNode> visited = new HashSet<CFGNode>();

		/* Make sure the starting nodes are equivalent. */
		if(!expectedCFG.getEntryNode().equals(actualCFG.getEntryNode())) {
			if(this.print)
				System.out.println("actual=" + actualCFG.getEntryNode().toString()
						+ ", expected=" + expectedCFG.getEntryNode().toString());
			return false;
		}

		queue.add(Pair.of(actualCFG.getEntryNode(), expectedCFG.getEntryNode()));
		visited.add(actualCFG.getEntryNode());

		/* BFS until we have inspected all nodes in the graph. */
		while(!queue.isEmpty()) {

			Pair<CFGNode, MockCFGNode> pair = queue.remove();
			CFGNode actual = pair.getLeft();
			MockCFGNode expected = pair.getRight();

			if(this.print)
				System.out.println("actual=" + actual.toString() + ", expected=" + expected.toString());

			/* Check that all the edges of actual and expected are the same. */
			for(CFGEdge actualEdge : actual.getEdges()) {

				/* Find the equivalent edge in the expected. */
				MockCFGEdge expectedEdge = getExpectedEdge(actualEdge, expected.edges);

				/* If there is no matching edge, the graphs are not equivalent. */
				if(expectedEdge == null) {
					if(this.print) {
						System.out.println("actual=" + actualEdge.toString());
						for(MockCFGEdge e : expected.edges) {
							System.out.println("expected=" + e.toString());
						}

					}
					return false;
				}

				if(!visited.contains(expectedEdge.to)) {
					/* Add the next node to the queue to be explored. */
					queue.add(Pair.of(actualEdge.getTo(), expectedEdge.to));
				}

			}


		}

		return true;

	}

	/**
	 * @param actualEdge An edge from the current node in the actual CFG.
	 * @param expectedEdges The edges from the current node in the expected CFG.
	 * @return the edge in the expected CFG that matches the edge in the
	 * 		   actual CFG.
	 */
	private static MockCFGEdge getExpectedEdge(CFGEdge actualEdge, List<MockCFGEdge> expectedEdges) {

		/* Find the equivalent edge in the expected. */
		for(MockCFGEdge expectedEdge : expectedEdges) {
			if(expectedEdge.equals(actualEdge)) {
				return expectedEdge;
			}
		}

		/* Expected edge was not found. */
		return null;

	}

}
