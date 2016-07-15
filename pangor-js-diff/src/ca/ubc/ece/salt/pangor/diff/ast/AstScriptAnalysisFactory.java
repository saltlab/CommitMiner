package ca.ubc.ece.salt.pangor.diff.ast;

import ca.ubc.ece.salt.pangor.analysis.SourceCodeFileAnalysis;
import ca.ubc.ece.salt.pangor.analysis.factories.ISourceCodeFileAnalysisFactory;

public class AstScriptAnalysisFactory implements ISourceCodeFileAnalysisFactory {

	@Override
	public SourceCodeFileAnalysis newInstance() {
		return new AstScriptAnalysis();
	}

}
