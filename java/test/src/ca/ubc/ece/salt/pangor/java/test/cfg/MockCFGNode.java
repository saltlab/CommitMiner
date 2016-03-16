package ca.ubc.ece.salt.pangor.java.test.cfg;

import java.util.LinkedList;
import java.util.List;

import ca.ubc.ece.salt.pangor.cfg.CFGNode;

public class MockCFGNode {

	public int id;
	public String statementType;
	public List<MockCFGEdge> edges;

	public MockCFGNode(int id, String statementType) {
		this.id = id;
		this.statementType = statementType;
		this.edges = new LinkedList<MockCFGEdge>();
	}

	public void addEdge(MockCFGEdge edge) {
		this.edges.add(edge);
	}

	@Override
	public boolean equals(Object o) {
		if(o instanceof CFGNode) {
			CFGNode node = (CFGNode)o;
			return this.id == node.getId() && this.statementType.equals(node.getName());
		}
		return false;
	}

	@Override
	public String toString() {
		return this.id + "_" + this.statementType;
	}

}
