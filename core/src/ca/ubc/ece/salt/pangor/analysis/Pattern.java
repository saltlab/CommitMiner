package ca.ubc.ece.salt.pangor.analysis;

import java.util.Set;

/**
 * Describes a pattern found by an analysis.
 * @param <A> The type of alert that is produced by this analysis.
 */
public abstract class Pattern<A extends Alert> {

	/** The commit information. */
	public Commit commit;

	/** The source code file change information. */
	public SourceCodeFileChange sourceCodeFileChange;

	/**
	 * @param commit The commit information.
	 * @param sourceCodeFileChange The source code file change information.
	 */
	public Pattern(Commit commit, SourceCodeFileChange sourceCodeFileChange) {
		this.commit = commit;
		this.sourceCodeFileChange = sourceCodeFileChange;
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

	@Override
	public abstract boolean equals(Object o);

	@Override
	public abstract int hashCode();

}
