package ca.ubc.ece.salt.pangor.analysis.flow.abstractdomain;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;

import org.apache.commons.lang3.tuple.Pair;
import org.mozilla.javascript.ast.AstNode;
import org.mozilla.javascript.ast.FunctionNode;
import org.mozilla.javascript.ast.Name;
import org.mozilla.javascript.ast.ScriptNode;

import ca.ubc.ece.salt.pangor.analysis.flow.factories.FunctionLiftVisitor;
import ca.ubc.ece.salt.pangor.analysis.flow.factories.StoreFactory;
import ca.ubc.ece.salt.pangor.analysis.flow.factories.VariableLiftVisitor;
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

		Map<String, BValue> external = new HashMap<String, BValue>();
		external.put("length", Num.inject(Num.top()));

		InternalFunctionProperties internal = new InternalFunctionProperties(
				Address.inject(StoreFactory.Function_proto_Addr),
				closure,
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

	/**
	 *
	 * @param fun The address(es) of the function object to execute.
	 * @param self The value of 'this' when executing the function.
	 * @param args The address of the arguments object.
	 * @param x The variable to store the return value.
	 * @param env The environment of the caller.
	 * @param store The store at the caller.
	 * @param sp Scratchpad memory.
	 * @param trace The trace at the caller.
	 * @return
	 */
	public State applyClosure(BValue fun, BValue self, BValue args,
							  String x, Environment env, Store store,
							  Scratchpad sp, Trace trace) {

		State state = null;

		/* Get the results for each possible function. */
		for(Address address : fun.addressAD.addresses) {

			/* Get the function object to execute. */
			Obj functObj = store.getObj(address);

			/* Ignore addresses that don't resolve to objects. */
			if(functObj == null || !(functObj.internalProperties
									 instanceof InternalFunctionProperties)) {
				continue;
			}
			InternalFunctionProperties ifp =
					(InternalFunctionProperties)functObj.internalProperties;

			/* Run the function. */
			State endState = ifp.closure.run(self, args, x, env, store, sp, trace);

			/* Join the states. */
			if(state == null) state = endState;
			else state = state.join(endState);

		}

		return state;

	}

	/**
	 * Lifts local variables and function definitions into the environment.
	 * @param env The environment (or closure) in which the function executes.
	 * @param store The current store.
	 * @param function The code we are analyzing.
	 * @param cfgs The control flow graphs for the file. Needed for
	 * 			   initializing lifted functions.
	 * @param trace The program trace including the call site of this function.
	 */
	public static Pair<Environment, Store> lift(Environment env,
										  Store store,
										  ScriptNode function,
										  Map<AstNode, CFG> cfgs,
										  Trace trace ) {
	
		/* Lift variables into the function's environment and initialize to undefined. */
		List<Name> localVars = VariableLiftVisitor.getVariableDeclarations(function);
		for(Name localVar : localVars) {
			Address address = trace.makeAddr(localVar.getID());
			env = env.strongUpdate(localVar.toSource(), new Addresses(address));
			store = store.alloc(address, Undefined.inject(Undefined.top()));
		}
	
		/* Get a list of function declarations to lift into the function's environment. */
		List<FunctionNode> children = FunctionLiftVisitor.getFunctionDeclarations(function);
		for(FunctionNode child : children) {
			if(child.getName().isEmpty()) continue; // Not accessible.
			Address address = trace.makeAddr(child.getID());
			env = env.strongUpdate(child.getName(),  new Addresses(address));
	
			/* Create a function object. */
			Closure closure = new FunctionClosure(cfgs.get(child), env, cfgs);
			store = store.alloc(address, createFunctionObj(closure));
	
			/* The function name variable points to out new function. */
			store = store.alloc(address, Address.inject(address));
		}
	
		return Pair.of(env, store);
	
	}

}
