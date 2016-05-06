package ca.ubc.ece.salt.pangor.analysis.flow;

import ca.ubc.ece.salt.pangor.analysis.DomainAnalysis;
import ca.ubc.ece.salt.pangor.analysis.SourceCodeFileAnalysis;
import ca.ubc.ece.salt.pangor.js.cfg.JavaScriptCFGFactory;

public class FlowDomainAnalysis extends DomainAnalysis {

	/**
	 * @param srcAnalysis the source analysis the anlaysis runs to extract domain facts
	 * @param dstAnalysis the destination analysis the anlaysis runs to extract domain facts
	 */
	private FlowDomainAnalysis(SourceCodeFileAnalysis srcAnalysis,
							 SourceCodeFileAnalysis dstAnalysis) {
		super(srcAnalysis, dstAnalysis, new JavaScriptCFGFactory(), true);
	}

	/**
	 * Builds a new {@code LearningAnalysis}
	 * @param maxChangeComplexity The maximum number of statements that can change in a commit.
	 * @return an analysis for extracting facts in the learning domain.
	 */
	public static FlowDomainAnalysis createFlowDomainAnalysis() {

		SourceCodeFileAnalysis srcSCFA = new ScriptFlowAnalysis();
		SourceCodeFileAnalysis dstSCFA = new ScriptFlowAnalysis();

		FlowDomainAnalysis analysis = new FlowDomainAnalysis(srcSCFA, dstSCFA);

		return analysis;

	}

}
