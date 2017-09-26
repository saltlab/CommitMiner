package commitminer.learn.js.factories;

import commitminer.analysis.SourceCodeFileAnalysis;
import commitminer.analysis.factories.ISourceCodeFileAnalysisFactory;
import commitminer.learn.js.nodes.NodeScriptAnalysis;

public class NodeFileAnalysisFactory implements ISourceCodeFileAnalysisFactory {

	@Override
	public SourceCodeFileAnalysis newInstance() {
		return new NodeScriptAnalysis();
	}

}
