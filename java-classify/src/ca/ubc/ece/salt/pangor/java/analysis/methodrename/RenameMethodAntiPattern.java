package ca.ubc.ece.salt.pangor.java.analysis.methodrename;

import ca.ubc.ece.salt.pangor.analysis.AntiPattern;

/**
 * Indicates that a method rename pattern is a false positive.
 */
public class RenameMethodAntiPattern implements AntiPattern {

	/** The unchanged method name. **/
	public String methodName;

	public RenameMethodAntiPattern(String methodName) {
		this.methodName = methodName;
	}

	@Override
	public boolean equals(Object o) {

		/* This may be compared to the RenameMethod pattern. */
		if(o instanceof RenameMethodPattern) {
			RenameMethodPattern rmp = (RenameMethodPattern)o;
			if(this.methodName.equals(rmp.newName)) {
				return true;
			}
		}
		/* This may be compared to the UpdateCallsite pattern. */
		else if(o instanceof UpdateCallsitePattern) {
			UpdateCallsitePattern rmp = (UpdateCallsitePattern)o;
			if(this.methodName.equals(rmp.newName)) {
				return true;
			}
		}

		return false;

	}

	@Override
	public int hashCode() {
		return (this.methodName).hashCode();
	}

}