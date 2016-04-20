package ca.ubc.ece.salt.pangor.analysis.flow;

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
import ca.ubc.ece.salt.pangor.js.analysis.FunctionAnalysis;
import ca.ubc.ece.salt.pangor.js.analysis.scope.Scope;
import ca.ubc.ece.salt.pangor.pointsto.PointsToPrediction;

/**
 * Performs a change-sensitive, intra-procedural analysis.
 *
 * This provides the analysis framework and the current scope.
 *
 * Loops are executed once.
 *
 * NOTE: This class only works with the Mozilla Rhino IAbstractStateT.
 *
 * @param <IAbstractState> The abstract state that stores the analysis information.
 */
public abstract class FlowAnalysis extends FunctionAnalysis {

	/**
	 * @param function The function under analysis.
	 * @return an initialized lattice element for the function.
	 */
	public abstract IAbstractState entryValue(ScriptNode function);

	public abstract void createFacts(SourceCodeFileChange sourceCodeFileChange,
			Map<IPredicate, IRelation> facts,
			CFG cfg, Scope<AstNode> scope,
			PointsToPrediction model);

	@Override
	public void analyze(SourceCodeFileChange sourceCodeFileChange,
								 Map<IPredicate, IRelation> facts,
								 CFG cfg, Scope<AstNode> scope,
								 PointsToPrediction model) {

		/* For terminating a long running analysis. */
		long edgesVisited = 0;

		/* Initialize the stack for a depth-first traversal. */
		Stack<PathState> stack = new Stack<PathState>();
		IAbstractState initialState = this.entryValue((ScriptNode)cfg.getEntryNode().getStatement());
		for(CFGEdge edge : cfg.getEntryNode().getEdges()) {
			stack.add(new PathState(edge, new HashSet<CFGEdge>(), initialState));
		}

		/* Break when the analysis time reaches some limit. */
		while(!stack.isEmpty() && edgesVisited < 100000) {

			PathState state = stack.pop();
			edgesVisited++;

			/* Join the lattice elements from the current edge and 'from'
			 * node. */
			IAbstractState as = state.le.join(state.edge.getAS());
			state.edge.setAS(as);

			/* Transfer the abstract state over the edge. */
			as = as.transfer(state.edge);

			/* Join the abstract states from the 'to' node and current
			 * edge. */
			as = as.join(state.edge.getTo().getAS());
			state.edge.getTo().setAS(as);

			/* Add all unvisited edges to the stack.
			 * We currently only execute loops once. */
			for(CFGEdge edge : state.edge.getTo().getEdges()) {
				if(!state.visited.contains(edge)) {
					Set<CFGEdge> newVisited = new HashSet<CFGEdge>(state.visited);
					newVisited.add(edge);
					PathState newState = new PathState(edge, newVisited, as);
					stack.push(newState);
				}
			}

		}

		/* Generate facts from the LatticeElements in the CFG. */
		this.createFacts(sourceCodeFileChange, facts, cfg, scope, model);

	}

	/**
	 * Stores the state of the flow analysis for each path.
	 */
	private class PathState {

		public CFGEdge edge;
		public Set<CFGEdge> visited;
		public IAbstractState le;

		public PathState (CFGEdge edge, Set<CFGEdge> visited, IAbstractState le) {
			this.edge = edge;
			this.visited = visited;
			this.le = le;
		}

	}

}