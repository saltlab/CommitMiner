package commitminer.diff.line;

import java.util.LinkedList;
import java.util.List;

import commitminer.analysis.CommitAnalysis;
import commitminer.analysis.DataSet;
import commitminer.analysis.factories.ICommitAnalysisFactory;
import commitminer.analysis.factories.IDomainAnalysisFactory;

public class LineCommitAnalysisFactory implements ICommitAnalysisFactory {

	private DataSet dataSet;

	public LineCommitAnalysisFactory(DataSet dataSet) {
		this.dataSet = dataSet;
	}

	@Override
	public CommitAnalysis newInstance() {
		List<IDomainAnalysisFactory> domainFactories = new LinkedList<IDomainAnalysisFactory>();
		domainFactories.add(new LineDomainAnalysisFactory());
		return new CommitAnalysis(dataSet, domainFactories);
	}

}