package ca.ubc.ece.salt.pangor.analysis.simple;

import ca.ubc.ece.salt.pangor.analysis.Alert;
import ca.ubc.ece.salt.pangor.analysis.Commit;
import ca.ubc.ece.salt.pangor.analysis.SourceCodeFileChange;

public class SimpleAlert extends Alert {

	/** The pattern found by the analysis. **/
	public String pattern;

	public SimpleAlert(Commit commit, SourceCodeFileChange sourceCodeFileChange, String pattern) {
		super(commit, sourceCodeFileChange);
		this.pattern = pattern;
	}

	@Override
	public String toString() {
		return this.commit.buggyCommitID
				+ ", " + this.commit.repairedCommitID
				+ ", " + this.sourceCodeFileChange.buggyFile
				+ ", " + this.sourceCodeFileChange.repairedFile
				+ ", " + this.pattern;
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