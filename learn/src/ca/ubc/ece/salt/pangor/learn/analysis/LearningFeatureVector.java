package ca.ubc.ece.salt.pangor.learn.analysis;

import java.util.Map;
import java.util.Map.Entry;

import ca.ubc.ece.salt.pangor.analysis.Commit;
import ca.ubc.ece.salt.pangor.analysis.FeatureVector;
import ca.ubc.ece.salt.pangor.learn.api.KeywordUse;
import ca.ubc.ece.salt.pangor.learn.api.StatementUse;

/**
 * Stores a feature vector of keyword changes in a commit
 *
 * The feature vector includes meta information. The meta information (e.g.,
 * project, commit #, file names, etc.) is used to separate projects and during
 * manual examination of the pervasive defect classes discovered during mining.
 *
 * The feature vector also includes statement changes and keyword changes.
 * Statement changes store information about changes that were made to the
 * class or script at the statement level. Keyword changes store information
 * about changes that were made to the class or script at the keyword level.
 *
 * Statement changes and keyword changes are represented by the analysis in
 * two Datalog facts:
 * 	- StatementChange(class, method, change type, statement type)
 * 	- KeywordChange(class, method, type, context, change type, package, name)
 */
public abstract class LearningFeatureVector extends FeatureVector {

	/** The class that was analyzed (if at or below class granularity). **/
	public String klass;

	/** The method that was analyzed (if at or below method granularity). **/
	public String method;

	/** The statement counts in each commit, class or method (depending on desired granularity). **/
	public Map<StatementUse, Integer> statementMap;

	/** The keyword counts in each commit, class or method (depending on desired granularity). **/
	public Map<KeywordUse, Integer> keywordMap;

	/**
	 * An alert is always associated a concrete Checker.
	 * @param commit
	 * @param sourceCodeFileChange
	 * @param type The checker which generated the alert.
	 * @param subtype A checker may detect more than one repair subtype.
	 */
	public LearningFeatureVector(Commit commit, String project,
								 String srcCommit, String dstCommit,
								 String url,
								 String klass, String method,
								 Map<StatementUse, Integer> statementMap,
								 Map<KeywordUse, Integer> keywordMap) {
		super(commit);
		this.klass = klass;
		this.method = method;
		this.statementMap = statementMap;
		this.keywordMap = keywordMap;
	}

	/**
	 * An alert is always associated a concrete Checker. This constructor
	 * should only be used if making a LearningAlert from serial. Otherwise
	 * the other constructor should be used so the ID is automatically
	 * generated.
	 * @param commit
	 * @param sourceCodeFileChange
	 * @param type The checker which generated the alert.
	 * @param subtype A checker may detect more than one repair subtype.
	 * @param id The unique id for the alert.
	 */
	public LearningFeatureVector(Commit commit, int id) {
		super(commit, id);
	}

	/**
	 * This method serializes the alert. This is useful when writing
	 * a data set to the disk.
	 * @return The serialized version of the alert.
	 */
	public String serialize() {

		String serialized = id + "," + this.commit.projectID
				+ "," + this.commit.url + "/commit/" + this.commit.repairedCommitID
				+ "," + this.commit.buggyCommitID + "," + this.commit.repairedCommitID;

		/* Iterate through the keyword uses. */
		for(Entry<KeywordUse, Integer> entry : this.keywordMap.entrySet()) {
			serialized += "," + entry.getKey().toString() + ":" + entry.getValue();
		}

		/* Iterate through the statement uses. */
		for(Entry<StatementUse, Integer> entry : this.statementMap.entrySet()) {
			serialized += "," + entry.getKey().toString() + ":" + entry.getValue();
		}

		return serialized;

	}

	@Override
	public String toString() {
		return this.serialize();
	}

	@Override
	public boolean equals(Object o) {
		if(o instanceof LearningFeatureVector) {
			LearningFeatureVector sa = (LearningFeatureVector)o;
			if(this.commit.equals(sa.commit)
					&& this.klass.equals(sa.klass)
					&& this.method.equals(sa.method)) return true;
		}
		return false;
	}

	@Override
	public int hashCode() {
		return (this.commit.projectID + this.commit.repairedCommitID + this.klass + this.method).hashCode();
	}

}