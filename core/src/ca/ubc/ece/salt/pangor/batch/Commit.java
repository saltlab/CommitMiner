package ca.ubc.ece.salt.pangor.batch;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import ca.ubc.ece.salt.pangor.analysis.AntiPattern;
import ca.ubc.ece.salt.pangor.analysis.Pattern;
import ca.ubc.ece.salt.pangor.analysis.PreCondition;

/**
 * Stores the meta information for a commit. This information includes meta
 * information like the commit IDs as well as a list of individual files that
 * were modified in the commit.
 *
 * This is used by {@code CommitAnalysis} to analyze all the modified files in
 * the commit and synthesize results from the extracted facts (patterns,
 * anti-patterns and pre-conditions).
 */
public class Commit {

	/** The number of commits inspected. **/
	public int totalCommits;

	/** The number of bug fixing commits analyzed. **/
	public int commitsAnalyzed;

	/** The identifier for the project. **/
	public String projectID;

	/** The homepage for the project. **/
	public String projectHomepage;

	/** The ID for the prior commit. **/
	public String buggyCommitID;

	/** The ID for the current commit. **/
	public String repairedCommitID;

	/** The list of source code file changes that occur in this commit. */
	public List<SourceCodeFileChange> sourceCodeFileChanges;

	/** Pre-conditions found by the analysis. **/
	private Set<PreCondition> preConditions;

	/** Anti-patterns found by the analysis. **/
	private Set<AntiPattern> antiPatterns;

	/** Patterns found by the analysis. **/
	private List<Pattern> patterns;

	/**
	 * @param totalCommits The number of commits inspected.
	 * @param bugFixingCommits The number of bug fixing commits analyzed.
	 * @param projectID The identifier for the project.
	 * @param projectHomepage The homepage for the project.
	 * @param buggyCommitID The ID for the prior commit.
	 * @param repairedCommitID The ID for the current commit.
	 */
	public Commit(int totalCommits, int bugFixingCommits,
			String projectID,
			String projectHomepage,
			String buggyCommitID, String repairedCommitID) {

		this.totalCommits = totalCommits;
		this.commitsAnalyzed = bugFixingCommits;
		this.projectID = projectID;
		this.projectHomepage = projectHomepage;
		this.buggyCommitID = buggyCommitID;
		this.repairedCommitID = repairedCommitID;

		this.sourceCodeFileChanges = new LinkedList<SourceCodeFileChange>();

		/* Initialize the pattern sets. */
		this.preConditions = new HashSet<PreCondition>();
		this.antiPatterns = new HashSet<AntiPattern>();
		this.patterns = new LinkedList<Pattern>();

	}

	/**
	 * Adds a {@code SourceCodeFileChange} to be analyzed.
	 * @param scfc Two versions of a source code file.
	 */
	public void addSourceCodeFileChange(SourceCodeFileChange scfc) {
		this.sourceCodeFileChanges.add(scfc);
	}

	/**
	 * Adds a pre-condition fact.
	 * @param preCondition The pre-condition.
	 */
	protected void addPreCondition(PreCondition preCondition) {
		this.preConditions.add(preCondition);
	}

	/**
	 * @return The set of pre-condition facts generated during the analysis.
	 */
	public Set<PreCondition> getPreConditions() {
		return this.preConditions;
	}

	/**
	 * Adds an anti-pattern fact.
	 * @param antiPattern The anti-pattern.
	 */
	protected void addAntiPattern(AntiPattern antiPattern) {
		this.antiPatterns.add(antiPattern);
	}

	/**
	 * @return The set of anti-pattern facts generated during the analysis.
	 */
	public Set<AntiPattern> getAntiPatterns() {
		return this.antiPatterns;
	}

	/**
	 * Adds a pattern fact.
	 * @param pattern The pattern.
	 */
	protected void addPattern(Pattern pattern) {
		this.patterns.add(pattern);
	}

	/**
	 * @return The set of pattern facts generated during the analysis.
	 */
	public List<Pattern> getPatterns() {
		return this.patterns;
	}

	@Override
	public boolean equals(Object o) {

		if(o instanceof Commit) {

			Commit a = (Commit) o;

			if(this.projectID.equals(a.projectID)
				&& this.buggyCommitID.equals(a.buggyCommitID)
				&& this.repairedCommitID.equals(a.repairedCommitID)) {

				return true;

			}

		}
		return false;
	}

	@Override
	public int hashCode() {
		return (projectID + repairedCommitID).hashCode();
	}

}