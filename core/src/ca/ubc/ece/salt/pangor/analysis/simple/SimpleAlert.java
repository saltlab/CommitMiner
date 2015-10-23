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

	@Override
	public String toString() {
		return this.commit.sourceCodeFileChanges.get(0).buggyFile + ", " + this.pattern;
	}

	@Override
	public boolean equals(Object o) {
		if(o instanceof SimpleAlert) {
			SimpleAlert sa = (SimpleAlert)o;
			if(this.pattern.equals(sa.pattern)
					&& this.commit.equals(sa.commit)) return true;
		}
		return false;
	}

}