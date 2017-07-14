package commitminer.js.diff;

import java.util.LinkedList;
import java.util.List;

import commitminer.analysis.CommitAnalysis;
import commitminer.analysis.DataSet;
import commitminer.analysis.factories.ICommitAnalysisFactory;
import commitminer.analysis.factories.IDomainAnalysisFactory;
import commitminer.analysis.flow.FlowDomainAnalysisFactory;
import commitminer.cfg.ICFGVisitorFactory;
import commitminer.js.diff.controldependency.ControlDependencyCFGVisitorFactory;
import commitminer.js.diff.defenvironment.DefEnvCFGVisitorFactory;
import commitminer.js.diff.defvalue.DefValueCFGVisitorFactory;

/**
 * Use for creating a diff commit analysis.
 */
public class DependencyCommitAnalysisFactory implements ICommitAnalysisFactory {

	private DataSet dataSet;

	public DependencyCommitAnalysisFactory(DataSet dataSet) {
		this.dataSet = dataSet;
	}

	@Override
	public CommitAnalysis newInstance() {
		List<ICFGVisitorFactory> cfgVisitorFactories = new LinkedList<ICFGVisitorFactory>();
		cfgVisitorFactories.add(new ControlDependencyCFGVisitorFactory());
		cfgVisitorFactories.add(new DefValueCFGVisitorFactory());
		cfgVisitorFactories.add(new DefEnvCFGVisitorFactory());

		List<IDomainAnalysisFactory> domainFactories = new LinkedList<IDomainAnalysisFactory>();
		domainFactories.add(new FlowDomainAnalysisFactory(cfgVisitorFactories));
		return new CommitAnalysis(dataSet, domainFactories);
	}

}