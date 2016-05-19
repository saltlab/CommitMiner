package ca.ubc.ece.salt.pangor.analysis.factories;

import ca.ubc.ece.salt.pangor.analysis.DomainAnalysis;

/**
 * Builds new instances of a domain analysis.
 */
public interface IDomainAnalysisFactory {
	DomainAnalysis newInstance();
}
