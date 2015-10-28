package ca.ubc.ece.salt.pangor.java.analysis.methodrename;

import ca.ubc.ece.salt.pangor.analysis.Commit;
import ca.ubc.ece.salt.pangor.analysis.Pattern;
import ca.ubc.ece.salt.pangor.analysis.PreCondition;
import ca.ubc.ece.salt.pangor.analysis.SourceCodeFileChange;
import ca.ubc.ece.salt.pangor.analysis.classify.ClassifierAlert;
import ca.ubc.ece.salt.pangor.java.classify.alert.RenameMethodAlert;

/**
 * A pattern where a method was renamed (i.e., a method name was updated).
 */
public class RenameMethodPattern extends Pattern<ClassifierAlert> implements PreCondition {

	/** The original method name. **/
	public String oldName;

	/** The new method name. **/
	public String newName;

	public RenameMethodPattern(Commit commit,
			SourceCodeFileChange sourceCodeFileChange,
			String oldName, String newName) {
		super(commit, sourceCodeFileChange);
		this.oldName = oldName;
		this.newName = newName;
	}

	@Override
	public RenameMethodAlert getAlert() {
		return new RenameMethodAlert(this.commit, this.sourceCodeFileChange,
				"~NA~", "REFACTOR", "METHOD_RENAME", this.oldName, this.newName);
	}

	@Override
	public boolean equals(Object o) {

		/* This may be compared to an updated call site. */
		if(o instanceof UpdateCallsitePattern) {
			UpdateCallsitePattern rmp = (UpdateCallsitePattern)o;
			if(this.oldName.equals(rmp.oldName)
				&& this.newName.equals(rmp.newName)) {
				return true;
			}
		}
		/* This may be compared to a MethodRename anti-pattern. */
		if(o instanceof RenameMethodAntiPattern) {
			RenameMethodAntiPattern rmap = (RenameMethodAntiPattern)o;
			if(this.oldName.equals(rmap.methodName)) {
				return true;
			}
		}

		return false;

	}

	@Override
	public int hashCode() {
		return (this.oldName).hashCode();
	}

}
