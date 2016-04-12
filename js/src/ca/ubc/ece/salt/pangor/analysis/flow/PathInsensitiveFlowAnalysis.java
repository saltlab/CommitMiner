package ca.ubc.ece.salt.pangor.analysis.flow;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.Stack;

import org.deri.iris.api.basics.IPredicate;
import org.deri.iris.storage.IRelation;
import org.mozilla.javascript.ast.AstNode;
import org.mozilla.javascript.ast.ScriptNode;

import ca.ubc.ece.salt.pangor.analysis.SourceCodeFileChange;
import ca.ubc.ece.salt.pangor.cfg.CFG;
import ca.ubc.ece.salt.pangor.cfg.CFGEdge;
import ca.ubc.ece.salt.pangor.cfg.CFGNode;
import ca.ubc.ece.salt.pangor.js.analysis.scope.Scope;
import ca.ubc.ece.salt.pangor.pointsto.PointsToPrediction;

/**
 * A fixed point analysis.
 *
 * This does not compute a fixed point because loops are only
 * iterated once (instead of until we reach a fixed point). It's not
 * really necessary to do a proper fixed point analysis right now, but
 * such a change may be necessary for higher precision in the future.
 *
 * @param <LE> The type that stores the analysis information.
 */
public abstract class PathInsensitiveFlowAnalysis<LE extends AbstractLatticeElement> extends FlowAnalysis<LE> {

	/**
	 * Perform a path-sensitive analysis.
	 */
	@Override
	public void analyze(SourceCodeFileChange sourceCodeFileChange,
								 Map<IPredicate, IRelation> facts,
								 CFG cfg, Scope<AstNode> scope,
								 PointsToPrediction model) {

		@SuppressWarnings("unused")
		long pathsComplete = 0;
		long edgesVisited = 0;

		/* Store the lattice elements for each node. */
		Map<CFGNode, LE> leMap = new HashMap<CFGNode, LE>();

		/* Prepare the graph by labeling the nodes with the number of edges
		 * that go through it. */
		this.addNodeCounters(cfg);

		/* Initialize the stack for a depth-first traversal. */
		Stack<PathState> stack = new Stack<PathState>();
		for(CFGEdge edge : cfg.getEntryNode().getEdges()) stack.add(new PathState(edge, this.entryValue((ScriptNode)cfg.getEntryNode().getStatement())));

		/* Break when the number of edges visited reaches some limit. */
		while(!stack.isEmpty() && edgesVisited < 100000) {

			PathState state = stack.pop();
			edgesVisited++;

			/* Transfer over the edge. */
			this.transfer(state.edge, state.le, scope, facts, sourceCodeFileChange);

			/* Join with the lattice element in the node. */
			LE joined = this.join(state.le, leMap.get(state.edge.getTo()));

			/* Store the joined LE in the map. TODO: Is this correct? */
			leMap.put(state.edge.getTo(), joined);

			/* Wait until all the edges have joined to transfer over the node. */
			if(state.edge.getTo().decrementEdges()) {

                /* Transfer over the node. */
                this.transfer(state.edge.getTo(), joined, scope, facts, sourceCodeFileChange);

                /* Push the new edges onto the stack. */
                for(CFGEdge edge : state.edge.getTo().getEdges()) {

                    /* Increment the # of paths visited by one if we're at an exit node. */
                    if(edge.getTo().getEdges().size() == 0) {
                        pathsComplete++;
                    }

                    /* If an edge has been visited on this path, don't visit it
                     * again (only loop once). */
                    if(edge.getCondition() == null || state.le.getVisitedCount(edge) == 0) {
                        LE copy = this.copy(state.le);
                        copy.visit(edge);
                        stack.add(new PathState(edge, copy));
                    }

                }

			}
			/* Traverse any loop edges.
			 * TODO: Right now, we are not allowing the analysis to converge
			 * 		 since we are only visiting the loop edge once. */
			else {

				for(CFGEdge edge : state.edge.getTo().getEdges()) {
					if(edge.loopEdge) {

                        /* Transfer over the node. */
						LE copy = this.copy(joined);
                        this.transfer(state.edge.getTo(), copy, scope, facts, sourceCodeFileChange);

	                    /* If an edge has been visited on this path, don't visit it
	                     * again (only loop once). */
	                    if(state.le.getVisitedCount(edge) == 0) {
	                        copy.visit(edge);
	                        stack.add(new PathState(edge, copy));
	                    }

					}
				}

			}


		}

	}

	/**
	 * Join two lattice elements.
	 * @param left
	 * @param right
	 * @return The joined lattice element.
	 */
	protected abstract LE join(LE left, LE right);

	/**
	 * Increments the node counters by one each time the node is visited
	 * (counts the number of edges that go into the node).
	 * @param cfg
	 */
	private void addNodeCounters(CFG cfg) {

		Set<CFGNode> visited = new HashSet<CFGNode>();

		/* Initialize the stack for a depth-first traversal. */
		Stack<CFGNode> stack = new Stack<CFGNode>();
		stack.push(cfg.getEntryNode());
		visited.add(cfg.getEntryNode());

		while(!stack.isEmpty()) {

			CFGNode node = stack.pop();

			/* Push the new edges onto the stack. */
			for(CFGEdge edge : node.getEdges()) {

				/* There is another edge that goes to the node. */
				edge.getTo().incrementEdges();

				/* Visit the node if it hasn't been visited. */
				if(!visited.contains(edge.getTo())) {
					stack.push(edge.getTo());
					visited.add(edge.getTo());
				}

			}

		}

	}

	/**
	 * Stores the state of the analysis.
	 *
	 * This is needed for a path-sensitive analysis. When we traverse the graph
	 * we keep track of the lattice element for a given path we are traversing
	 * and the next edge to transfer over using this clas.
	 */
	private class PathState {

		public CFGEdge edge;
		public LE le;

		public PathState (CFGEdge edge, LE le) {
			this.edge = edge;
			this.le = le;
		}

	}

}
