package commitminer.js.diff.ast;

import commitminer.analysis.SourceCodeFileAnalysis;
import commitminer.analysis.factories.ISourceCodeFileAnalysisFactory;

public class AstScriptAnalysisFactory implements ISourceCodeFileAnalysisFactory {

	@Override
	public SourceCodeFileAnalysis newInstance() {
		return new AstScriptAnalysis();
	}

}
