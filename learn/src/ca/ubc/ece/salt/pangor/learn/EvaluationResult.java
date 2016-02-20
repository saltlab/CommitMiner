package ca.ubc.ece.salt.pangor.learn;

import ca.ubc.ece.salt.pangor.learn.ClusterMetrics.ConfusionMatrix;

public class EvaluationResult {
	public ConfusionMatrix confusionMatrix;
	public double epsilon, precision, recall, fMeasure, fowlkesMallows,
			   inspected, patternRecall;

	public EvaluationResult(ConfusionMatrix confusionMatrix, double epsilon,
			double precision, double recall, double fMeasure,
			double fowlkesMallows,  double inspected, double patternRecall) {
		this.epsilon = epsilon;
		this.confusionMatrix = confusionMatrix;
		this.precision = precision;
		this.recall = recall;
		this.fMeasure = fMeasure;
		this.fowlkesMallows = fowlkesMallows;
		this.inspected = inspected;
		this.patternRecall = patternRecall;
	}

	public String getConfusionMatrix() {
		String matrix = "              \tClustered\tNot Clustered";
		matrix += "Classified    \t" + this.confusionMatrix.tp + "\t\t" + this.confusionMatrix.fn;
		matrix += "Not Classified\t" + this.confusionMatrix.fp + "\t\t" + this.confusionMatrix.tn;
		return matrix;
	}

	public String getResultsArrayHeader() {
		return "Epsilon, Precision, Recall, F-Measure, Fowlkes-Mallows, Inspected, Captured-Patterns";
	}

	public String getResultsArray() {
		String array = "";
		array += String.format("%.2f,%.2f, %.2f, %.2f, %.2f, %.2f, %.2f",
			this.epsilon, this.precision, this.recall, this.fMeasure,
			this.fowlkesMallows, this.inspected, this.patternRecall);
		return array;
	}
}