package ca.ubc.ece.salt.pangor.analysis;

import ca.ubc.ece.salt.pangor.batch.Commit;
import ca.ubc.ece.salt.pangor.batch.SourceCodeFileChange;

/**
 * Stores some information that has been inferred by static analysis.
 */
public abstract class Alert {

	/** A counter to produce unique IDs for each alert. **/
	private static int idCounter;

	/** The unique ID for the alert. **/
	public int id;

	/** The name of the function that was analyzed. **/
	public String functionName;

	/** The commit information. */
	public Commit commit;

	/**
	 * The source code file information (may be null if it is a
	 * commit-level alert).
	 **/
	public SourceCodeFileChange sourceCodeFileChange;

	/**
	 * @param commit The meta and change information from the commit.
	 * @param functionName The name of the function that was analyzed.
	 **/
	public Alert(Commit commit, SourceCodeFileChange sourceCodeFileChange, String functionName) {
		this.commit = commit;
		this.sourceCodeFileChange = sourceCodeFileChange;
		this.functionName = functionName;
		this.id = getNextID();
	}

	/**
	 * Used for de-serializing alerts
	 * @param commit The meta information from a bulk analysis.
	 * @param functionName The name of the function that was analyzed.
	 **/
	public Alert(Commit commit, SourceCodeFileChange sourceCodeFileChange, String functionName, int id) {
		this.commit = commit;
		this.sourceCodeFileChange = sourceCodeFileChange;
		this.functionName = functionName;
		this.id = id;
	}

	/**
	 * Generates unique IDs for alerts. Synchronized because it may be
	 * called by several GitProjectAnalysis threads.
	 *
	 * @return The next unique ID for an alert.
	 */
	private synchronized static int getNextID() {
		idCounter++;
		return idCounter;
	}

}