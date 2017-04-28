package commitminer.analysis.simple;

import commitminer.analysis.SourceCodeFileAnalysis;
import commitminer.analysis.factories.ISourceCodeFileAnalysisFactory;

public class SimpleScriptAnalysisFactory implements
		ISourceCodeFileAnalysisFactory {

	@Override
	public SourceCodeFileAnalysis newInstance() {
		return new SimpleSrcFileAnalysis();
	}

}
