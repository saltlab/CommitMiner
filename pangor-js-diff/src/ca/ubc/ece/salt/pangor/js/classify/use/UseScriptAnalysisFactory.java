package ca.ubc.ece.salt.pangor.js.classify.use;

import ca.ubc.ece.salt.pangor.analysis.SourceCodeFileAnalysis;
import ca.ubc.ece.salt.pangor.analysis.factories.ISourceCodeFileAnalysisFactory;

public class UseScriptAnalysisFactory implements ISourceCodeFileAnalysisFactory {

	@Override
	public SourceCodeFileAnalysis newInstance() {
		return new UseScriptAnalysis();
	}

}
