package ca.ubc.ece.salt.pangor.analysis;

import ca.ubc.ece.salt.pangor.batch.Commit;

/**
 * Describes an anti-pattern found by an analysis.
 */
public class AntiPattern {

	/** The commit that this anti-pattern was found in. */
	protected Commit commit;

	public AntiPattern(Commit commit) {
		this.commit = commit;
	}

}
