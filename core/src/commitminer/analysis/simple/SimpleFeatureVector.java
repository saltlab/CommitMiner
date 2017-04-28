package commitminer.analysis.simple;

import commitminer.analysis.Commit;
import commitminer.analysis.FeatureVector;

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