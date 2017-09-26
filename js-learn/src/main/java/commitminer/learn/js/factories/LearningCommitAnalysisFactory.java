package commitminer.learn.js.factories;

import java.util.LinkedList;
import java.util.List;

import commitminer.analysis.CommitAnalysis;
import commitminer.analysis.factories.ICommitAnalysisFactory;
import commitminer.analysis.factories.IDomainAnalysisFactory;

public class LearningCommitAnalysisFactory implements ICommitAnalysisFactory {
	
	@Override
	public CommitAnalysis newInstance() {
		
		/* Set up the analysis. */
		List<IDomainAnalysisFactory> domainAnalysisFactories = new LinkedList<IDomainAnalysisFactory>();
		domainAnalysisFactories.add(new LearningDomainAnalysisFactory());
		domainAnalysisFactories.add(new ChangeComplexityDomainAnalysisFactory());

		/* Set up the commit analysis. */
		return new CommitAnalysis(domainAnalysisFactories);

	}

}
