package ca.ubc.ece.salt.pangor.analysis.factories;

import ca.ubc.ece.salt.pangor.analysis.SourceCodeFileAnalysis;

/**
 * Builds new instances of a source code file analysis.
 */
public interface ISourceCodeFileAnalysisFactory {
	SourceCodeFileAnalysis newInstance();
}
