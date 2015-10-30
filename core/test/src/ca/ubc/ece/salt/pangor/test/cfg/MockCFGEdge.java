package ca.ubc.ece.salt.pangor.test.cfg;

import ca.ubc.ece.salt.pangor.cfg.CFGEdge;

public class MockCFGEdge {

	public MockCFGNode from;
	public MockCFGNode to;
	public String branchCondition;

	public MockCFGEdge(MockCFGNode from, MockCFGNode to, String branchCondition) {
		this.from = from;
		this.to = to;
		this.branchCondition = branchCondition;
	}

	@Override
	public boolean equals(Object o) {
		if(o instanceof CFGEdge) {

			CFGEdge edge = (CFGEdge)o;

			/* No branch condition. */
			if(edge.getCondition() == null && this.branchCondition == null) {
				return this.to.equals(edge.getTo())
						&& this.from.equals(edge.getFrom());
			}

			/* Only one has a branch condition. */
			if(edge.getCondition() == null || this.branchCondition == null) return false;

			/* Both have a branch condition. */
			System.out.println("actual=" + edge.getCondition().toString()
					+ "expected=" + this.branchCondition);
			return this.to.equals(edge.getTo())
					&& this.from.equals(edge.getFrom())
					&& this.branchCondition.equals(edge.getCondition().toString());

		}
		return false;
	}

	@Override
	public String toString() {
		return this.from.toString() + "-[" + this.branchCondition + "]->" + this.to.toString();
	}

}
