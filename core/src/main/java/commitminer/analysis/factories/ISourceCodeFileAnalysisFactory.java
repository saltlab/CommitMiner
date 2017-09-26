package commitminer.analysis.factories;

import commitminer.analysis.SourceCodeFileAnalysis;

/**
 * Builds new instances of a source code file analysis.
 */
public interface ISourceCodeFileAnalysisFactory {
	SourceCodeFileAnalysis newInstance();
}
