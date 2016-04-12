package ca.ubc.ece.salt.pangor.js.classify.sth;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.deri.iris.api.basics.IPredicate;
import org.deri.iris.storage.IRelation;

import ca.ubc.ece.salt.pangor.analysis.Commit;
import ca.ubc.ece.salt.pangor.analysis.DomainAnalysis;
import ca.ubc.ece.salt.pangor.analysis.SourceCodeFileAnalysis;
import ca.ubc.ece.salt.pangor.js.analysis.FunctionAnalysis;
import ca.ubc.ece.salt.pangor.js.analysis.ScriptAnalysis;
import ca.ubc.ece.salt.pangor.js.cfg.JavaScriptCFGFactory;

public class STHDomainAnalysis extends DomainAnalysis {

	/**
	 * @param srcAnalysis the source analysis the anlaysis runs to extract domain facts
	 * @param dstAnalysis the destination analysis the anlaysis runs to extract domain facts
	 */
	private STHDomainAnalysis(SourceCodeFileAnalysis srcAnalysis,
							 SourceCodeFileAnalysis dstAnalysis) {
		super(srcAnalysis, dstAnalysis, new JavaScriptCFGFactory(), true);
	}

	@Override
	protected boolean preAnalysis(Commit commit, Map<IPredicate, IRelation> facts) throws Exception {
		/* Set up the stronger/weaker relations. */
		return true;
	}

	/**
	 * Builds a new {@code LearningAnalysis}
	 * @param maxChangeComplexity The maximum number of statements that can change in a commit.
	 * @return an analysis for extracting facts in the learning domain.
	 */
	public static STHDomainAnalysis createLearningAnalysis() {

		List<FunctionAnalysis> srcFunctionAnalyses = new LinkedList<FunctionAnalysis>();
		List<FunctionAnalysis> dstFunctionAnalyses = new LinkedList<FunctionAnalysis>();

		srcFunctionAnalyses.add(new STHFlowAnalysis());
		dstFunctionAnalyses.add(new STHFlowAnalysis());

		SourceCodeFileAnalysis srcSCFA = new ScriptAnalysis(srcFunctionAnalyses);
		SourceCodeFileAnalysis dstSCFA = new ScriptAnalysis(dstFunctionAnalyses);

		STHDomainAnalysis analysis = new STHDomainAnalysis(srcSCFA, dstSCFA);

		return analysis;
	}

}
