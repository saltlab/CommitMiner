package commitminer.analysis.simple;

import java.util.LinkedList;
import java.util.List;

import commitminer.analysis.CommitAnalysis;
import commitminer.analysis.DataSet;
import commitminer.analysis.factories.ICommitAnalysisFactory;
import commitminer.analysis.factories.IDomainAnalysisFactory;

public class SimpleCommitAnalysisFactory implements ICommitAnalysisFactory {

	public SimpleCommitAnalysisFactory(DataSet dataSet) { }

	@Override
	public CommitAnalysis newInstance() {
		List<IDomainAnalysisFactory> domainFactories = new LinkedList<IDomainAnalysisFactory>();
		return new CommitAnalysis(domainFactories);
	}

}
