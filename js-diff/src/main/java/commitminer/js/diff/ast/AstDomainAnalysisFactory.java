package commitminer.js.diff.ast;

import commitminer.analysis.DomainAnalysis;
import commitminer.analysis.factories.IDomainAnalysisFactory;
import commitminer.analysis.factories.ISourceCodeFileAnalysisFactory;
import commitminer.js.cfg.JavaScriptCFGFactory;

public class AstDomainAnalysisFactory implements IDomainAnalysisFactory {

	@Override
	public DomainAnalysis newInstance() {
		ISourceCodeFileAnalysisFactory srcFactory = new AstScriptAnalysisFactory();
		ISourceCodeFileAnalysisFactory dstFactory = new AstScriptAnalysisFactory();
		return new DomainAnalysis(srcFactory, dstFactory, new JavaScriptCFGFactory(), false, false);
	}

}
