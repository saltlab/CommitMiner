package ca.ubc.ece.salt.pangor.test.cfg;

import ca.ubc.ece.salt.pangor.cfg.CFGEdge;

public class MockCFGEdge {

	public MockCFGNode to;
	public MockCFGNode from;
	public String branchCondition;

	public MockCFGEdge(MockCFGNode to, MockCFGNode from, String branchCondition) {
		this.to = to;
		this.from = from;
		this.branchCondition = branchCondition;
	}

	@Override
	public boolean equals(Object o) {
		if(o instanceof CFGEdge) {
			CFGEdge edge = (CFGEdge)o;
			return this.to.equals(edge.getTo())
					&& this.from.equals(edge.getFrom())
					&& this.branchCondition.equals(edge.getCondition().toString());
		}
		return false;
	}

}
