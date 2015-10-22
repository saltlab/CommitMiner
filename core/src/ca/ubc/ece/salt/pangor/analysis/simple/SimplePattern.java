package ca.ubc.ece.salt.pangor.analysis.simple;

import ca.ubc.ece.salt.pangor.analysis.Commit;
import ca.ubc.ece.salt.pangor.analysis.Pattern;

/**
 * Describes a change pattern.
 */
public class SimplePattern extends Pattern<SimpleAlert> {

	/** The pattern description. **/
	private String pattern;

	/**
	 * @param commit The commit that contained this pattern.
	 * @param pattern The pattern description.
	 */
	public SimplePattern(String pattern) {
		this.pattern = pattern;
	}

	@Override
	public SimpleAlert getAlert(Commit commit) {
		return new SimpleAlert(commit, this.pattern);
	}

}