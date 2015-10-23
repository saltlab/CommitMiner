package ca.ubc.ece.salt.pangor.analysis.simple;

import ca.ubc.ece.salt.pangor.analysis.AntiPattern;
import ca.ubc.ece.salt.pangor.analysis.Commit;
import ca.ubc.ece.salt.pangor.analysis.Pattern;
import ca.ubc.ece.salt.pangor.analysis.PreCondition;
import ca.ubc.ece.salt.pangor.analysis.SourceCodeFileChange;

/**
 * Describes a change pattern.
 */
public class SimplePattern extends Pattern<SimpleAlert> implements PreCondition, AntiPattern {

	/** The pattern description. **/
	private String pattern;

	/**
	 * @param commit The commit that contained this pattern.
	 * @param pattern The pattern description.
	 */
	public SimplePattern(Commit commit, SourceCodeFileChange sourceCodeFileChange, String pattern) {
		super(commit, sourceCodeFileChange);
		this.pattern = pattern;
	}

	@Override
	public SimpleAlert getAlert() {
		return new SimpleAlert(this.commit, this.sourceCodeFileChange, this.pattern);
	}

	@Override
	public boolean equals(Object o) {
		if(o instanceof SimplePattern) {
			SimplePattern sp = (SimplePattern)o;
			return this.pattern.equals(sp.pattern);
		}
		return false;
	}

	@Override
	public int hashCode() {
		return this.pattern.hashCode();
	}

}