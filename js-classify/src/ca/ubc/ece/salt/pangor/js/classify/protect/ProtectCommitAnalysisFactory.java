package ca.ubc.ece.salt.pangor.js.classify.protect;

import java.util.LinkedList;
import java.util.List;

import ca.ubc.ece.salt.pangor.analysis.CommitAnalysis;
import ca.ubc.ece.salt.pangor.analysis.DataSet;
import ca.ubc.ece.salt.pangor.analysis.factories.ICommitAnalysisFactory;
import ca.ubc.ece.salt.pangor.analysis.factories.IDomainAnalysisFactory;
import ca.ubc.ece.salt.pangor.analysis.flow.FlowDomainAnalysisFactory;
import ca.ubc.ece.salt.pangor.cfg.ICFGVisitorFactory;
import ca.ubc.ece.salt.pangor.js.classify.use.UseDomainAnalysisFactory;

public class ProtectCommitAnalysisFactory implements ICommitAnalysisFactory {

	private DataSet dataSet;

	public ProtectCommitAnalysisFactory(DataSet dataSet) {
		this.dataSet = dataSet;
	}

	@Override
	public CommitAnalysis newInstance() {
		List<ICFGVisitorFactory> cfgVisitorFactories = new LinkedList<ICFGVisitorFactory>();
		cfgVisitorFactories.add(new ProtectedCFGVisitorFactory());
		List<IDomainAnalysisFactory> domainFactories = new LinkedList<IDomainAnalysisFactory>();
		domainFactories.add(new FlowDomainAnalysisFactory(cfgVisitorFactories));
		domainFactories.add(new UseDomainAnalysisFactory());
		return new CommitAnalysis(dataSet, domainFactories);
	}

}