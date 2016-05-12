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
import ca.ubc.ece.salt.pangor.analysis.flow.factories.VariableLiftVisitor;
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
	public static void addProp(String prop, BValue propVal, Map<String, Address> ext, Store store, Trace trace) {
		Address propAddr = trace.toAddr(prop);
		store.alloc(propAddr, propVal);
		ext.put(prop, propAddr);
	}

	/**
	 * Adds a property to the object and allocates the property's value on the
	 * store.
	 * @param propID The node id of the property being added to the object.
	 * @param propVal The value of the property.
	 */
	public static void addProp(int propID, String prop, BValue propVal, Map<String, Address> ext, Store store, Trace trace) {
		Address propAddr = trace.makeAddr(propID);
		store.alloc(propAddr, propVal);
		ext.put(prop, propAddr);
	}

	/**
	 * Creates a regular function from a closure stack.
	 * @param closures The closure for the function.
	 * @return The function object.
	 */
	public static Obj createFunctionObj(Closure closure, Store store, Trace trace, int id) {

		Map<String, Address> external = new HashMap<String, Address>();
		addProp(id, "length", Num.inject(Num.top()), external, store, trace);

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
	 * @param self The binding of 'this' for the call.
	 * @return The state after the script has finished.
	 */
	public static State run(CFG cfg, State state, BValue self) {

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
			state = state.transfer(pathState.edge, self);

			/* Join the abstract states from the 'to' node and the current
			 * edge. */
			state = state.join((State)pathState.edge.getTo().getState());
			pathState.edge.getTo().setState(state);

			/* Transfer the abstract state over the node. */
			state = state.transfer(pathState.edge.getTo(), self);

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
	 * @param fun The address(es) of the function object to execute.
	 * @param self The value of 'this' when executing the function.
	 * @param args The address of the arguments object.
	 * @param store The store at the caller.
	 * @param sp Scratchpad memory.
	 * @param trace The trace at the caller.
	 * @return The final state of the closure.
	 */
	public static State applyClosure(BValue fun, BValue self, Address args,
							  Store store, Scratchpad sp, Trace trace) {

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
			State endState = ifp.closure.run(self, args, store, sp, trace);

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
			env = env.strongUpdate(localVar.toSource(), address);
			store = store.alloc(address, Undefined.inject(Undefined.top()));
		}

		/* Get a list of function declarations to lift into the function's environment. */
		List<FunctionNode> children = FunctionLiftVisitor.getFunctionDeclarations(function);
		for(FunctionNode child : children) {
			if(child.getName().isEmpty()) continue; // Not accessible.
			Address address = trace.makeAddr(child.getID());

			/* The function name variable points to out new function. */
			store = store.alloc(address, Address.inject(address));
			env = env.strongUpdate(child.getName(), address);

			/* Create a function object. */
			Closure closure = new FunctionClosure(cfgs.get(child), env, cfgs);
			store = store.alloc(address, createFunctionObj(closure, store, trace, child.getID()));

		}

		return Pair.of(env, store);

	}

	/**
	 * Resolves a variable to its BValue in the store. Follows fields and
	 * function calls as best it can.
	 * @return The BValue for the variable, function or field.
	 */
	public static BValue resolve(Environment env, Store store, AstNode node) {
		BValue result = null;
		if(node instanceof Name) {
			/* Base case, we have a simple name. */
			Address address = env.apply(node.toSource());
			if(address == null) return BValue.top();
			return store.apply(address);
		}
		else if(node instanceof InfixExpression) {
			/* We have a qualified name. Recursively find the addresses. */
			InfixExpression ie = (InfixExpression) node;
			BValue lhs = resolve(env, store, ie.getLeft());
			for(Address address : lhs.addressAD.addresses) {
				Obj object = store.getObj(address);
				Address propAddr = object.externalProperties.get(ie.getRight().toSource());
				BValue propVal = store.apply(propAddr);
				if(propVal == null) return BValue.top();
				else if(result == null ) result = propVal;
				else result = result.join(propVal);
			}
			return result;
		}
		else {
			/* Ignore everything else (e.g., method calls) for now. */
			return BValue.top();
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
			return resolve(env, store, ie.getLeft());
		}
		else {
			/* Ignore everything else (e.g., method calls) for now. */
			return BValue.top();
		}
	}

}