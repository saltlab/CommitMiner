package ca.ubc.ece.salt.pangor.analysis.flow.abstractdomain;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.Stack;

import ca.ubc.ece.salt.pangor.analysis.flow.factories.StoreFactory;
import ca.ubc.ece.salt.pangor.analysis.flow.trace.Trace;
import ca.ubc.ece.salt.pangor.cfg.CFG;
import ca.ubc.ece.salt.pangor.cfg.CFGEdge;
import ca.ubc.ece.salt.pangor.cfg.CFGNode;

public class Helpers {

	/**
	 * Creates a regular function from a closure stack.
	 * @param closures The stack of closures (code and environments).
	 * @return The function object.
	 */
	public static Obj createFunctionObj(Closure closure) {

		Set<Closure> closures = new HashSet<Closure>();
		closures.add(closure);

		Map<String, BValue> external = new HashMap<String, BValue>();
		external.put("length", Num.inject(Num.top()));

		InternalFunctionProperties internal = new InternalFunctionProperties(
				Address.inject(StoreFactory.Function_proto_Addr),
				closures,
				JSClass.CFunction);

		return new Obj(external, internal, external.keySet());

	}

	/**
	 * Runs a script or function.
	 * @param cfg
	 * @param state
	 * @return The state after the script has finished.
	 */
	public static State run(CFG cfg, State state) {

		/* For terminating a long running analysis. */
		long edgesVisited = 0;

		/* Initialize the stack for a depth-first traversal. */
		Stack<PathState> stack = new Stack<PathState>();
		for(CFGEdge edge : cfg.getEntryNode().getEdges()) {
			stack.add(new PathState(edge, new HashSet<CFGEdge>(), state));
		}

		/* Break when the analysis time reaches some limit. */
		while(!stack.isEmpty() && edgesVisited < 100000) {

			PathState pathState = stack.pop();
			edgesVisited++;

			/* Join the lattice elements from the current edge and 'from'
			 * node. */
			state = pathState.state.join((State)pathState.edge.getState());
			pathState.edge.setState(state);

			/* Transfer the abstract state over the edge. */
			state = state.transfer(pathState.edge);

			/* Join the abstract states from the 'to' node and the current
			 * edge. */
			state = state.join((State)pathState.edge.getTo().getState());
			pathState.edge.getTo().setState(state);

			/* Transfer the abstract state over the node. */
			state = state.transfer(pathState.edge.getTo());

			/* Add all unvisited edges to the stack.
			 * We currently only execute loops once. */
			for(CFGEdge edge : pathState.edge.getTo().getEdges()) {
				if(!pathState.visited.contains(edge)) {
					Set<CFGEdge> newVisited = new HashSet<CFGEdge>(pathState.visited);
					newVisited.add(edge);
					PathState newState = new PathState(edge, newVisited, state);
					stack.push(newState);
				}
			}

		}

		/* Return the merged state of all exit nodes. */
		for(CFGNode exitNode : cfg.getExitNodes()) {
			state = state.join((State)exitNode.getState());
		}

		return state;

	}

	public State applyClosure(BValue fun, BValue self, BValue args,
							  String x, Environment env, Store store,
							  Scratchpad sp, Trace trace) {

		/* Lift variables and function declarations into the environment. */

		/* 'this' should point to 'self' in the environment. */

		/* Match parameters to arguments and update the environment. */

		/* Create the return value x in the scratchpad. */

		/* Run the function. */

		return null;
	}

}
