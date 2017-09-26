package commitminer.learn.js.factories;

import commitminer.analysis.DomainAnalysis;
import commitminer.analysis.factories.IDomainAnalysisFactory;
import commitminer.analysis.factories.ISourceCodeFileAnalysisFactory;
import commitminer.learn.js.statements.StatementDomainAnalysis;

public class StatementDomainAnalysisFactory implements IDomainAnalysisFactory {

	@Override
	public DomainAnalysis newInstance() {

		ISourceCodeFileAnalysisFactory srcFactory = new StatementFileAnalysisFactory();
		ISourceCodeFileAnalysisFactory dstFactory = new StatementFileAnalysisFactory();

		return new StatementDomainAnalysis(srcFactory, dstFactory);

	}

}
