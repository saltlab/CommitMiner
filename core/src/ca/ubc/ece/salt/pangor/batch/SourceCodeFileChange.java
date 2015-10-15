package ca.ubc.ece.salt.pangor.batch;

/**
 * Stores the information that represents a change to a source code file.
 *
 * This is used by {@code CommitAnalysid} to initiate a
 * {@code SourceCodeFileAnalysis}.
 */
public class SourceCodeFileChange {

	/** The path to the source file before the commit. **/
	public String buggyFile;

	/** The path to the source file after the commit. **/
	public String repairedFile;

	/** The code before the commit. **/
	public String buggyCode;

	/** The code after the commit. **/
	public String repairedCode;

	/**
	 * @param buggyFile The path to the source file before the commit.
	 * @param repairedFile The path to the source file after the commit.
	 * @param buggyCode The code before the commit.
	 * @param repairedCode The code after the commit.
	 */
	public SourceCodeFileChange(String buggyFile, String repairedFile,
								String buggyCode, String repairedCode) {
		this.buggyFile = buggyFile;
		this.repairedFile = repairedFile;
		this.buggyCode = buggyCode;
		this.repairedCode = repairedCode;
	}

}
