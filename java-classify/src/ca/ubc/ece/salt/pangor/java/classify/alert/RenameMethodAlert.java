package ca.ubc.ece.salt.pangor.java.classify.alert;

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
	protected String getAlertDescription() {
		return "A method was renamed from " + this.oldName + " to " + this.newName + ".";
	}

	@Override
	protected String getAlertExplanation() {
		return "A rename method refactoring was performed.";
	}

}