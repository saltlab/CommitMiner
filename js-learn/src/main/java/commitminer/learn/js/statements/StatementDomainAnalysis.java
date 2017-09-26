package commitminer.learn.js.statements;

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
public class StatementDomainAnalysis extends DomainAnalysis {

	/**
	 * @param dataSet the manager that stores the feature vectors produced by this analysis.
	 * @param domainAnalyses The domains to extract facts from.
	 */
	public StatementDomainAnalysis(ISourceCodeFileAnalysisFactory srcSCFA,
							ISourceCodeFileAnalysisFactory dstSCFA) {
		super(srcSCFA, dstSCFA, new JavaScriptCFGFactory(), false, false);
	}

}