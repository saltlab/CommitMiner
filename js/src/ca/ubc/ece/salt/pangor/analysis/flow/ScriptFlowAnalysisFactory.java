package ca.ubc.ece.salt.pangor.analysis.flow;

import java.util.List;

import ca.ubc.ece.salt.pangor.analysis.SourceCodeFileAnalysis;
import ca.ubc.ece.salt.pangor.analysis.factories.ISourceCodeFileAnalysisFactory;
import ca.ubc.ece.salt.pangor.cfg.ICFGVisitorFactory;

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
