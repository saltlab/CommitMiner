package ca.ubc.ece.salt.pangor.analysis.classify;

import ca.ubc.ece.salt.pangor.analysis.Commit;
import ca.ubc.ece.salt.pangor.analysis.SourceCodeFileChange;

/**
 * A special alert that stores the information from a de-serialized alert.
 * Useful when reading and filtering a data set of classifier alerts.
 */
public class DeserializedClassifierAlert extends ClassifierAlert {

	/**
	 * Describes the details of the alert, such as what objects and functions
	 * are involved.
	 */
	private String description;

	/**
	 * Explains what the alert means, why it was produced, how to repair, etc.
	 */
	private String explanation;

	public DeserializedClassifierAlert(Commit commit, SourceCodeFileChange sourceCodeFileChange,
			String functionName, String type, String subtype, String description,
			String explanation, int id) {
		super(commit, sourceCodeFileChange, functionName, type, subtype, id);
		this.description = description;
		this.explanation = explanation;
	}

	/**
	 * This method de-serializes a classifier alert. This is useful when reading
	 * a data set from the disk.
	 * @param serialized The serialized version of a classifier alert.
	 * @return The classifier alert represented by {@code serialized}.
	 */
	public static DeserializedClassifierAlert deSerialize(String serialized) throws Exception {

		String[] features = serialized.split(",");

		if(features.length < 12) throw new Exception("De-serialization exception. Serial format not recognized.");

		Commit commit = new Commit(-1, -1, features[1], features[2], features[3], features[4]);

		DeserializedClassifierAlert classifierAlert = new DeserializedClassifierAlert(
				commit, null, features[7], features[8], features[9], features[10], features[11],
				Integer.parseInt(features[0]));

		return classifierAlert;

	}

	@Override
	protected String getAlertDescription() {
		return this.description;
	}

	@Override
	protected String getAlertExplanation() {
		return this.explanation;
	}

	@Override
	public boolean equals(Object o) {
		if(o instanceof DeserializedClassifierAlert) {
			DeserializedClassifierAlert a = (DeserializedClassifierAlert) o;
			return this.commit.equals(a.commit) && this.getLongDescription().equals(a.getLongDescription());
		}
		return false;
	}

	@Override
	public int hashCode() {
		return this.commit.hashCode();
	}

}