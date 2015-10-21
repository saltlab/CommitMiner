package ca.ubc.ece.salt.pangor.analysis.simple;

import ca.ubc.ece.salt.pangor.analysis.Alert;
import ca.ubc.ece.salt.pangor.analysis.Pattern;
import ca.ubc.ece.salt.pangor.batch.Commit;

public class SimplePattern extends Pattern {

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
	public Alert getAlert(Commit commit) {
		return new SimpleAlert(commit, this.pattern);
	}

}