package ca.ubc.ece.salt.pangor.js.classify.use;

import java.util.LinkedList;
import java.util.List;

import ca.ubc.ece.salt.pangor.analysis.CommitAnalysis;
import ca.ubc.ece.salt.pangor.analysis.DataSet;
import ca.ubc.ece.salt.pangor.analysis.factories.ICommitAnalysisFactory;
import ca.ubc.ece.salt.pangor.analysis.factories.IDomainAnalysisFactory;

public class UseCommitAnalysisFactory implements ICommitAnalysisFactory {

	private DataSet dataSet;

	public UseCommitAnalysisFactory(DataSet dataSet) {
		this.dataSet = dataSet;
	}

	@Override
	public CommitAnalysis newInstance() {
		List<IDomainAnalysisFactory> domainFactories = new LinkedList<IDomainAnalysisFactory>();
		domainFactories.add(new UseDomainAnalysisFactory());
		return new CommitAnalysis(dataSet, domainFactories);
	}

}