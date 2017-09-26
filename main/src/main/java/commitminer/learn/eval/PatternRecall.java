package commitminer.learn.eval;

import commitminer.learn.EvaluationResult;

public class PatternRecall implements Data {

	@Override
	public String[] getData(EvaluationResult[] dataSetResult) {
		String[] data = new String[dataSetResult.length];
		for(int j = 0; j < dataSetResult.length; j++) {
			data[j] = String.valueOf(dataSetResult[j].patternRecall);
		}
		return data;
	}

	@Override
	public String[] getLim() {
		return new String[]{ "0.0", "1.0" };
	}

	@Override
	public String[] getAxp() {
		return new String[]{ "0.0", "1.0", "5.0" };
	}

	@Override
	public String getLabel() {
		return "Pattern Recall";
	}

}