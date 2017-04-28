package commitminer.js.diff.typeerror;

import java.util.LinkedList;
import java.util.List;

import commitminer.analysis.CommitAnalysis;
import commitminer.analysis.DataSet;
import commitminer.analysis.factories.ICommitAnalysisFactory;
import commitminer.analysis.factories.IDomainAnalysisFactory;
import commitminer.analysis.flow.FlowDomainAnalysisFactory;
import commitminer.cfg.ICFGVisitorFactory;

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