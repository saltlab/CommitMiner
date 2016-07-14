package ca.ubc.ece.salt.pangor.js.classify.use;

import ca.ubc.ece.salt.pangor.analysis.DomainAnalysis;
import ca.ubc.ece.salt.pangor.analysis.factories.IDomainAnalysisFactory;
import ca.ubc.ece.salt.pangor.analysis.factories.ISourceCodeFileAnalysisFactory;
import ca.ubc.ece.salt.pangor.js.cfg.JavaScriptCFGFactory;

public class UseDomainAnalysisFactory implements IDomainAnalysisFactory {

	@Override
	public DomainAnalysis newInstance() {
		ISourceCodeFileAnalysisFactory srcFactory = new UseScriptAnalysisFactory();
		ISourceCodeFileAnalysisFactory dstFactory = new UseScriptAnalysisFactory();
		return new DomainAnalysis(srcFactory, dstFactory, new JavaScriptCFGFactory(), false);
	}

}
