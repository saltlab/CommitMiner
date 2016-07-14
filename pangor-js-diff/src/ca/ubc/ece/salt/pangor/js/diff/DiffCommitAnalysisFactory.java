package ca.ubc.ece.salt.pangor.js.diff;

import java.util.LinkedList;
import java.util.List;

import ca.ubc.ece.salt.pangor.analysis.CommitAnalysis;
import ca.ubc.ece.salt.pangor.analysis.DataSet;
import ca.ubc.ece.salt.pangor.analysis.factories.ICommitAnalysisFactory;
import ca.ubc.ece.salt.pangor.analysis.factories.IDomainAnalysisFactory;
import ca.ubc.ece.salt.pangor.analysis.flow.FlowDomainAnalysisFactory;
import ca.ubc.ece.salt.pangor.cfg.ICFGVisitorFactory;
import ca.ubc.ece.salt.pangor.js.classify.use.UseDomainAnalysisFactory;
import ca.ubc.ece.salt.pangor.js.diff.control.ControlCFGVisitorFactory;
import ca.ubc.ece.salt.pangor.js.diff.environment.EnvCFGVisitorFactory;
import ca.ubc.ece.salt.pangor.js.diff.value.ValueCFGVisitorFactory;

/**
 * Use for creating a diff commit analysis.
 */
public class DiffCommitAnalysisFactory implements ICommitAnalysisFactory {

	private DataSet dataSet;

	public DiffCommitAnalysisFactory(DataSet dataSet) {
		this.dataSet = dataSet;
	}

	@Override
	public CommitAnalysis newInstance() {
		List<ICFGVisitorFactory> cfgVisitorFactories = new LinkedList<ICFGVisitorFactory>();
		cfgVisitorFactories.add(new ControlCFGVisitorFactory());
		cfgVisitorFactories.add(new EnvCFGVisitorFactory());
		cfgVisitorFactories.add(new ValueCFGVisitorFactory());

		// TODO: While these (use and flow) are sufficient for extracting
		//		 control flow, environment and value diffs, we also need to
		//		 add an analysis for extracting the modified lines from
		//		 gumtree and line-level diff.
		List<IDomainAnalysisFactory> domainFactories = new LinkedList<IDomainAnalysisFactory>();
		domainFactories.add(new FlowDomainAnalysisFactory(cfgVisitorFactories));
		domainFactories.add(new UseDomainAnalysisFactory());
		return new CommitAnalysis(dataSet, domainFactories);
	}

}