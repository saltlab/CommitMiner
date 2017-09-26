package commitminer.learn.js.factories;

import commitminer.analysis.SourceCodeFileAnalysis;
import commitminer.analysis.factories.ISourceCodeFileAnalysisFactory;
import commitminer.learn.js.statements.StatementScriptAnalysis;

public class StatementFileAnalysisFactory implements ISourceCodeFileAnalysisFactory {

	@Override
	public SourceCodeFileAnalysis newInstance() {
		return new StatementScriptAnalysis();
	}

}
