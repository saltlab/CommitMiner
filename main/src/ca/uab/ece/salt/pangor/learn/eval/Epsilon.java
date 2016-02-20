package ca.uab.ece.salt.pangor.learn.eval;

import ca.ubc.ece.salt.pangor.learn.EvaluationResult;

class Epsilon implements Data {

	@Override
	public double[] getData(EvaluationResult[] dataSetResult) {
		double[] data = new double[dataSetResult.length];
		for(int j = 0; j < dataSetResult.length; j++) {
			data[j] = Math.round(dataSetResult[j].epsilon*100.0)/100.0;
		}
		return data;
	}

	@Override
	public double[] getLim() {
		return new double[]{ 0.1, 5.9 };
	}

	@Override
	public double[] getAxp() {
		return new double[]{ 0.1, 5.9, 29 };
	}

}