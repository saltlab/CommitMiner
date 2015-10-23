package ca.ubc.ece.salt.pangor.analysis;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

/**
 * Stores the analysis facts for a commit.
 */
public class Facts<A extends Alert> {

	/** Pre-conditions found by the analysis. **/
	private Set<PreCondition> preConditions;

	/** Anti-patterns found by the analysis. **/
	private Set<AntiPattern> antiPatterns;

	/** Pattern<A>s found by the analysis. **/
	private List<Pattern<A>> patterns;

	public Facts() {
		/* Initialize the pattern sets. */
		this.preConditions = new HashSet<PreCondition>();
		this.antiPatterns = new HashSet<AntiPattern>();
		this.patterns = new LinkedList<Pattern<A>>();

	}

	/**
	 * Adds a pre-condition fact.
	 * @param preCondition The pre-condition.
	 */
	public void addPreCondition(PreCondition preCondition) {
		this.preConditions.add(preCondition);
	}

	/**
	 * @return The set of pre-condition facts generated during the analysis.
	 */
	public Set<PreCondition> getPreConditions() {
		return this.preConditions;
	}

	/**
	 * Adds an anti-pattern fact.
	 * @param antiPattern<A> The anti-pattern.
	 */
	public void addAntiPattern(AntiPattern antiPattern) {
		this.antiPatterns.add(antiPattern);
	}

	/**
	 * @return The set of anti-pattern facts generated during the analysis.
	 */
	public Set<AntiPattern> getAntiPatterns() {
		return this.antiPatterns;
	}

	/**
	 * Adds a pattern fact.
	 * @param pattern The pattern.
	 */
	public void addPattern(Pattern<A> pattern) {
		this.patterns.add(pattern);
	}

	/**
	 * @return The set of pattern facts generated during the analysis.
	 */
	public List<Pattern<A>> getPatterns() {
		return this.patterns;
	}


}
