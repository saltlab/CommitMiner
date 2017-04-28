package commitminer.analysis.flow;

import java.util.List;

import commitminer.analysis.DomainAnalysis;
import commitminer.analysis.factories.IDomainAnalysisFactory;
import commitminer.analysis.factories.ISourceCodeFileAnalysisFactory;
import commitminer.cfg.ICFGVisitorFactory;
import commitminer.js.cfg.JavaScriptCFGFactory;

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
