package ca.ubc.ece.salt.pangor.java.analysis.methodrename;

import ca.ubc.ece.salt.pangor.analysis.Commit;
import ca.ubc.ece.salt.pangor.analysis.SourceCodeFileChange;
import ca.ubc.ece.salt.pangor.analysis.classify.ClassifierAlert;

/**
 * Indicates that a refactoring occurred where a method was renamed.
 */
public class RenameMethodAlert extends ClassifierAlert {

	/** The original method name. **/
	private String oldName;

	/** The new method name. **/
	private String newName;

	public RenameMethodAlert(Commit commit, SourceCodeFileChange sourceCodeFileChange,
			String functionName, String type, String subtype, String oldName,
			String newName) {
		super(commit, sourceCodeFileChange, functionName, type, subtype);
		this.oldName = oldName;
		this.newName = newName;
	}

	@Override
	public boolean equals(Object o) {
		if(o instanceof RenameMethodAlert) {
			RenameMethodAlert a = (RenameMethodAlert) o;
			return this.type.equals(a.type) && this.subtype.equals(a.subtype)
					&& this.commit.equals(a.commit)
					&& this.sourceCodeFileChange.equals(a.sourceCodeFileChange)
					&& this.oldName.equals(a.oldName)
					&& this.newName.equals(a.newName);
		}
		return false;
	}

	@Override
	protected String getAlertDescription() {
		return "A method in " + this.sourceCodeFileChange.getFileName() + " was renamed from " + this.oldName + " to " + this.newName + ".";
	}

	@Override
	protected String getAlertExplanation() {
		return "A rename method refactoring was performed.";
	}

}