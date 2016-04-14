package ca.ubc.ece.salt.pangor.js.classify.use;

import ca.ubc.ece.salt.pangor.analysis.DomainAnalysis;
import ca.ubc.ece.salt.pangor.analysis.SourceCodeFileAnalysis;
import ca.ubc.ece.salt.pangor.js.cfg.JavaScriptCFGFactory;

/**
 * Extracts variable use facts.
 *
 * Example program:
 * s1	a.foo();
 * s2	var x = b;
 *
 * Facts derived:
 * 	Use(s1, dst, a)
 * 	Use(s1, dst, a.foo)
 * 	Use(s2, dst, b)
 */
public class UseDomainAnalysis extends DomainAnalysis {

	/**
	 * @param srcAnalysis the source analysis the anlaysis runs to extract domain facts
	 * @param dstAnalysis the destination analysis the anlaysis runs to extract domain facts
	 */
	private UseDomainAnalysis(SourceCodeFileAnalysis srcAnalysis,
							 SourceCodeFileAnalysis dstAnalysis) {
		super(srcAnalysis, dstAnalysis, new JavaScriptCFGFactory(), true);
	}



	/**
	 * Builds a new {@code UseDomainAnalysis}
	 * @return an analysis for extracting facts about identifier usage.
	 */
	public static UseDomainAnalysis createLearningAnalysis() {

		SourceCodeFileAnalysis srcSCFA = new UseScriptAnalysis();
		SourceCodeFileAnalysis dstSCFA = new UseScriptAnalysis();

		UseDomainAnalysis analysis = new UseDomainAnalysis(srcSCFA, dstSCFA);

		return analysis;
	}

}
