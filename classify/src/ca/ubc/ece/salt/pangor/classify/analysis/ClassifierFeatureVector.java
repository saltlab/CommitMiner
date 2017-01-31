package ca.ubc.ece.salt.pangor.classify.analysis;

import ca.ubc.ece.salt.pangor.analysis.Commit;
import ca.ubc.ece.salt.pangor.analysis.Commit.Type;
import ca.ubc.ece.salt.pangor.analysis.FeatureVector;

/**
 * Stores a feature vector for a pattern query.
 *
 * The feature vector includes information that can be used to localize the
 * pattern, and a description of the patterns itself.
 *
 * Patterns must be specified as Datalog queries passed to the constructor.
 */
public class ClassifierFeatureVector extends FeatureVector {

	/** The version of the file (SOURCE or DESTINATION) **/
	public String version;

	/** The class that was analyzed (if at or below class granularity). **/
	public String klass;

	/** The method that was analyzed (if at or below method granularity). **/
	public String method;

	/** The line number for the alert. **/
	public String line;

	/** The absolute position of the alert in the file. **/
	public String absolutePosition;

	/** The length of the highlighting in the file. **/
	public String length;

	/** The type of pattern found. **/
	public String type;

	/** The subtype of pattern found. **/
	public String subtype;

	/** A description of the pattern found. **/
	public String description;

	/**
	 * @param commit The commit that the features were extracted from.
	 * @param klass The class that the features were extracted from.
	 * @param method The method that the features were extracted from.
	 */
	@Deprecated
	public ClassifierFeatureVector(Commit commit, String version,
								   String klass, String method,
								   String line,
								   String type, String subtype,
								   String description) {
		super(commit);
		this.version = version;
		this.klass = klass;
		this.method = method;
		this.line = line;
		this.absolutePosition = "0";
		this.length = "0";
		this.type = type;
		this.subtype = subtype;
		this.description = description;
	}

	/**
	 * @param commit The commit that the features were extracted from.
	 * @param klass The class that the features were extracted from.
	 * @param method The method that the features were extracted from.
	 */
	public ClassifierFeatureVector(Commit commit, String version,
								   String klass, String method,
								   String line,
								   String absolutePosition,
								   String length,
								   String type, String subtype,
								   String description) {
		super(commit);
		this.version = version;
		this.klass = klass;
		this.method = method;
		this.line = line;
		this.absolutePosition = absolutePosition;
		this.length = length;
		this.type = type;
		this.subtype = subtype;
		this.description = description;
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
	public ClassifierFeatureVector(Commit commit, String version,
								   String klass, String method,
								   String line, String type, String subtype,
								   String description, int id) {
		super(commit, id);
		this.version = version;
		this.klass = klass;
		this.method = method;
		this.line = line;
		this.type = type;
		this.subtype = subtype;
		this.description = description;
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
				+ "," + this.version
				+ "," + this.klass + "," + this.method
				+ "," + this.line
				+ "," + this.absolutePosition
				+ "," + this.length
				+ "," + this.type
				+ "," + this.subtype + "," + this.description;

		return serialized;

	}

	/**
	 * This method de-serializes a feature vector. This is useful when reading
	 * a data set from the disk.
	 * @param serialized The serialized version of a feature vector.
	 * @return The feature vector represented by {@code serialized}.
	 */
	public static ClassifierFeatureVector deSerialize(String serialized) throws Exception {

		String[] features = serialized.split(",");

		if(features.length < 8) throw new Exception("De-serialization exception. Serial format not recognized.");

		Commit commit = new Commit(features[1], features[3], features[4],
								   features[5], Type.valueOf(features[2]));

		ClassifierFeatureVector featureVector = new ClassifierFeatureVector(commit,
				features[6], features[7],
				features[8], features[9], features[10],
				features[11], features[12],
				Integer.parseInt(features[0]));

		return featureVector;

	}

	@Override
	public String toString() {
		return this.serialize();
	}

	@Override
	public boolean equals(Object o) {
		if(o instanceof ClassifierFeatureVector) {
			ClassifierFeatureVector sa = (ClassifierFeatureVector)o;
			if(this.commit.equals(sa.commit)
					&& this.version.equals(sa.version)
					&& this.klass.equals(sa.klass)
					&& this.method.equals(sa.method)
					&& this.line.equals(sa.line)
					&& this.absolutePosition.equals(sa.absolutePosition)
					&& this.length.equals(sa.length)
					&& this.type.equals(sa.type)
					&& this.subtype.equals(sa.subtype)
					&& this.description.equals(sa.description)) return true;
		}
		return false;
	}

	@Override
	public int hashCode() {
		return (this.commit.projectID + this.commit.repairedCommitID + this.klass + this.method).hashCode();
	}

}