package ca.uab.ece.salt.pangor.learn.eval;

import ca.ubc.ece.salt.pangor.learn.EvaluationResult;

interface Data {
	double[] getData(EvaluationResult[] dataSetResult);
	double[] getLim();
	double[] getAxp();
	String getLabel();
}