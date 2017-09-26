package commitminer.analysis.factories;

import commitminer.analysis.DomainAnalysis;

/**
 * Builds new instances of a domain analysis.
 */
public interface IDomainAnalysisFactory {
	DomainAnalysis newInstance();
}
