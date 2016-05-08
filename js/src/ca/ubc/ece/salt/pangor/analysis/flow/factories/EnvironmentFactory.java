package ca.ubc.ece.salt.pangor.analysis.flow.factories;

import java.util.List;
import java.util.Map;

import org.mozilla.javascript.ast.AstNode;
import org.mozilla.javascript.ast.FunctionNode;
import org.mozilla.javascript.ast.Name;
import org.mozilla.javascript.ast.ScriptNode;

import ca.ubc.ece.salt.pangor.analysis.flow.abstractdomain.Address;
import ca.ubc.ece.salt.pangor.analysis.flow.abstractdomain.Addresses;
import ca.ubc.ece.salt.pangor.analysis.flow.abstractdomain.Closure;
import ca.ubc.ece.salt.pangor.analysis.flow.abstractdomain.Environment;
import ca.ubc.ece.salt.pangor.analysis.flow.abstractdomain.FunctionClosure;
import ca.ubc.ece.salt.pangor.analysis.flow.abstractdomain.Helpers;
import ca.ubc.ece.salt.pangor.analysis.flow.abstractdomain.State;
import ca.ubc.ece.salt.pangor.analysis.flow.abstractdomain.Store;
import ca.ubc.ece.salt.pangor.analysis.flow.abstractdomain.Undefined;
import ca.ubc.ece.salt.pangor.analysis.flow.trace.Trace;
import ca.ubc.ece.salt.pangor.cfg.CFG;

/**
 * Initializes the environment from a JavaScript file AST.
 */
public class EnvironmentFactory {

	/**
	 * Creates an initial environment for a function by lifting local variables
	 * and functions into the environment. Local variables are initialized to
	 * undefined in the store, while functions are initialized to objects.
	 *
	 * Variables are initialized in the store after the environment has been
	 * computed. Variables that point to functions are initialized recursively
	 * so that their closure can be properly computed.
	 * @param script The root of the AST for the file under analysis.
	 * @param store The initial store, variable values and functions will be
	 * 			initialized here.
	 * @param trace The trace, which should be empty initially.
	 * @return The initial ρ ∈ Environment
	 */
	public static State createInitialEnvironment(ScriptNode script, Store store, Map<AstNode, CFG> cfgs, Trace trace) {
		Environment env = createBaseEnvironment();
		return createEnvironment(env, store, script, cfgs, trace);
	}

	/**
	 * Completes the environment for the function or script by lifting
	 * variables and function definitions.
	 * @param env The environment (or closure) in which the function executes.
	 * @param store The current store.
	 * @param function The code we are analyzing.
	 * @param cfgs The control flow graphs for the file. Needed for
	 * 			   initializing lifted functions.
	 * @param trace The program trace including the call site of this function.
	 */
	public static State createEnvironment(Environment env, Store store,
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
			Closure closure = new FunctionClosure(cfgs.get(child), env);
			store = store.alloc(address, Helpers.createFunctionObj(closure));

			/* The function name variable points to out new function. */
			store = store.alloc(address, Address.inject(address));
		}

		return new State(store, env, trace);

	}

	/**
	 * @return The global environment of builtins, without user defined variables.
	 */
	private static Environment createBaseEnvironment() {
		Environment env = new Environment();
		env.strongUpdate("Object", new Addresses(StoreFactory.Object_Addr));
		return env;
	}

}
