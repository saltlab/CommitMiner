package ca.ubc.ece.salt.pangor.analysis.simple;

import ca.ubc.ece.salt.pangor.analysis.DomainAnalysis;
import ca.ubc.ece.salt.pangor.analysis.factories.IDomainAnalysisFactory;
import ca.ubc.ece.salt.pangor.analysis.factories.ISourceCodeFileAnalysisFactory;

public class SimpleDomainAnalysisFactory implements IDomainAnalysisFactory {

	@Override
	public DomainAnalysis newInstance() {
		ISourceCodeFileAnalysisFactory srcFactory = new SimpleScriptAnalysisFactory();
		ISourceCodeFileAnalysisFactory dstFactory = new SimpleScriptAnalysisFactory();
		SimpleCFGFactory cfgFactory = new SimpleCFGFactory();
		return new DomainAnalysis(srcFactory, dstFactory, cfgFactory, false);
	}

}
