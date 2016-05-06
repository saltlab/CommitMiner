package ca.ubc.ece.salt.pangor.analysis.flow.factories;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.tuple.Pair;
import org.mozilla.javascript.ast.AstNode;
import org.mozilla.javascript.ast.FunctionNode;
import org.mozilla.javascript.ast.ScriptNode;

import ca.ubc.ece.salt.pangor.analysis.flow.abstractdomain.Address;
import ca.ubc.ece.salt.pangor.analysis.flow.abstractdomain.Addresses;
import ca.ubc.ece.salt.pangor.analysis.flow.abstractdomain.Closure;
import ca.ubc.ece.salt.pangor.analysis.flow.abstractdomain.Environment;
import ca.ubc.ece.salt.pangor.analysis.flow.abstractdomain.FunctionClosure;
import ca.ubc.ece.salt.pangor.analysis.flow.abstractdomain.Helpers;
import ca.ubc.ece.salt.pangor.analysis.flow.abstractdomain.Store;
import ca.ubc.ece.salt.pangor.analysis.flow.abstractdomain.Undefined;
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
	 * @param σ The initial store, variable values and functions will be
	 * 			initialized here.
	 * @return The initial ρ ∈ Environment
	 */
	public static Environment createInitialEnvironment(ScriptNode script, Store store, Map<AstNode, CFG> cfgs) {
		Environment env = createBaseEnvironment();
		createEnvironment(env, store, script, cfgs);
		return env;
	}

	/**
	 * Completes the environment for the function or script.
	 */
	public static void createEnvironment(Environment env, Store store,
										 ScriptNode function,
										 Map<AstNode, CFG> cfgs) {

		/* Create the environment for this function. */

		// TODO: Does this work?
		String[] variableNames = function.getParamAndVarNames();
		List<Address> variableAddresses = new LinkedList<Address>();
		for(String variableName : variableNames) {
			Address address = null; // TODO: How do we create a new address?
			env.strongUpdate(variableName, new Addresses(address));
		}

		// TODO: Does this work? Does it return only statically declared functions?
		List<FunctionNode> children = function.getFunctions();
		List<Pair<FunctionNode, Address>> functionAddresses = new LinkedList<Pair<FunctionNode, Address>>();
		for(FunctionNode child : children) {
			if(child.getName().isEmpty()) continue; // Not accessible.
			Address address = null; // TODO: How do we create a new address?
			env.strongUpdate(child.getName(),  new Addresses(address));
		}

		/* All variables point to undefined in the store. */
		for(Address variableAddress : variableAddresses) {
			store.alloc(variableAddress, Undefined.inject(Undefined.top()));
		}

		/* Functions point to function objects in the store. */
		for(Pair<FunctionNode, Address> functionAddress : functionAddresses) {

			/* Create the environment for this function. */
			Environment functionEnvironment = new Environment();
			createEnvironment(functionEnvironment, store, functionAddress.getLeft(), cfgs);

			/* Merge this functions environment with the child's environment
			 * (JS closure). */
			for(String var : env.environment.keySet()) {
				if(!functionEnvironment.environment.containsKey(var)) {
					functionEnvironment.environment.put(var, env.environment.get(var));
				}
			}

			/* Create the new closure. */
			Closure closure = new FunctionClosure(cfgs.get(functionAddress.getLeft()), functionEnvironment);

			/* Allocate the object on the store. */
			store.alloc(functionAddress.getRight(), Helpers.createFunctionObj(closure));
		}

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
