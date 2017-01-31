package ca.ubc.ece.salt.pangor.js.diff.typeerror;

import java.util.LinkedList;
import java.util.List;

import ca.ubc.ece.salt.pangor.analysis.CommitAnalysis;
import ca.ubc.ece.salt.pangor.analysis.DataSet;
import ca.ubc.ece.salt.pangor.analysis.factories.ICommitAnalysisFactory;
import ca.ubc.ece.salt.pangor.analysis.factories.IDomainAnalysisFactory;
import ca.ubc.ece.salt.pangor.analysis.flow.FlowDomainAnalysisFactory;
import ca.ubc.ece.salt.pangor.cfg.ICFGVisitorFactory;

public class TypeCommitAnalysisFactory implements ICommitAnalysisFactory {

	private DataSet dataSet;

	public TypeCommitAnalysisFactory(DataSet dataSet) {
		this.dataSet = dataSet;
	}

	@Override
	public CommitAnalysis newInstance() {
		List<ICFGVisitorFactory> cfgVisitorFactories = new LinkedList<ICFGVisitorFactory>();
		cfgVisitorFactories.add(new TypeCFGVisitorFactory());
		List<IDomainAnalysisFactory> domainFactories = new LinkedList<IDomainAnalysisFactory>();
		domainFactories.add(new FlowDomainAnalysisFactory(cfgVisitorFactories));
		return new CommitAnalysis(dataSet, domainFactories);
	}

}