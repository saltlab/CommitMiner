package ca.ubc.ece.salt.pangor.analysis.flow.abstractdomain;

import java.util.HashMap;
import java.util.Map;

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
	 * Creates an empty environment.
	 */
	public Environment() {
		this.environment = new HashMap<String, Addresses>();
	}

	/**
	 * Creates an environment from an existing set of addresses.
	 * @param env The environment to replicate.
	 */
	private Environment(Map<String, Addresses> env) {
		this.environment = env;
	}

	/**
	 * Retrieve a variable's addresses.
	 * @param x The variable.
	 * @return The set of possible addresses.
	 */
	public Addresses apply(String x) {
		return this.environment.get(x);
	}

	/**
	 * Performs a weak update on a variable in the environment.
	 * @param variable The variable to update.
	 * @param addresses The addresses for the variable.
	 * @return The updated environment.
	 */
	public Environment weakUpdate(String variable, Addresses addresses) {
		Map<String, Addresses> map = new HashMap<String, Addresses>(this.environment);
		Addresses left = map.get(variable);
		if(left == null) map.put(variable, addresses);
		else map.put(variable, left.weakUpdate(addresses.addresses));
		return new Environment(map);
	}

	/**
	 * Performs a strong update on a variable in the environment.
	 * @param variable The variable to update.
	 * @param address The address for the variable.
	 * @return The updated environment.
	 */
	public Environment strongUpdate(String variable, Addresses addresses) {
		Map<String, Addresses> map = new HashMap<String, Addresses>(this.environment);
		map.put(variable, addresses);
		return new Environment(map);
	}

	/**
	 * Computes ρ ∪ ρ
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