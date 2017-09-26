package commitminer.learn.js.analysis;

import java.util.LinkedList;
import java.util.List;

import commitminer.analysis.DomainAnalysis;
import commitminer.analysis.SourceCodeFileAnalysis;
import commitminer.js.analysis.FunctionAnalysis;
import commitminer.js.analysis.ScriptAnalysis;
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
	private LearningDomainAnalysis(SourceCodeFileAnalysis srcSCFA,
								   SourceCodeFileAnalysis dstSCFA) {
		super(srcSCFA, dstSCFA, new JavaScriptCFGFactory(), false);
	}

	/**
	 * Builds a new {@code LearningAnalysis}
	 * @param maxChangeComplexity The maximum number of statements that can change in a commit.
	 * @return an analysis for extracting facts in the learning domain.
	 */
	public static LearningDomainAnalysis createLearningAnalysis() {

		List<FunctionAnalysis> srcFunctionAnalyses = new LinkedList<FunctionAnalysis>();
		List<FunctionAnalysis> dstFunctionAnalyses = new LinkedList<FunctionAnalysis>();

		srcFunctionAnalyses.add(new LearningFunctionAnalysis(false));
		dstFunctionAnalyses.add(new LearningFunctionAnalysis(true));

		SourceCodeFileAnalysis srcSCFA = new ScriptAnalysis(srcFunctionAnalyses);
		SourceCodeFileAnalysis dstSCFA = new ScriptAnalysis(dstFunctionAnalyses);

		LearningDomainAnalysis analysis = new LearningDomainAnalysis(srcSCFA, dstSCFA);

		return analysis;
	}

}