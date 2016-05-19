package ca.ubc.ece.salt.pangor.analysis.factories;

import ca.ubc.ece.salt.pangor.analysis.CommitAnalysis;

/**
 * Builds new instances of a commit analysis.
 */
public interface ICommitAnalysisFactory {
	CommitAnalysis newInstance();
}
