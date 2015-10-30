package ca.ubc.ece.salt.pangor.test.cfg;

import java.util.List;

import ca.ubc.ece.salt.pangor.cfg.CFGNode;

public class MockCFGNode {

	public int id;
	public String statementType;
	public List<MockCFGEdge> edges;

	public MockCFGNode(int id, String statementType) {
		this.id = id;
		this.statementType = statementType;
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


}
