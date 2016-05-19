package ca.ubc.ece.salt.pangor.analysis.flow;

import java.util.List;

import ca.ubc.ece.salt.pangor.analysis.DomainAnalysis;
import ca.ubc.ece.salt.pangor.analysis.factories.IDomainAnalysisFactory;
import ca.ubc.ece.salt.pangor.analysis.factories.ISourceCodeFileAnalysisFactory;
import ca.ubc.ece.salt.pangor.cfg.ICFGVisitorFactory;
import ca.ubc.ece.salt.pangor.js.cfg.JavaScriptCFGFactory;

public class FlowDomainAnalysisFactory implements IDomainAnalysisFactory {

	public List<ICFGVisitorFactory> cfgVisitorFactories;

	public FlowDomainAnalysisFactory(List<ICFGVisitorFactory> cfgVisitorFactories) {
		this.cfgVisitorFactories = cfgVisitorFactories;
	}

	@Override
	public DomainAnalysis newInstance() {
		ISourceCodeFileAnalysisFactory srcFactory = new ScriptFlowAnalysisFactory(cfgVisitorFactories);
		ISourceCodeFileAnalysisFactory dstFactory = new ScriptFlowAnalysisFactory(cfgVisitorFactories);
		return new DomainAnalysis(srcFactory, dstFactory, new JavaScriptCFGFactory(), false);
	}

}
