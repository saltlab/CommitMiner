package commitminer.learn.js.factories;

import java.util.LinkedList;
import java.util.List;

import commitminer.analysis.SourceCodeFileAnalysis;
import commitminer.analysis.factories.ISourceCodeFileAnalysisFactory;
import commitminer.js.analysis.FunctionAnalysis;
import commitminer.js.analysis.ScriptAnalysis;
import commitminer.learn.js.analysis.LearningFunctionAnalysis;

public class LearningFileAnalysisFactory implements ISourceCodeFileAnalysisFactory {

	/** True if this is the old version of the file. **/
	private boolean isSrc;
	
	public LearningFileAnalysisFactory(boolean isSrc) {
		this.isSrc = isSrc;
	}

	@Override
	public SourceCodeFileAnalysis newInstance() {
		List<FunctionAnalysis> functionAnalyses = new LinkedList<FunctionAnalysis>();
		functionAnalyses.add(new LearningFunctionAnalysis(isSrc));
		return new ScriptAnalysis(functionAnalyses);
	}

}
