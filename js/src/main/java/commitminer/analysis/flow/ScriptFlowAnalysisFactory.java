package commitminer.analysis.flow;

import java.util.List;

import commitminer.analysis.SourceCodeFileAnalysis;
import commitminer.analysis.factories.ISourceCodeFileAnalysisFactory;
import commitminer.cfg.ICFGVisitorFactory;

public class ScriptFlowAnalysisFactory implements
		ISourceCodeFileAnalysisFactory {

	public List<ICFGVisitorFactory> cfgVisitorFactories;

	public ScriptFlowAnalysisFactory(List<ICFGVisitorFactory> cfgVisitorFactories) {
		this.cfgVisitorFactories = cfgVisitorFactories;
	}

	@Override
	public SourceCodeFileAnalysis newInstance() {
		return new ScriptFlowAnalysis(this.cfgVisitorFactories);
	}

}
