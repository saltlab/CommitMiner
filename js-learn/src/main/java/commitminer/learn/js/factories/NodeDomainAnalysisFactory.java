package commitminer.learn.js.factories;

import commitminer.analysis.DomainAnalysis;
import commitminer.analysis.factories.IDomainAnalysisFactory;
import commitminer.analysis.factories.ISourceCodeFileAnalysisFactory;
import commitminer.learn.js.nodes.NodeDomainAnalysis;

public class NodeDomainAnalysisFactory implements IDomainAnalysisFactory {

	@Override
	public DomainAnalysis newInstance() {

		ISourceCodeFileAnalysisFactory srcFactory = new NodeFileAnalysisFactory();
		ISourceCodeFileAnalysisFactory dstFactory = new NodeFileAnalysisFactory();

		return new NodeDomainAnalysis(srcFactory, dstFactory);

	}

}
