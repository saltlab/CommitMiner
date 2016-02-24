package ca.ubc.ece.salt.pangor.learn;

import java.util.Map;

import ca.ubc.ece.salt.pangor.learn.ClusterMetrics.ConfusionMatrix;

public class EvaluationResult {

	/** The confusion matrix. **/
	public ConfusionMatrix confusionMatrix;

	/** Precision/recall metrics. **/
	public double epsilon, precision, recall, fMeasure, fowlkesMallows,
			   inspected;

	public int patternRecall;

	/**
	 * Maps the class ID to the max composition of the cluster in which it is
	 * found.
	 **/
	public Map<String, Double> clusterComposition;

	/**
	 * Maps the class ID to the max composition of the class in which it is
	 * found.
	 **/
	public Map<String, Double> classComposition;

	public EvaluationResult(ConfusionMatrix confusionMatrix, double epsilon,
			double precision, double recall, double fMeasure,
			double fowlkesMallows,  double inspected, int patternRecall,
			Map<String, Double> clusterComposition,
			Map<String, Double> classComposition) {
		this.epsilon = epsilon;
		this.confusionMatrix = confusionMatrix;
		this.precision = precision;
		this.recall = recall;
		this.fMeasure = fMeasure;
		this.fowlkesMallows = fowlkesMallows;
		this.inspected = inspected;
		this.patternRecall = patternRecall;
		this.clusterComposition = clusterComposition;
		this.classComposition = classComposition;
	}

	public String getConfusionMatrix() {
		String matrix = "              \tClustered\tNot Clustered" + "\n";
		matrix += "Classified    \t" + this.confusionMatrix.tp + "\t\t" + this.confusionMatrix.fn + "\n";
		matrix += "Not Classified\t" + this.confusionMatrix.fp + "\t\t" + this.confusionMatrix.tn + "\n";
		return matrix;
	}

	public String getResultsArrayHeader() {
		return "Class, Epsilon, Precision, Recall, FMeasure, FowlkesMallows, Inspected, CapturedPatterns";
	}

	/**
	 * @param classes A list of actual classes.
	 */
	public String getResultsArray(String[] classes) {
		String array = "";
		array += String.format("%.2f,%.2f, %.2f, %.2f, %.2f, %.2f, %s",
			this.epsilon, this.precision, this.recall, this.fMeasure,
			this.fowlkesMallows, this.inspected, this.patternRecall);

		/* Add the 'cluster/class composition' results. One for each class.
		 * Compute the average composition. */
		double totalClusterComp = 0, totalClassComp = 0, totalInst = 0;
		for(int k = 0; k < classes.length; k++) {
			Double clusterComp = this.clusterComposition.get(classes[k]);
			if(clusterComp == null) array += ",NA";
			else {
				totalClusterComp += clusterComp;
				totalInst++;
				array += String.format(",%.2f", clusterComp);
			}

			Double classComp = this.classComposition.get(classes[k]);
			if(classComp == null) array += ",NA";
			else {
				totalClassComp += classComp;
				array += String.format(",%.2f", classComp);
			}
		}
		array += String.format(",%.2f,%.2f", totalClusterComp/totalInst, totalClassComp/totalInst);
		return array;
	}
}