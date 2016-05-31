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
import org.mozilla.javascript.ast.InfixExpression;
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
		store = addProp(id, "length", Num.inject(Num.top(Change.u())), external, store, trace);

		InternalFunctionProperties internal = new InternalFunctionProperties(
				Address.inject(StoreFactory.Function_proto_Addr, Change.u()),
				closure,
				JSClass.CFunction);

		store = store.alloc(address, new Obj(external, internal));

		return store;

	}

	/**
	 * Runs a script or function.
	 * @param cfg
	 * @param state
	 * @param selfAddr The binding of 'this' for the call.
	 * @return The state after the script has finished.
	 */
	public static State run(CFG cfg, State state, Address selfAddr) {

		/* For terminating a long running analysis. */
		long edgesVisited = 0;

		/* Initialize the stack for a depth-first traversal. */
		Stack<PathState> stack = new Stack<PathState>();
		for(CFGEdge edge : cfg.getEntryNode().getEdges()) {
			stack.add(new PathState(edge, new HashSet<CFGEdge>(), state));
		}

		/* Set the initial state. */
		cfg.getEntryNode().setState(state);

		/* Break when the analysis time reaches some limit. */
		while(!stack.isEmpty() && edgesVisited < 100000) {

			PathState pathState = stack.pop();
			edgesVisited++;

			System.out.println(pathState.edge.toString());

			/* Join the lattice elements from the current edge and 'from'
			 * node. */
			state = pathState.state.join((State)pathState.edge.getState());
			pathState.edge.setState(state);

			/* Transfer the abstract state over the edge. */
			state = state.transfer(pathState.edge, selfAddr);

			/* Join the abstract states from the 'to' node and the current
			 * edge. */
			state = state.join((State)pathState.edge.getTo().getState());
			pathState.edge.getTo().setState(state);

			/* Transfer the abstract state over the node. */
			state = state.transfer(pathState.edge.getTo(), selfAddr);

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
	 * @param funVal The address(es) of the function object to execute.
	 * @param selfAddr The value of the 'this' identifier (a set of objects).
	 * @param args The address of the arguments object.
	 * @param store The store at the caller.
	 * @param sp Scratchpad memory.
	 * @param trace The trace at the caller.
	 * @return The final state of the closure.
	 */
	public static State applyClosure(BValue funVal, Address selfAddr, Address args,
							  Store store, Scratchpad sp, Trace trace) {

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

			/* Run the function. */
			State endState = ifp.closure.run(selfAddr, args, store, sp, trace);

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
			Change change = Change.conv(localVar);
			Address address = trace.makeAddr(localVar.getID(), "");
			env = env.strongUpdate(new Identifier(localVar.toSource(), Change.conv(localVar)), address);
			store = store.alloc(address, Undefined.inject(Undefined.top(change)));
		}

		/* Get a list of function declarations to lift into the function's environment. */
		List<FunctionNode> children = FunctionLiftVisitor.getFunctionDeclarations(function);
		for(FunctionNode child : children) {
			if(child.getName().isEmpty()) continue; // Not accessible.
			Address address = trace.makeAddr(child.getID(), "");

			/* The function name variable points to our new function. */
			env = env.strongUpdate(new Identifier(child.getName(), Change.conv(child.getFunctionName())), address); // Env update with env change type
			store = store.alloc(address, Address.inject(address, Change.conv(child))); // Store update with value change type

			/* Create a function object. */
			Closure closure = new FunctionClosure(cfgs.get(child), env, cfgs);
			store = createFunctionObj(closure, store, trace, address, child.getID());

		}

		return Pair.of(env, store);

	}

	/**
	 * Resolves a variable to its addresses in the store.
	 * @return The addresses as a BValue.
	 */
	public static BValue resolveValue(Environment env, Store store, AstNode node) {
		BValue value = null;
		Set<Address> addrs = resolve(env, store, node);
		if(addrs == null) return null;
		for(Address addr : addrs) {
			if(value == null) value = store.apply(addr);
			else value = value.join(store.apply(addr));
		}
		return value;
	}

	/**
	 * Resolves a variable to its Address in the store. Follows fields and
	 * function calls as best it can.
	 * @return The value of the variable, function or field, or null if it
	 * 		   cannot be resolved.
	 */
	public static Set<Address> resolve(Environment env, Store store, AstNode node) {

		Set<Address> result = new HashSet<Address>();

		if(node instanceof Name) {

			/* Base case, we have a simple name. */
			Address addr = env.apply(node.toSource());
			if(addr == null) return null;
			result.add(addr);
			return result;

		}
		else if(node instanceof InfixExpression) {

			/* We have a qualified name. Recursively find all the addresses
			 * that lhs can resolve to. */
			InfixExpression ie = (InfixExpression) node;
			Set<Address> lhs = resolve(env, store, ie.getLeft());

			/* We may not have been able to resolve the name. */
			if(lhs == null) return null;

			/* Lookup the current property at each of these addresses. Ignore
			 * type errors and auto-boxing for now. */
			for(Address valAddr : lhs) {

				/* Get the value at the address. */
				BValue val = store.apply(valAddr);

				for(Address objAddr : val.addressAD.addresses) {

					/* Get the Obj from the store. */
					Obj obj = store.getObj(objAddr);

					/* Look up the property. We do not handle the case when the
					 * rhs is an expression. */
					Address propAddr = obj.externalProperties.get(ie.getRight().toSource());
					result.add(propAddr);

				}

			}

			return result;

		}
		else {
			/* Ignore everything else (e.g., method calls) for now. */
			return null;
		}

	}

	/**
	 * Resolves a function's parent object.
	 * @return The parent object (this) and the function object.
	 */
	public static BValue resolveSelf(Environment env, Store store, AstNode node) {
		if(node instanceof Name) {
			/* This is a variable name, not a field. */
			return null;
		}
		else if(node instanceof InfixExpression) {
			/* We have a qualified name. Recursively find the addresses. */
			InfixExpression ie = (InfixExpression) node;
			Set<Address> addrs = resolve(env, store, ie.getLeft());
			Change change = Change.conv(node);
			if(addrs == null) return null;
			return Addresses.inject(new Addresses(addrs, change));
		}
		else {
			/* Ignore everything else (e.g., method calls) for now. */
			return null;
		}
	}

}