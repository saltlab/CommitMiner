package ca.ubc.ece.salt.pangor.analysis.flow;

import ca.ubc.ece.salt.pangor.analysis.DomainAnalysis;
import ca.ubc.ece.salt.pangor.analysis.factories.IDomainAnalysisFactory;
import ca.ubc.ece.salt.pangor.analysis.factories.ISourceCodeFileAnalysisFactory;
import ca.ubc.ece.salt.pangor.js.cfg.JavaScriptCFGFactory;

public class FlowDomainAnalysisFactory implements IDomainAnalysisFactory {

	@Override
	public DomainAnalysis newInstance() {
		ISourceCodeFileAnalysisFactory srcFactory = new ScriptFlowAnalysisFactory();
		ISourceCodeFileAnalysisFactory dstFactory = new ScriptFlowAnalysisFactory();
		return new DomainAnalysis(srcFactory, dstFactory, new JavaScriptCFGFactory(), false);
	}

}
