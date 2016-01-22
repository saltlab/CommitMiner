package ca.ubc.ece.salt.pangor.learn.analysis;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import weka.core.Attribute;
import weka.core.DenseInstance;
import weka.core.Instance;
import weka.core.Instances;
import ca.ubc.ece.salt.gumtree.ast.ClassifiedASTNode.ChangeType;
import ca.ubc.ece.salt.pangor.analysis.Commit;
import ca.ubc.ece.salt.pangor.analysis.FeatureVector;
import ca.ubc.ece.salt.pangor.learn.api.KeywordDefinition;
import ca.ubc.ece.salt.pangor.learn.api.KeywordDefinition.KeywordType;
import ca.ubc.ece.salt.pangor.learn.api.KeywordUse;
import ca.ubc.ece.salt.pangor.learn.api.KeywordUse.KeywordContext;
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
public class LearningFeatureVector extends FeatureVector {

	/** The class that was analyzed (if at or below class granularity). **/
	public String klass;

	/** The method that was analyzed (if at or below method granularity). **/
	public String method;

	/** The statement counts in each commit, class or method (depending on desired granularity). **/
	public Map<StatementUse, Integer> statementMap;

	/** The keyword counts in each commit, class or method (depending on desired granularity). **/
	public Map<KeywordUse, Integer> keywordMap;

	/**
	 * @param commit The commit that the features were extracted from.
	 * @param klass The class that the features were extracted from.
	 * @param method The method that the features were extracted from.
	 */
	public LearningFeatureVector(Commit commit, String klass, String method) {
		super(commit);
		this.klass = klass;
		this.method = method;
		this.statementMap = new HashMap<StatementUse, Integer>();
		this.keywordMap = new HashMap<KeywordUse, Integer>();
	}

	/**
	 * An alert is always associated a concrete Checker. This constructor
	 * should only be used if making a LearningAlert from serial. Otherwise
	 * the other constructor should be used so the ID is automatically
	 * generated.
	 * @param commit The commit that the features were extracted from.
	 * @param klass The class that the features were extracted from.
	 * @param method The method that the features were extracted from.
	 * @param id The unique id for the alert.
	 */
	public LearningFeatureVector(Commit commit, String klass, String method, int id) {
		super(commit, id);
		this.klass = klass;
		this.method = method;
		this.statementMap = new HashMap<StatementUse, Integer>();
		this.keywordMap = new HashMap<KeywordUse, Integer>();
	}

	/**
	 * This method serializes the alert. This is useful when writing
	 * a data set to the disk.
	 * @return The serialized version of the alert.
	 * 		   Has the format [ID, ProjectID, URL, BuggyCommit, RepairedCommit, [KeywordList]]
	 * 		   where KeywordList = [Type:Context:ChangeType:Package:Keyword:COUNT].
	 */
	public String serialize() {

		String serialized = id + "," + this.commit.projectID
				+ "," + this.commit.url + "/commit/" + this.commit.repairedCommitID
				+ "," + this.commit.buggyCommitID + "," + this.commit.repairedCommitID
				+ "," + this.klass + "," + this.method;

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

	/**
	 * If the given token is a statement, that statement's count is incremented by
	 * one.
	 * @param token The string to check against the statement list.
	 */
	public void addStatement(StatementUse statement) {

		Integer count = this.statementMap.containsKey(statement) ? this.statementMap.get(statement) + 1 : 1;
		this.statementMap.put(statement,  count);

	}

	/**
	 * Add the statement to the feature vector and set its count.
	 * @param token The string to check against the statement list.
	 */
	public void addStatement(StatementUse statement, Integer count) {

		this.statementMap.put(statement,  count);

	}

	/**
	 * If the given token is a keyword, that keyword's count is incremented by
	 * one.
	 * @param token The string to check against the keyword list.
	 */
	public void addKeyword(KeywordUse keyword) {

		Integer count = this.keywordMap.containsKey(keyword) ? this.keywordMap.get(keyword) + 1 : 1;
		this.keywordMap.put(keyword,  count);

	}

	/**
	 * Add the keyword to the feature vector and set its count.
	 * @param token The string to check against the keyword list.
	 */
	public void addKeyword(KeywordUse keyword, Integer count) {

		this.keywordMap.put(keyword,  count);

	}

	/**
	 * This method de-serializes a feature vector. This is useful when reading
	 * a data set from the disk.
	 * @param serialized The serialized version of a feature vector.
	 * @return The feature vector represented by {@code serialized}.
	 */
	public static LearningFeatureVector deSerialize(String serialized) throws Exception {

		String[] features = serialized.split(",");

		if(features.length < 7) throw new Exception("De-serialization exception. Serial format not recognized.");

		Commit commit = new Commit(features[1], features[2], features[3],
								   features[4]);

		LearningFeatureVector featureVector = new LearningFeatureVector(commit, features[5], features[6], Integer.parseInt(features[0]));

		for(int i = 7; i < features.length; i++) {
			String[] feature = features[i].split(":");
			if(feature.length < 6) throw new Exception("De-serialization exception. Serial format not recognized.");
			KeywordUse keyword = new KeywordUse(KeywordType.valueOf(feature[0]),
												KeywordContext.valueOf(feature[1]),
												feature[4],
												ChangeType.valueOf(feature[2]),
												feature[3]);
			featureVector.addKeyword(keyword, Integer.parseInt(feature[5]));
		}

		return featureVector;

	}

	/**
	 * Converts this feature vector into a Weka Instance.
	 * @return This feature vector as a Weka Instance
	 */
	public Instance getWekaInstance(Instances dataSet, ArrayList<Attribute> attributes, Set<KeywordDefinition> keywords) {

		Instance instance = new DenseInstance(attributes.size());
		instance.setDataset(dataSet);

		/* Set the meta info for the instance. */
		instance.setValue(0, this.id);
		instance.setValue(1, this.commit.projectID);
		instance.setValue(2, this.commit.url);
		instance.setValue(3, this.commit.buggyCommitID);
		instance.setValue(4, this.commit.repairedCommitID);
		instance.setValue(5, this.klass);
		instance.setValue(6, this.method);
		instance.setValue(7, "?"); // assigned cluster

		/* Set the keyword values. */
		int i = 8;
		for(KeywordDefinition keyword : keywords) {
			if(this.keywordMap.containsKey(keyword)) {
				instance.setValue(i, this.keywordMap.get(keyword));
			}
			else {
				instance.setValue(i, 0);
			}
			i++;
		}

		return instance;

	}

	/**
	 * Prints the meta features and the specified keyword values in the order they are provided.
	 * @param keywords An ordered list of the keywords to print in the feature vector.
	 * @return the CSV row (the feature vector) as a string.
	 */
	public String getFeatureVector(Set<KeywordDefinition> keywords) {

		String vector = id + "," + this.commit.projectID + "," + this.commit.url + ","
				+ this.commit.buggyCommitID + "," + this.commit.repairedCommitID
				+ "," + this.klass + "," + this.method;

		for(KeywordDefinition keyword : keywords) {
			if(this.keywordMap.containsKey(keyword)) vector += "," + this.keywordMap.get(keyword).toString();
			else vector += ",0";
		}

		return vector;

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