package ca.ubc.ece.salt.pangor.analysis.classify;

import ca.ubc.ece.salt.pangor.analysis.Alert;
import ca.ubc.ece.salt.pangor.analysis.Commit;

/**
 * Stores an alert that notifies the user about a repair that has been found
 * by a checker.
 *
 * The alert includes meta information. The meta information (e.g., project,
 * commit #, file names, etc.) is used to investigate classification.
 */
public abstract class ClassifierAlert extends Alert {

	/** A description of the pattern that was found. **/
	protected String pattern;

	/**
	 * An alert is always associated a concrete Checker.
	 * @param commit
	 * @param sourceCodeFileChange
	 * @param type The checker which generated the alert.
	 * @param subtype A checker may detect more than one repair subtype.
	 */
	public ClassifierAlert(Commit commit, String pattern) {
		super(commit);
		this.pattern = pattern;
	}

	/**
	 * An alert is always associated a concrete Checker. This constructor
	 * should only be used if making a ClassifierAlert from serial. Otherwise
	 * the other constructor should be used so the ID is automatically
	 * generated.
	 * @param commit
	 * @param sourceCodeFileChange
	 * @param type The checker which generated the alert.
	 * @param subtype A checker may detect more than one repair subtype.
	 * @param id The unique id for the alert.
	 */
	public ClassifierAlert(Commit commit, int id) {
		super(commit, id);
	}

	/**
	 * This method serializes the alert. This is useful when writing
	 * a data set to the disk.
	 * @return The serialized version of the alert.
	 */
	public String serialize() {

		String serialized = id + "," + this.commit.projectID
				+ "," + this.commit.projectHomepage + "/commit/" + this.commit.repairedCommitID
				+ "," + this.commit.buggyCommitID + "," + this.commit.repairedCommitID
				+ "," + this.pattern;

		return serialized;

	}

	@Override
	public boolean equals(Object o) {
		if(o instanceof ClassifierAlert) {
			ClassifierAlert a = (ClassifierAlert) o;
			return this.pattern.equals(a.pattern);
		}
		return false;
	}

	@Override
	public String toString() {
		return this.pattern;
	}

	@Override
	public int hashCode() {
		return this.pattern.hashCode();
	}

}