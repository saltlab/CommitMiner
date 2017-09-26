package commitminer.learn.js.factories;

import commitminer.analysis.SourceCodeFileAnalysis;
import commitminer.analysis.factories.ISourceCodeFileAnalysisFactory;
import commitminer.learn.js.analysis.ChangeComplexitySCFA;

public class ChangeComplexityFileAnalysisFactory implements ISourceCodeFileAnalysisFactory {
	
	/** True if this is the old version of the file. **/
	private boolean isSrc;
	
	public ChangeComplexityFileAnalysisFactory(boolean isSrc) {
		this.isSrc = isSrc;
	}

	@Override
	public SourceCodeFileAnalysis newInstance() {
		return new ChangeComplexitySCFA(isSrc);
	}

}
