package commitminer.learn.analysis;

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
import commitminer.analysis.Commit;
import commitminer.analysis.Commit.Type;
import commitminer.analysis.FeatureVector;
import commitminer.api.KeywordDefinition;
import commitminer.api.KeywordDefinition.KeywordType;
import commitminer.api.KeywordUse;
import commitminer.api.KeywordUse.KeywordContext;

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

	/** The cluster assigned to this feature vector. **/
	public String cluster;

	/** The class that was analyzed (if at or below class granularity). **/
	public String klass;

	/** The method that was analyzed (if at or below method granularity). **/
	public String method;

	/** The number of modified statements in the feature vector. **/
	public int modifiedStatementCount;

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
		this.modifiedStatementCount = 0;
		this.keywordMap = new HashMap<KeywordUse, Integer>();
		this.cluster = "?";
	}

	/**
	 * This constructor should only be used if making a feature vector from
	 * serial. Otherwise the other constructor should be used so the ID is
	 * automatically generated.
	 * @param commit The commit that the features were extracted from.
	 * @param klass The class that the features were extracted from.
	 * @param method The method that the features were extracted from.
	 * @param id The unique id for the alert.
	 */
	public LearningFeatureVector(Commit commit, String klass, String method,
								 int modifiedStatementCount, int id) {
		super(commit, id);
		this.klass = klass;
		this.method = method;
		this.modifiedStatementCount = modifiedStatementCount;
		this.keywordMap = new HashMap<KeywordUse, Integer>();
		this.cluster = "?";
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
				+ "," + this.commit.commitMessageType.toString()
				+ "," + this.commit.url + "/commit/" + this.commit.repairedCommitID
				+ "," + this.commit.buggyCommitID + "," + this.commit.repairedCommitID
				+ "," + this.klass + "," + this.method + "," + this.modifiedStatementCount;

		/* Iterate through the keyword uses. */
		for(Entry<KeywordUse, Integer> entry : this.keywordMap.entrySet()) {
			serialized += "," + entry.getKey().toString() + ":" + entry.getValue();
		}

		return serialized;

	}

	/**
	 * This method serializes the alert with the cluster. This is useful when
	 * creating a CSV of clustered instances for evaluation.
	 * @return The serialized version of the alert including the cluster number.
	 * 		   Has the format [ID, ProjectID, URL, BuggyCommit, RepairedCommit, ClusterID, [KeywordList]]
	 * 		   where KeywordList = [Type:Context:ChangeType:Package:Keyword:COUNT].
	 */
	public String serializeWithCluster() {

		String serialized = id + "," + this.commit.projectID
				+ "," + this.commit.commitMessageType.toString()
				+ "," + this.commit.url + "/commit/" + this.commit.repairedCommitID
				+ "," + this.commit.buggyCommitID + "," + this.commit.repairedCommitID
				+ "," + this.klass + "," + this.method + "," + this.modifiedStatementCount
				+ "," + this.cluster;

		/* Iterate through the keyword uses. */
		for(Entry<KeywordUse, Integer> entry : this.keywordMap.entrySet()) {
			serialized += "," + entry.getKey().toString() + ":" + entry.getValue();
		}

		return serialized;

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

		if(features.length < 8) throw new Exception("De-serialization exception. Serial format not recognized.");

		Commit commit = new Commit(features[1], features[3], features[4],
								   features[5], Type.valueOf(features[2]));

		LearningFeatureVector featureVector = new LearningFeatureVector(commit,
				features[6], features[7], Integer.parseInt(features[8]),
				Integer.parseInt(features[0]));

		for(int i = 9; i < features.length; i++) {
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
	public Instance getWekaInstance(Instances dataSet,
									ArrayList<Attribute> attributes,
									Set<KeywordDefinition> keywords,
									double complexityWeight) {

		Instance instance = new DenseInstance(attributes.size());
		instance.setDataset(dataSet);

		/* Set the meta info for the instance. */
		instance.setValue(0, this.id);
		instance.setValue(1, this.commit.commitMessageType.toString());
		instance.setValue(2, this.commit.projectID);
		instance.setValue(3, this.commit.url);
		instance.setValue(4, this.commit.buggyCommitID);
		instance.setValue(5, this.commit.repairedCommitID);
		instance.setValue(6, this.klass);
		instance.setValue(7, this.method);
		instance.setValue(8, this.cluster); // assigned cluster
		instance.setValue(9, this.modifiedStatementCount * complexityWeight); // Weight the statement count

		/* Set the keyword values. */
		int i = 10;
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

		String vector = id + "," + this.commit.projectID + "," + this.commit.url
				+ "," + this.commit.commitMessageType
				+ "," + this.commit.buggyCommitID + "," + this.commit.repairedCommitID
				+ "," + this.klass + "," + this.method + this.modifiedStatementCount;

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