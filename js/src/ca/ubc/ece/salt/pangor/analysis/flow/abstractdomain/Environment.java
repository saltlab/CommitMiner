package ca.ubc.ece.salt.pangor.analysis.flow.abstractdomain;

import java.util.HashMap;
import java.util.Map;

import org.mozilla.javascript.ast.ScriptNode;

/**
 * The abstract domain for storing mappings from identifiers to addresses.
 * i.e. Environment# := String#->P(BValue# | Address#)
 *
 * Identifiers may be
 */
public class Environment extends SmartHash {

	/** The possible memory address for each identifier. **/
	public Map<String, Addresses> environment;

	/**
	 * Create the initial state for the environment. The initial sate includes
	 * variables and functions that are declared within the function and
	 * therefore raised before the function is executed.
	 * @param function The script of function we are analyzing.
	 */
	public Environment(ScriptNode function) {
		// TODO: Visit all the statements in the function and raise functions
		//		 and variables declared within the function.
	}

	/**
	 * Create the initial state for the environment. The initial sate includes
	 * variables and functions that are declared within the function and
	 * therefore raised before the function is executed.
	 * @param function The script of function we are analyzing.
	 */
	private Environment(Map<String, Addresses> environment) {
		this.environment = environment;
	}

	/**
	 * Performs a weak update on a variable in the environment.
	 * @param variable The variable to update.
	 * @param addresses The addresses for the variable.
	 * @return The new environment.
	 */
	public Environment weakUpdate(String variable, Addresses addresses) {
		Map<String, Addresses> environment = new HashMap<String, Addresses>(this.environment);
		Addresses left = environment.get(variable);
		if(left == null) environment.put(variable, addresses);
		else environment.put(variable, left.weakUpdate(addresses.addresses));
		return new Environment(environment);
	}

	/**
	 * Performs a strong update on a variable in the environment.
	 * @param variable The variable to update.
	 * @param addresses The addresses for the variable.
	 * @return The new environment.
	 */
	public Environment strongUpdate(String variable, Addresses addresses) {
		Map<String, Addresses> environment = new HashMap<String, Addresses>(this.environment);
		environment.put(variable, addresses);
		return new Environment(environment);
	}

	/**
	 * Joins two addresses.
	 * @param environment The environment to join with this environment.
	 * @return The joined environments as a new environment.
	 */
	public Environment join(Environment environment) {
		Environment joined = new Environment(this.environment);
		for(Map.Entry<String, Addresses> entry : environment.environment.entrySet()) {
			joined.weakUpdate(entry.getKey(), entry.getValue());
		}
		return joined;
	}

}