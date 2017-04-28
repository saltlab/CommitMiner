package commitminer.analysis;


/**
 * Stores a row of information that has been inferred by static analysis.
 */
public abstract class FeatureVector {

	/** A counter to produce unique IDs for each alert. **/
	private static int idCounter;

	/** The unique ID for the alert. **/
	public int id;

	/** The commit information. */
	public Commit commit;

	/**
	 * @param commit The meta and change information from the commit.
	 **/
	public FeatureVector(Commit commit) {
		this.commit = commit;
		this.id = getNextID();
	}

	/**
	 * Used for de-serializing alerts
	 * @param commit The meta information from a bulk analysis.
	 **/
	public FeatureVector(Commit commit, int id) {
		this.commit = commit;
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

	@Override
	public abstract boolean equals(Object o);

}