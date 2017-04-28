package commitminer.analysis.simple;

import commitminer.analysis.DomainAnalysis;
import commitminer.analysis.factories.IDomainAnalysisFactory;
import commitminer.analysis.factories.ISourceCodeFileAnalysisFactory;

public class SimpleDomainAnalysisFactory implements IDomainAnalysisFactory {

	@Override
	public DomainAnalysis newInstance() {
		ISourceCodeFileAnalysisFactory srcFactory = new SimpleScriptAnalysisFactory();
		ISourceCodeFileAnalysisFactory dstFactory = new SimpleScriptAnalysisFactory();
		SimpleCFGFactory cfgFactory = new SimpleCFGFactory();
		return new DomainAnalysis(srcFactory, dstFactory, cfgFactory, false);
	}

}
