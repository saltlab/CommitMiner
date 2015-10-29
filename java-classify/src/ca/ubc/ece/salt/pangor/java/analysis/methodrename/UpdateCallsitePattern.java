package ca.ubc.ece.salt.pangor.java.analysis.methodrename;

import ca.ubc.ece.salt.pangor.analysis.Commit;
import ca.ubc.ece.salt.pangor.analysis.Pattern;
import ca.ubc.ece.salt.pangor.analysis.SourceCodeFileChange;
import ca.ubc.ece.salt.pangor.analysis.classify.ClassifierAlert;
import ca.ubc.ece.salt.pangor.java.classify.alert.UpdatedCallsiteAlert;

/**
 * A pattern where a call site was updated to reflect a renamed method.
 */
public class UpdateCallsitePattern extends Pattern<ClassifierAlert> {

	/** The function containing the call site. **/
	public String function;

	/** The line number of the call site in the source file. **/
	public int srcLine;

	/** The line number of the call site in the destination file. **/
	public int dstLine;

	/** The original method name. **/
	public String oldName;

	/** The new method name. **/
	public String newName;

	public UpdateCallsitePattern(Commit commit,
			SourceCodeFileChange sourceCodeFileChange,
			String function,
			int srcLine, int dstLine,
			String oldName, String newName) {
		super(commit, sourceCodeFileChange);
		this.function = function;
		this.srcLine = srcLine;
		this.dstLine = dstLine;
		this.oldName = oldName;
		this.newName = newName;
	}

	@Override
	public UpdatedCallsiteAlert getAlert() {
		return new UpdatedCallsiteAlert(this.commit, this.sourceCodeFileChange,
				this.function, "REFACTOR", "METHOD_RENAME_UPDATE_CALLSITE", this.function,
				this.srcLine, this.dstLine, this.oldName, this.newName);
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
