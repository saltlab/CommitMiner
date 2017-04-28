package commitminer.analysis.factories;

import commitminer.analysis.CommitAnalysis;

/**
 * Builds new instances of a commit analysis.
 */
public interface ICommitAnalysisFactory {
	CommitAnalysis newInstance();
}
