package ca.ubc.ece.salt.pangor.js.learn;

import ca.ubc.ece.salt.pangor.cfd.ControlFlowDifferencing;
import ca.ubc.ece.salt.pangor.js.cfg.JavaScriptCFGFactory;
import ca.ubc.ece.salt.pangor.js.learn.analysis.LearningAnalysis;
import ca.ubc.ece.salt.pangor.learn.analysis.LearningDataSet;

public class LearningAnalysisRunner extends AnalysisRunner {

	/** Stores the feature vectors that make up the data set. **/
	private LearningDataSet dataset;

	/**
	 * The maximum change complexity for the file. If the change complexity for
	 * a file is greater than the maximum change complexity, the file is not
	 * analyzed and no feature vectors are generated.
	 */
	private int maxChangeComplexity;

	/**
	 * @param dataSetPath The file path to store the data set.
	 * @param supplementaryPath The directory path to store the supplementary
	 * 		  files.
	 */
	public LearningAnalysisRunner(String dataSetPath, String supplementaryPath, int maxChangeComplexity) {
		super(new JavaScriptCFGFactory());
		this.dataset = new LearningDataSet(dataSetPath, supplementaryPath);
		this.maxChangeComplexity = maxChangeComplexity;
	}

	@Override
	protected void analyze(ControlFlowDifferencing cfd, AnalysisMetaInformation ami) throws Exception {

		LearningAnalysis analysis = new LearningAnalysis(this.dataset, ami, this.maxChangeComplexity);
		cfd.analyze(analysis);

	}

}