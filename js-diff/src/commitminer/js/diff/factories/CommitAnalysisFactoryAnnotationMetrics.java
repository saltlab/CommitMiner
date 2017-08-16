package commitminer.js.diff.factories;

import java.util.LinkedList;
import java.util.List;

import commitminer.analysis.CommitAnalysis;
import commitminer.analysis.DataSet;
import commitminer.analysis.factories.ICommitAnalysisFactory;
import commitminer.analysis.factories.IDomainAnalysisFactory;
import commitminer.analysis.flow.FlowDomainAnalysisFactory;
import commitminer.cfg.ICFGVisitorFactory;
import commitminer.diff.line.LineDomainAnalysisFactory;
import commitminer.js.diff.ast.AstDomainAnalysisFactory;
import commitminer.js.diff.controlcall.ControlCallCFGVisitorFactory;
import commitminer.js.diff.controlcondition.ControlConditionCFGVisitorFactory;
import commitminer.js.diff.controldependency.ControlDependencyCFGVisitorFactory;
import commitminer.js.diff.datadependency.DataDependencyCFGVisitorFactory;
import commitminer.js.diff.defenvironment.DefEnvCFGVisitorFactory;
import commitminer.js.diff.defvalue.DefValueCFGVisitorFactory;
import commitminer.js.diff.environment.EnvCFGVisitorFactory;
import commitminer.js.diff.value.ValueCFGVisitorFactory;

/**
 * Use for creating a diff commit analysis.
 */
public class CommitAnalysisFactoryAnnotationMetrics implements ICommitAnalysisFactory {

	public CommitAnalysisFactoryAnnotationMetrics() { }

	@Override
	public CommitAnalysis newInstance() {

		List<ICFGVisitorFactory> cfgVisitorFactories = new LinkedList<ICFGVisitorFactory>();
		
		// MultiDiff
		cfgVisitorFactories.add(new ControlCallCFGVisitorFactory());
		cfgVisitorFactories.add(new ControlConditionCFGVisitorFactory());
		cfgVisitorFactories.add(new EnvCFGVisitorFactory());
		cfgVisitorFactories.add(new ValueCFGVisitorFactory());
		
		// DepDiff
		cfgVisitorFactories.add(new ControlDependencyCFGVisitorFactory());
		cfgVisitorFactories.add(new DataDependencyCFGVisitorFactory());

		List<IDomainAnalysisFactory> domainFactories = new LinkedList<IDomainAnalysisFactory>();
		
		domainFactories.add(new FlowDomainAnalysisFactory(cfgVisitorFactories));
		domainFactories.add(new LineDomainAnalysisFactory()); // UnixDiff

		return new CommitAnalysis(domainFactories);

	}

}