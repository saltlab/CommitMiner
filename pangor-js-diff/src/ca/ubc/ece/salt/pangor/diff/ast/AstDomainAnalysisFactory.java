package ca.ubc.ece.salt.pangor.diff.ast;

import ca.ubc.ece.salt.pangor.analysis.DomainAnalysis;
import ca.ubc.ece.salt.pangor.analysis.factories.IDomainAnalysisFactory;
import ca.ubc.ece.salt.pangor.analysis.factories.ISourceCodeFileAnalysisFactory;
import ca.ubc.ece.salt.pangor.js.cfg.JavaScriptCFGFactory;

public class AstDomainAnalysisFactory implements IDomainAnalysisFactory {

	@Override
	public DomainAnalysis newInstance() {
		ISourceCodeFileAnalysisFactory srcFactory = new AstScriptAnalysisFactory();
		ISourceCodeFileAnalysisFactory dstFactory = new AstScriptAnalysisFactory();
		return new DomainAnalysis(srcFactory, dstFactory, new JavaScriptCFGFactory(), false);
	}

}
