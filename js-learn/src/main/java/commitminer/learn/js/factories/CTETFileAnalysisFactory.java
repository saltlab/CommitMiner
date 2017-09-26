package commitminer.learn.js.factories;

import java.util.LinkedList;
import java.util.List;

import commitminer.analysis.SourceCodeFileAnalysis;
import commitminer.analysis.factories.ISourceCodeFileAnalysisFactory;
import commitminer.js.analysis.FunctionAnalysis;
import commitminer.learn.js.ctet.CTETFunctionAnalysis;
import commitminer.learn.js.ctet.CTETScriptAnalysis;

public class CTETFileAnalysisFactory implements ISourceCodeFileAnalysisFactory {

	/** True if this is the new version of the file. **/
	private boolean isDst;
	
	public CTETFileAnalysisFactory(boolean isSrc) {
		this.isDst = isSrc;
	}

	@Override
	public SourceCodeFileAnalysis newInstance() {

		List<FunctionAnalysis> functionAnalyses = new LinkedList<FunctionAnalysis>();

		functionAnalyses.add(new CTETFunctionAnalysis(isDst));

		return new CTETScriptAnalysis();

	}

}
