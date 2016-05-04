package ca.ubc.ece.salt.pangor.analysis.flow.abstractdomain;

import java.util.HashMap;
import java.util.Map;

/**
 * Scratchpad memory. This framework differs from JSAI in that it uses variable
 * names directly because there is not intermediate representation. Therefore,
 * unlike JSAI, we will use a map here because we have string identifiers.
 */
public class Scratchpad extends SmartHash {

	/** The values in this memory. **/
	Map<String, BValue> values;

	/**
	 * @param values The values in scratchpad memory.
	 */
	public Scratchpad(Map<String, BValue> values) {
		this.values = values;
	}

	/**
	 * @param scratchVar The variable to look up.
	 * @return The value in scratchpad memory.
	 */
	public BValue apply(String scratchVar) {
		return this.values.get(scratchVar);
	}

	/**
	 * Performs a strong update.
	 * @param scratchVar The variable identifier.
	 * @param value The value to update.
	 * @return The updated scratchpad.
	 */
	public Scratchpad strongUpdate(String scratchVar, BValue value) {
		Map<String, BValue> values = new HashMap<String, BValue>(this.values);
		values.put(scratchVar, value);
		return new Scratchpad(values);
	}

	/**
	 * Compute the union of this and another Scratchpad.
	 * @param pad The Scratchpad to union.
	 * @return The union of the scratchpads.
	 */
	public Scratchpad join(Scratchpad pad) {

		if(this.values.size() != pad.values.size()) throw new Error("Pad lengths do not match.");
		if(this == pad) return new Scratchpad(new HashMap<String, BValue>(this.values));

		Map<String, BValue> values = new HashMap<String, BValue>();
		for(String scratchVar : this.values.keySet()) {
			BValue left = this.values.get(scratchVar);
			BValue right = pad.values.get(scratchVar);
			values.put(scratchVar, left.join(right));
		}
		return new Scratchpad(values);

	}

}