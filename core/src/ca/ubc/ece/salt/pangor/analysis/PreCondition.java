package ca.ubc.ece.salt.pangor.analysis;

import ca.ubc.ece.salt.pangor.batch.Commit;

/**
 * Described a pre-condition found by the analysis.
 */
public class PreCondition {

	/** The commit that this anti-pattern was found in. */
	protected Commit commit;

	public PreCondition(Commit commit) {
		this.commit = commit;
	}

}
