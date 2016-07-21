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

import ca.ubc.ece.salt.pangor.analysis.flow.factories.StoreFactory;
import ca.ubc.ece.salt.pangor.analysis.flow.trace.Trace;
import ca.ubc.ece.salt.pangor.cfg.CFG;
import ca.ubc.ece.salt.pangor.cfg.CFGEdge;
import ca.ubc.ece.salt.pangor.cfg.CFGNode;

public class Helpers {

	/**
	 * Adds a property to the object and allocates the property's value on the
	 * store.
	 * @param prop The name of the property to add to the object.
	 */
	public static Store addProp(String prop, BValue propVal, Map<String, Address> ext, Store store, Trace trace) {
		Address propAddr = trace.toAddr(prop);
		store = store.alloc(propAddr, propVal);
		ext.put(prop, propAddr);
		return store;
	}

	/**
	 * Adds a property to the object and allocates the property's value on the
	 * store.
	 * @param propID The node id of the property being added to the object.
	 * @param propVal The value of the property.
	 */
	public static Store addProp(int propID, String prop, BValue propVal, Map<Identifier, Address> ext, Store store, Trace trace) {
		Address propAddr = trace.makeAddr(propID, prop);
		store = store.alloc(propAddr, propVal);
		ext.put(new Identifier(prop, Change.u()), propAddr);
		return store;
	}

	/**
	 * Creates and allocates a regular function from a closure stack.
	 * @param closures The closure for the function.
	 * @return The function object.
	 */
	public static Store createFunctionObj(Closure closure, Store store, Trace trace, Address address, int id) {

		Map<Identifier, Address> external = new HashMap<Identifier, Address>();
		store = addProp(id, "length", Num.inject(Num.top(Change.u()), Change.u()), external, store, trace);

		InternalFunctionProperties internal = new InternalFunctionProperties(
				Address.inject(StoreFactory.Function_proto_Addr, Change.u(), Change.u()),
				closure,
				JSClass.CFunction);

		store = store.alloc(address, new Obj(external, internal));

		return store;

	}

	/**
	 * Runs a script or function by traversing the CFG.
	 * @param cfg
	 * @param state
	 * @return The state after the script has finished.
	 */
	public static State run(CFG cfg, State state) {

		/* Merge state with prior state if needed. */
		State initState = (State)cfg.getEntryNode().getBeforeState();
		if(initState != null) state = state.join(initState);

		/* For terminating a long running analysis. */
		long edgesVisited = 0;

		/* Stores semaphores for tracking the number of incoming edges that
		 * have been traversed to a node. Stands for "I(ncoming) E(dges)
		 * S{emaphore} Map. */
		Map<CFGNode, Integer> iesMap = new HashMap<CFGNode, Integer>();

		/* TODO: We need to do two things:
		 * 	1. Compute the number of incoming edges for each node in the CFG...
		 * 	   which we probably don't want to do here.
		 * 	2. Change to a breadth-first traversal (probably more efficient in
		 * 	   practice).
		 * 	3. Do not transfer over a node if the semaphore for the node != 0.
		 */

		/* Initialize the stack for a depth-first traversal. */
		Stack<PathState> stack = new Stack<PathState>();
		for(CFGEdge edge : cfg.getEntryNode().getEdges()) {
			stack.add(new PathState(edge, new HashSet<CFGEdge>(), state));
		}

		/* Set the initial state. */
		cfg.getEntryNode().setBeforeState(state);
		cfg.getEntryNode().setAfterState(state);

		/* Break when the analysis time reaches some limit. */
		while(!stack.isEmpty() && edgesVisited < 100000) {

			PathState pathState = stack.pop();
			edgesVisited++;

			System.out.println(pathState.edge.toString());

			/* Join the lattice elements from the current edge and 'from'
			 * node. */
			state = pathState.state.join((State)pathState.edge.getBeforeState());
			pathState.edge.setBeforeState(state);

			/* Transfer the abstract state over the edge. */
			state = state.clone().transfer(pathState.edge);
			pathState.edge.setAfterState(state);

			/* Join the abstract states from the 'to' node and the current
			 * edge. */
			state = state.join((State)pathState.edge.getTo().getBeforeState());
			pathState.edge.getTo().setBeforeState(state);

			/* Look up the number of times this node has been visited in the
			 * visitedSemaphores map. */
			Integer semVal = iesMap.get(pathState.edge.getTo());

			/* If it does not exist, put it in the map and initialize the
			 * semaphore value to the number of incoming edges for the node. */
			if(semVal == null) semVal = pathState.edge.getTo().getIncommingEdges();

			/* Decrement the semaphore by one since we visited the node. */
			semVal = semVal - 1;
			iesMap.put(pathState.edge.getTo(), semVal);

			/* Transfer the abstract state over the node. */
			state = state.clone().transfer(pathState.edge.getTo());
			pathState.edge.getTo().setAfterState(state);

			/* Add all unvisited edges to the stack.
			 * We currently only execute loops once. */
			for(CFGEdge edge : pathState.edge.getTo().getEdges()) {

				/* Only visit an edge if the semaphore for the node is zero or if one of the
				* edges is a loop edge. */
				if(!pathState.visited.contains(edge)
						&& (semVal == 0 || edge.isLoopEdge)) {
					Set<CFGEdge> newVisited = new HashSet<CFGEdge>(pathState.visited);
					newVisited.add(edge);
					PathState newState = new PathState(edge, newVisited, state);
					stack.push(newState);
				}

			}

		}

		/* Return the merged state of all exit nodes. */
		for(CFGNode exitNode : cfg.getExitNodes()) {
			state = state.join((State)exitNode.getBeforeState());
		}

		return state;

	}

	/**
	 * @param funVal The address(es) of the function object to execute.
	 * @param selfAddr The value of the 'this' identifier (a set of objects).
	 * @param args The address of the arguments object.
	 * @param store The store at the caller.
	 * @param sp Scratchpad memory.
	 * @param trace The trace at the caller.
	 * @return The final state of the closure.
	 */
	public static State applyClosure(BValue funVal, Address selfAddr, Address args,
							  Store store, Scratchpad sp, Trace trace, Control control,
							  Stack<Address> callStack) {

		State state = null;

		/* Get the results for each possible function. */
		for(Address address : funVal.addressAD.addresses) {

			/* Get the function object to execute. */
			Obj functObj = store.getObj(address);

			/* Ignore addresses that don't resolve to objects. */
			if(functObj == null || !(functObj.internalProperties
									 instanceof InternalFunctionProperties)) {
				continue;
			}
			InternalFunctionProperties ifp =
					(InternalFunctionProperties)functObj.internalProperties;

			/* Is this function being called recursively? If so abort. */
			if(callStack.contains(address)) return state;

			/* Push this function onto the call stack. */
			callStack.push(address);

			/* Run the function. */
			State endState = ifp.closure.run(selfAddr, args, store, sp, trace, control, callStack);

			/* Pop this function off the call stack. */
			callStack.pop();

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
			Change change = Change.convU(localVar);
			Address address = trace.makeAddr(localVar.getID(), "");
			env = env.strongUpdate(new Identifier(localVar.toSource(), Change.convU(localVar)), address);
			store = store.alloc(address, Undefined.inject(Undefined.top(change), Change.u()));
		}

		/* Get a list of function declarations to lift into the function's environment. */
		List<FunctionNode> children = FunctionLiftVisitor.getFunctionDeclarations(function);
		for(FunctionNode child : children) {
			if(child.getName().isEmpty()) continue; // Not accessible.
			Address address = trace.makeAddr(child.getID(), "");

			/* The function name variable points to our new function. */
			env = env.strongUpdate(new Identifier(child.getName(), Change.convU(child.getFunctionName())), address); // Env update with env change type
			store = store.alloc(address, Address.inject(address, Change.convU(child), Change.convU(child))); // Store update with value change type

			/* Create a function object. */
			Closure closure = new FunctionClosure(cfgs.get(child), env, cfgs);
			store = createFunctionObj(closure, store, trace, address, child.getID());

		}

		return Pair.of(env, store);

	}

}