package ca.ubc.ece.salt.pangor.analysis;

import java.util.Set;

import ca.ubc.ece.salt.pangor.batch.Commit;

/**
 * Describes a pattern found by an analysis.
 * @param <A> The type of alert that is produced by this analysis.
 */
public abstract class Pattern<A extends Alert> {

	/** The commit that this pattern was found in. */
	protected Commit commit;

	public Pattern(Commit commit) {
		this.commit = commit;
	}

	/**
	 * @return An {@code Alert} representation of this pattern.
	 */
	public abstract A getAlert();

	/**
	 * @param antiPatterns The list of anti-patterns produced during the analysis.
	 * @param preConditions The list of pre-conditions produced during the analysis.
	 * @return true if P in (P - AP n PC)
	 */
	public boolean accept(Set<AntiPattern> antiPatterns, Set<PreCondition> preConditions) {
		if(!antiPatterns.contains(this) && preConditions.contains(this)) {
			return true;
		}
		return false;
	}

}