package commitminer.learn.js.analysis;

import commitminer.analysis.DomainAnalysis;
import commitminer.analysis.factories.ISourceCodeFileAnalysisFactory;
import commitminer.js.cfg.JavaScriptCFGFactory;

/**
 * An analysis for extracting facts in the learning domain.
 *
 * Creates a data set for learning bug and repair patterns related to the use
 * of Node.js package APIs. This class will produce one feature vector for each
 * function in the analyzed script with a source and destination function.
 */
public class LearningDomainAnalysis extends DomainAnalysis {

	/**
	 * @param srcAnalysis the source analysis the anlaysis runs to extract domain facts
	 * @param dstAnalysis the destination analysis the anlaysis runs to extract domain facts
	 */
	public LearningDomainAnalysis(ISourceCodeFileAnalysisFactory srcSCFA,
								   ISourceCodeFileAnalysisFactory dstSCFA) {
		super(srcSCFA, dstSCFA, new JavaScriptCFGFactory(), false, false);
	}

}