package ca.ubc.ece.salt.pangor.js.diff.line;

import ca.ubc.ece.salt.pangor.analysis.DomainAnalysis;
import ca.ubc.ece.salt.pangor.analysis.factories.IDomainAnalysisFactory;

public class LineDomainAnalysisFactory implements IDomainAnalysisFactory {

	@Override
	public DomainAnalysis newInstance() {
		return new LineDomainAnalysis();
	}

}
