package commitminer.learn.js.factories;

import commitminer.analysis.DomainAnalysis;
import commitminer.analysis.factories.IDomainAnalysisFactory;
import commitminer.learn.js.analysis.ChangeComplexityDomainAnalysis;

public class ChangeComplexityDomainAnalysisFactory implements IDomainAnalysisFactory {

	@Override
	public DomainAnalysis newInstance() {

		ChangeComplexityFileAnalysisFactory srcFactory = new ChangeComplexityFileAnalysisFactory(true);
		ChangeComplexityFileAnalysisFactory dstFactory = new ChangeComplexityFileAnalysisFactory(false);

		return new ChangeComplexityDomainAnalysis(srcFactory, dstFactory);

	}

}
