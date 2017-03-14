package ca.ubc.ece.salt.pangor.analysis.flow.abstractdomain;

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
	public Map<Identifier, Address> environment;

	/**
	 * Creates an empty environment.
	 */
	public Environment() {
		this.environment = new HashMap<Identifier, Address>();
	}

	/**
	 * Creates an environment from an existing set of addresses.
	 * @param env The environment to replicate.
	 */
	private Environment(Map<Identifier, Address> env) {
		this.environment = env;
	}

	@Override
	public Environment clone() {
		Map<Identifier, Address> map = new HashMap<Identifier, Address>(this.environment);
		return new Environment(map);
	}

	/**
	 * Retrieve a variable's address.
	 * @param x The variable.
	 * @return The store address of the var.
	 */
	public Address apply(Identifier x) {
		return this.environment.get(x);
	}

	/**
	 * Performs a strong update on a variable in the environment.
	 * @param variable The variable to update.
	 * @param address The address for the variable.
	 * @return The updated environment.
	 */
	public Environment strongUpdate(Identifier variable, Address address) {
		Map<Identifier, Address> map = new HashMap<Identifier, Address>(this.environment);
		map.remove(variable); // We must remove the old variable to update the change LE.
		map.put(variable, address);
		return new Environment(map);
	}

	/**
	 * Performs a strong update on a variable in the environment.
	 * Updates the object directly without making a copy.
	 * @param variable The variable to update.
	 * @param address The address for the variable.
	 * @return The updated environment.
	 */
	public void strongUpdateNoCopy(Identifier variable, Address address) {
		this.environment.put(variable,  address);
	}

	/**
	 * Computes ρ ∪ ρ
	 * @param environment The environment to join with this environment.
	 * @return The joined environments as a new environment.
	 */
	public Environment join(Environment environment) {
		Environment joined = new Environment(new HashMap<Identifier, Address>(this.environment));

		/* Because we dynamically allocate unexpected local variables to the
		 * environment, sometimes we will need to merge different environments.
		 *
		 * We do this by merging BValues and keeping only one address. */

		for(Map.Entry<Identifier, Address> entry : environment.environment.entrySet()) {

			/* The variable is missing from left. */
			if(!joined.environment.containsKey(entry.getKey())) {
				joined.environment.put(entry.getKey(), entry.getValue());
			}

			/* The value of left != the value from right. For now we'll just
			 * keep left and emit a warning. */
			if(joined.environment.get(entry.getKey()) != entry.getValue()) {

//				System.out.println("Warning: Merging unequal environments.");
//				System.out.println("\tVariable name = " + entry.getKey().name);
//				System.out.println("\tChange type = " + entry.getKey().change);
//				System.out.println("\tAddress = " + entry.getValue());

			}

		}
		return joined;
	}

	@Override
	public String toString() {
		String str = "-Variables-\n";
		for(Map.Entry<Identifier, Address> entry : this.environment.entrySet()) {
			str += entry.getKey().name.toString() + "_" + entry.getKey().change.toString() + ": " + entry.getValue().toString() + "\n";
		}
		return str;
	}

}