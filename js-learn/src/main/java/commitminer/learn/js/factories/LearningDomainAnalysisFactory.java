package commitminer.learn.js.factories;

import commitminer.analysis.DomainAnalysis;
import commitminer.analysis.factories.IDomainAnalysisFactory;
import commitminer.learn.js.analysis.LearningDomainAnalysis;

public class LearningDomainAnalysisFactory implements IDomainAnalysisFactory {

	@Override
	public DomainAnalysis newInstance() {

		LearningFileAnalysisFactory srcFactory = new LearningFileAnalysisFactory(true);
		LearningFileAnalysisFactory dstFactory = new LearningFileAnalysisFactory(false);

		LearningDomainAnalysis analysis = new LearningDomainAnalysis(srcFactory, dstFactory);

		return analysis;

	}

}
