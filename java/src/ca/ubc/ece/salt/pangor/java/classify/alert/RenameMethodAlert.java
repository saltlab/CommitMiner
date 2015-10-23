package ca.ubc.ece.salt.pangor.java.classify.alert;

import ca.ubc.ece.salt.pangor.analysis.Commit;
import ca.ubc.ece.salt.pangor.classify.alert.ClassifierAlert;

/**
 * Indicates that a refactoring occurred where a method was renamed.
 */
public class RenameMethodAlert extends ClassifierAlert {

	public RenameMethodAlert(Commit ami, String functionName,
			String type, String subtype) {
		super(ami, functionName, type, subtype);
	}

	@Override
	protected String getAlertDescription() {
		return "A rename method refactoring was performed.";
	}

	@Override
	protected String getAlertExplanation() {
		return "A rename method refactoring was performed.";
	}

}
