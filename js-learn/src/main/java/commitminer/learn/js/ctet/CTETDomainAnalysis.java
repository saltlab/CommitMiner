package commitminer.learn.js.ctet;

import java.util.LinkedList;
import java.util.List;

import commitminer.analysis.DomainAnalysis;
import commitminer.analysis.SourceCodeFileAnalysis;
import commitminer.js.analysis.FunctionAnalysis;
import commitminer.js.cfg.JavaScriptCFGFactory;

/**
 * An analysis for extracting facts in the learning domain.
 *
 * Creates a data set for learning bug and repair patterns related to the use
 * of Node.js package APIs. This class will produce one feature vector for each
 * function in the analyzed script with a source and destination function.
 */
public class CTETDomainAnalysis extends DomainAnalysis {

	/**
	 * @param dataSet the manager that stores the feature vectors produced by this analysis.
	 * @param domainAnalyses The domains to extract facts from.
	 */
	private CTETDomainAnalysis(SourceCodeFileAnalysis srcSCFA,
							SourceCodeFileAnalysis dstSCFA) {
		super(srcSCFA, dstSCFA, new JavaScriptCFGFactory(), false);
	}

	/**
	 * Builds a new {@code LearningAnalysis}
	 * @param maxChangeComplexity The maximum number of statements that can change in a commit.
	 * @return an analysis for extracting facts in the learning domain.
	 */
	public static CTETDomainAnalysis createLearningAnalysis() {

		List<FunctionAnalysis> srcFunctionAnalyses = new LinkedList<FunctionAnalysis>();
		List<FunctionAnalysis> dstFunctionAnalyses = new LinkedList<FunctionAnalysis>();

		srcFunctionAnalyses.add(new CTETFunctionAnalysis(false));
		dstFunctionAnalyses.add(new CTETFunctionAnalysis(true));

		SourceCodeFileAnalysis srcSCFA = new CTETScriptAnalysis();
		SourceCodeFileAnalysis dstSCFA = new CTETScriptAnalysis();

		CTETDomainAnalysis analysis = new CTETDomainAnalysis(srcSCFA, dstSCFA);

		return analysis;
	}

}