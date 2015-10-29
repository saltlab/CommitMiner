package ca.ubc.ece.salt.pangor.java.classify.alert;

import ca.ubc.ece.salt.pangor.analysis.Commit;
import ca.ubc.ece.salt.pangor.analysis.SourceCodeFileChange;
import ca.ubc.ece.salt.pangor.analysis.classify.ClassifierAlert;

/**
 * Indicates that a refactoring occurred where a method was renamed.
 */
public class UpdatedCallsiteAlert extends ClassifierAlert {

	/** The function containing the call site. **/
	public String function;

	/** The line number of the call site in the source file. **/
	public int srcLine;

	/** The line number of the call site in the destination file. **/
	public int dstLine;

	/** The original method name. **/
	private String oldName;

	/** The new method name. **/
	private String newName;

	public UpdatedCallsiteAlert(Commit commit, SourceCodeFileChange sourceCodeFileChange,
			String functionName, String type, String subtype, String function,
			int srcLine, int dstLine, String oldName, String newName) {
		super(commit, sourceCodeFileChange, functionName, type, subtype);
		this.function = function;
		this.srcLine = srcLine;
		this.dstLine = dstLine;
		this.oldName = oldName;
		this.newName = newName;
	}

	@Override
	public boolean equals(Object o) {
		if(o instanceof UpdatedCallsiteAlert) {
			UpdatedCallsiteAlert a = (UpdatedCallsiteAlert) o;
			return this.type.equals(a.type) && this.subtype.equals(a.subtype)
					&& this.commit.equals(a.commit)
					&& this.sourceCodeFileChange.equals(a.sourceCodeFileChange)
					&& this.function.equals(a.function)
					&& this.srcLine == a.srcLine
					&& this.dstLine == a.dstLine
					&& this.oldName.equals(a.oldName)
					&& this.newName.equals(a.newName);
		}
		return false;
	}

	@Override
	protected String getAlertDescription() {
		return "A call site target in " + this.sourceCodeFileChange.getFileName() + "." + this.function + " at line " + this.srcLine + " was updated from " + this.oldName + " to " + this.newName + ".";
	}

	@Override
	protected String getAlertExplanation() {
		return "A rename method refactoring was performed.";
	}

}