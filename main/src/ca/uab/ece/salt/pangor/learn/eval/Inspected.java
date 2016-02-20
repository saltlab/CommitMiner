package ca.uab.ece.salt.pangor.learn.eval;

import ca.ubc.ece.salt.pangor.learn.EvaluationResult;

class Inspected implements Data {

	@Override
	public double[] getData(EvaluationResult[] dataSetResult) {
		double[] data = new double[dataSetResult.length];
		for(int j = 0; j < dataSetResult.length; j++) {
			data[j] = dataSetResult[j].inspected;
		}
		return data;
	}

	@Override
	public double[] getLim() {
		return new double[]{ 0, 0.3 };
	}

	@Override
	public double[] getAxp() {
		return new double[]{ 0, 0.3, 3 };
	}

}