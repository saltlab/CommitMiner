package ca.ubc.ece.salt.pangor.analysis.flow.abstractdomain;

import java.util.Set;

import ca.ubc.ece.salt.pangor.cfg.CFGEdge;

/**
 * Stores the state of the flow analysis for each path.
 */
class PathState {

	public CFGEdge edge;
	public Set<CFGEdge> visited;
	public State state;

	public PathState (CFGEdge edge, Set<CFGEdge> visited, State state) {
		this.edge = edge;
		this.visited = visited;
		this.state = state;
	}

}