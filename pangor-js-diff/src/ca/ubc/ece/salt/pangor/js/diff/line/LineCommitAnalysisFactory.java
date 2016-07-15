package ca.ubc.ece.salt.pangor.js.diff.line;

import java.util.LinkedList;
import java.util.List;

import ca.ubc.ece.salt.pangor.analysis.CommitAnalysis;
import ca.ubc.ece.salt.pangor.analysis.DataSet;
import ca.ubc.ece.salt.pangor.analysis.factories.ICommitAnalysisFactory;
import ca.ubc.ece.salt.pangor.analysis.factories.IDomainAnalysisFactory;

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