package commitminer.js.metrics;

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
import commitminer.js.diff.defenvironment.DefEnvCFGVisitorFactory;
import commitminer.js.diff.defvalue.DefValueCFGVisitorFactory;
import commitminer.js.diff.environment.EnvCFGVisitorFactory;
import commitminer.js.diff.value.ValueCFGVisitorFactory;

/**
 * Use for creating a diff commit analysis.
 */
public class CommitAnalysisFactoryUnixDiffMetrics implements ICommitAnalysisFactory {

	private DataSet dataSet;

	public CommitAnalysisFactoryUnixDiffMetrics(DataSet dataSet) {
		this.dataSet = dataSet;
	}

	@Override
	public CommitAnalysis newInstance() {
		List<IDomainAnalysisFactory> domainFactories = new LinkedList<IDomainAnalysisFactory>();
		domainFactories.add(new LineDomainAnalysisFactory());
		return new CommitAnalysis(dataSet, domainFactories);
	}

}