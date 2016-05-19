package ca.ubc.ece.salt.pangor.analysis.flow;

import ca.ubc.ece.salt.pangor.analysis.SourceCodeFileAnalysis;
import ca.ubc.ece.salt.pangor.analysis.factories.ISourceCodeFileAnalysisFactory;

public class ScriptFlowAnalysisFactory implements
		ISourceCodeFileAnalysisFactory {

	@Override
	public SourceCodeFileAnalysis newInstance() {
		return new ScriptFlowAnalysis();
	}

}
