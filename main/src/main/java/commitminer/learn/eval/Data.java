package commitminer.learn.eval;

import commitminer.learn.EvaluationResult;

interface Data {
	String[] getData(EvaluationResult[] dataSetResult);
	String[] getLim();
	String[] getAxp();
	String getLabel();
}