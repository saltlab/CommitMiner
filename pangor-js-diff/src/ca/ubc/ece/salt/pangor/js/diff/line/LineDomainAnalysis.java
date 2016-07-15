package ca.ubc.ece.salt.pangor.js.diff.line;

import java.util.Map;

import org.deri.iris.api.basics.IPredicate;
import org.deri.iris.storage.IRelation;

import ca.ubc.ece.salt.pangor.analysis.DomainAnalysis;
import ca.ubc.ece.salt.pangor.analysis.SourceCodeFileChange;

public class LineDomainAnalysis extends DomainAnalysis {

	public LineDomainAnalysis() {
		super(null, null, null, false);
	}

	@Override
	protected void analyzeFile(SourceCodeFileChange sourceCodeFileChange,
							   Map<IPredicate, IRelation> facts) throws Exception {
		// TODO
	}

}
