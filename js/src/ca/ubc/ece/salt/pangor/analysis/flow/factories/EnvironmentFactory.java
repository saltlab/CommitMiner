package ca.ubc.ece.salt.pangor.analysis.flow.factories;

import java.util.Map;

import org.apache.commons.lang3.tuple.Pair;
import org.mozilla.javascript.ast.AstNode;
import org.mozilla.javascript.ast.ScriptNode;

import ca.ubc.ece.salt.pangor.analysis.flow.abstractdomain.Addresses;
import ca.ubc.ece.salt.pangor.analysis.flow.abstractdomain.Environment;
import ca.ubc.ece.salt.pangor.analysis.flow.abstractdomain.Helpers;
import ca.ubc.ece.salt.pangor.analysis.flow.abstractdomain.Store;
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
	public static Pair<Environment, Store> createInitialEnvironment(ScriptNode script, Store store, Map<AstNode, CFG> cfgs, Trace trace) {
		Environment env = createBaseEnvironment();
		return Helpers.lift(env, store, script, cfgs, trace);
	}

	/**
	 * @return The global environment of builtins, without user defined variables.
	 */
	private static Environment createBaseEnvironment() {
		Environment env = new Environment();
		env = env.strongUpdate("this", new Addresses(StoreFactory.global_binding_Addr));
		env = env.strongUpdate("Object", new Addresses(StoreFactory.Object_Addr));
		return env;
	}

}
