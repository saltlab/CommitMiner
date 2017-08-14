package commitminer.js.metrics;

import java.util.LinkedList;
import java.util.List;

import commitminer.analysis.CommitAnalysis;
import commitminer.analysis.DataSet;
import commitminer.analysis.factories.ICommitAnalysisFactory;
import commitminer.analysis.factories.IDomainAnalysisFactory;
import commitminer.analysis.flow.FlowDomainAnalysisFactory;
import commitminer.cfg.ICFGVisitorFactory;
import commitminer.js.diff.controldependency.ControlDependencyCFGVisitorFactory;
import commitminer.js.diff.datadependency.DataDependencyCFGVisitorFactory;
import commitminer.js.diff.defenvironment.DefEnvCFGVisitorFactory;
import commitminer.js.diff.defvalue.DefValueCFGVisitorFactory;

/**
 * Use for creating a diff commit analysis.
 */
public class CommitAnalysisFactoryDepDiffMetrics implements ICommitAnalysisFactory {

	private DataSet dataSet;

	public CommitAnalysisFactoryDepDiffMetrics(DataSet dataSet) {
		this.dataSet = dataSet;
	}

	@Override
	public CommitAnalysis newInstance() {
		List<ICFGVisitorFactory> cfgVisitorFactories = new LinkedList<ICFGVisitorFactory>();
		cfgVisitorFactories.add(new ControlDependencyCFGVisitorFactory());
		cfgVisitorFactories.add(new DataDependencyCFGVisitorFactory());

		List<IDomainAnalysisFactory> domainFactories = new LinkedList<IDomainAnalysisFactory>();
		domainFactories.add(new FlowDomainAnalysisFactory(cfgVisitorFactories));
		return new CommitAnalysis(dataSet, domainFactories);
	}

}