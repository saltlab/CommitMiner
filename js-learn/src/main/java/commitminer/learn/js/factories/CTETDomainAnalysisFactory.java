package commitminer.learn.js.factories;

import commitminer.analysis.DomainAnalysis;
import commitminer.analysis.factories.IDomainAnalysisFactory;
import commitminer.analysis.factories.ISourceCodeFileAnalysisFactory;
import commitminer.learn.js.ctet.CTETDomainAnalysis;

public class CTETDomainAnalysisFactory implements IDomainAnalysisFactory {

	@Override
	public DomainAnalysis newInstance() {

		ISourceCodeFileAnalysisFactory srcFactory = new CTETFileAnalysisFactory(false);
		ISourceCodeFileAnalysisFactory dstFactory = new CTETFileAnalysisFactory(true);

		return new CTETDomainAnalysis(srcFactory, dstFactory);

	}

}
