package ca.uab.ece.salt.pangor.learn.eval;

import ca.ubc.ece.salt.pangor.learn.EvaluationResult;

public class PatternRecall implements Data {

	@Override
	public double[] getData(EvaluationResult[] dataSetResult) {
		double[] data = new double[dataSetResult.length];
		for(int j = 0; j < dataSetResult.length; j++) {
			data[j] = dataSetResult[j].patternRecall;
		}
		return data;
	}

	@Override
	public double[] getLim() {
		return new double[]{ 0, 1 };
	}

	@Override
	public double[] getAxp() {
		return new double[]{ 0, 1, 5 };
	}

	@Override
	public String getLabel() {
		return "Pattern Recall";
	}

}