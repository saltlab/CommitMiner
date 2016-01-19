package ca.ubc.ece.salt.pangor.analysis.simple;

import ca.ubc.ece.salt.pangor.analysis.FeatureVector;
import ca.ubc.ece.salt.pangor.analysis.Commit;

public class SimpleFeatureVector extends FeatureVector {

	/** The pattern found by the analysis. **/
	public String pattern;

	public SimpleFeatureVector(Commit commit, String pattern) {
		super(commit);
		this.pattern = pattern;
	}

	@Override
	public String toString() {
		return this.commit.buggyCommitID
				+ ", " + this.commit.repairedCommitID
				+ ", " + this.pattern;
	}

	@Override
	public boolean equals(Object o) {
		if(o instanceof SimpleFeatureVector) {
			SimpleFeatureVector sa = (SimpleFeatureVector)o;
			if(this.pattern.equals(sa.pattern)
					&& this.commit.equals(sa.commit)) return true;
		}
		return false;
	}

}