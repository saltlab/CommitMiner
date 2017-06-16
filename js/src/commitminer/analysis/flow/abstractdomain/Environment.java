package commitminer.analysis.flow.abstractdomain;

import java.util.HashMap;
import java.util.Map;

/**
 * The abstract domain for storing mappings from identifiers to addresses.
 * i.e. Environment# := String#->P(BValue# | Address#)
 *
 * Identifiers may be
 */
public class Environment {

	/** The possible memory address for each identifier. **/
	public Map<Identifier, Addresses> environment;

	/**
	 * Creates an empty environment.
	 */
	public Environment() {
		this.environment = new HashMap<Identifier, Addresses>();
	}

	/**
	 * Creates an environment from an existing set of addresses.
	 * @param env The environment to replicate.
	 */
	private Environment(Map<Identifier, Addresses> env) {
		this.environment = env;
	}

	@Override
	public Environment clone() {
		Map<Identifier, Addresses> map = new HashMap<Identifier, Addresses>(this.environment);
		return new Environment(map);
	}

	/**
	 * Retrieve a variable's address.
	 * @param x The variable.
	 * @return The store addresses of the var.
	 */
	public Addresses apply(Identifier x) {
		return this.environment.get(x);
	}

	/**
	 * Performs a strong update on a variable in the environment.
	 * @param variable The variable to update.
	 * @param addresses The addresses for the variable.
	 * @return The updated environment.
	 */
	public Environment strongUpdate(Identifier variable, Addresses addresses) {
		Map<Identifier, Addresses> map = new HashMap<Identifier, Addresses>(this.environment);
		map.remove(variable); // We must remove the old variable to update the change LE.
		map.put(variable, addresses);
		return new Environment(map);
	}

	/**
	 * Performs a strong update on a variable in the environment.
	 * Updates the object directly without making a copy.
	 * @param variable The variable to update.
	 * @param addresses The addresses for the variable.
	 * @return The updated environment.
	 */
	public void strongUpdateNoCopy(Identifier variable, Addresses addresses) {
		this.environment.put(variable,  addresses);
	}

	/**
	 * Performs a weak update on a variable in the environment.
	 * @param variable The variable to update.
	 * @param addresses The addresses for the variable.
	 * @return The updated environment.
	 */
	public Environment weakUpdate(Identifier variable, Addresses addresses) {
		Map<Identifier, Addresses> map = new HashMap<Identifier, Addresses>(this.environment);
		// TODO: Do we need to merge the variables as well? We shouldn't need to
		// during flow analysis, but we do this for strongUpdate...
		map.put(variable, map.get(variable).join(addresses));
		return new Environment(map);
	}

	/**
	 * Computes ρ ∪ ρ
	 * @param environment The environment to join with this environment.
	 * @return The joined environments as a new environment.
	 */
	public Environment join(Environment environment) {
		Environment joined = new Environment(new HashMap<Identifier, Addresses>(this.environment));

		/* Because we dynamically allocate unexpected local variables to the
		 * environment, sometimes we will need to merge different environments.
		 *
		 * We do this by merging BValues and keeping only one address. */

		for(Map.Entry<Identifier, Addresses> entry : environment.environment.entrySet()) {

			/* The variable is missing from left. */
			if(!joined.environment.containsKey(entry.getKey())) {
				joined.environment.put(entry.getKey(), entry.getValue());
			}

			/* The value of left != the value from right. Merge the address lists for both environments. */
			if(joined.environment.get(entry.getKey()) != entry.getValue()) {
				joined.environment.put(entry.getKey(), joined.environment.get(entry.getKey()).join(entry.getValue()));
			}

		}
		return joined;
	}

	@Override
	public String toString() {
		String str = "-Variables-\n";
		for(Map.Entry<Identifier, Addresses> entry : this.environment.entrySet()) {
			str += entry.getKey().name.toString() + "_" + entry.getKey().change.toString() + ": " + entry.getValue().toString() + "\n";
		}
		return str;
	}
	
	@Override
	public boolean equals(Object o) {
		if(!(o instanceof Environment)) return false;
		Environment env = (Environment)o;
		for(Map.Entry<Identifier, Addresses> entry : env.environment.entrySet()) {
			if(!this.environment.containsKey(entry.getKey())) return false;
			if(!this.environment.get(entry.getKey()).equals(entry.getValue())) return false;
		}
		return true;
	}

}