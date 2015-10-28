package ca.ubc.ece.salt.pangor.java.analysis.methodrename;

import ca.ubc.ece.salt.pangor.analysis.Commit;
import ca.ubc.ece.salt.pangor.analysis.Pattern;
import ca.ubc.ece.salt.pangor.analysis.SourceCodeFileChange;
import ca.ubc.ece.salt.pangor.analysis.classify.ClassifierAlert;
import ca.ubc.ece.salt.pangor.java.classify.alert.RenameMethodAlert;

/**
 * A pattern where a call site was updated to reflect a renamed method.
 */
public class UpdateCallsitePattern extends Pattern<ClassifierAlert> {

	/** The original method name. **/
	public String oldName;

	/** The new method name. **/
	public String newName;

	public UpdateCallsitePattern(Commit commit,
			SourceCodeFileChange sourceCodeFileChange,
			String oldName, String newName) {
		super(commit, sourceCodeFileChange);
		this.oldName = oldName;
		this.newName = newName;
	}

	@Override
	public RenameMethodAlert getAlert() {
		return new RenameMethodAlert(this.commit, this.sourceCodeFileChange,
				"~NA~", "REFACTOR", "METHOD_RENAME_UPDATE_CALLSITE", this.oldName, this.newName);
	}

	@Override
	public boolean equals(Object o) {

		/* This may be compared to the method rename. */
		if(o instanceof RenameMethodPattern) {
			RenameMethodPattern rmp = (RenameMethodPattern)o;
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
