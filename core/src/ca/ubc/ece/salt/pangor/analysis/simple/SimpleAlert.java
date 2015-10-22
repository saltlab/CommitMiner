package ca.ubc.ece.salt.pangor.analysis.simple;

import ca.ubc.ece.salt.pangor.analysis.Alert;
import ca.ubc.ece.salt.pangor.analysis.Commit;

public class SimpleAlert extends Alert {

	/** The pattern found by the analysis. **/
	public String pattern;

	public SimpleAlert(Commit commit, String pattern) {
		super(commit);
		this.pattern = pattern;
	}

}