package ca.ubc.ece.salt.pangor.js.learn.analysis;

import java.util.Map;

import org.deri.iris.api.basics.IPredicate;
import org.deri.iris.api.basics.ITuple;
import org.deri.iris.factory.Factory;
import org.deri.iris.storage.IRelation;
import org.deri.iris.storage.IRelationFactory;
import org.deri.iris.storage.simple.SimpleRelationFactory;

import ca.ubc.ece.salt.pangor.analysis.Commit;
import ca.ubc.ece.salt.pangor.analysis.DomainAnalysis;
import ca.ubc.ece.salt.pangor.analysis.SourceCodeFileChange;
import ca.ubc.ece.salt.pangor.js.cfg.JavaScriptCFGFactory;

public class ChangeComplexityDomainAnalysis extends DomainAnalysis {

	private ChangeComplexitySCFA srcComplexity;
	private ChangeComplexitySCFA dstComplexity;

	public ChangeComplexityDomainAnalysis(ChangeComplexitySCFA srcAnalysis,
									ChangeComplexitySCFA dstAnalysis) {
		super(srcAnalysis, dstAnalysis, new JavaScriptCFGFactory(), false);
		this.srcComplexity = srcAnalysis;
		this.dstComplexity = dstAnalysis;
	}

	@Override
	public void analyze(Commit commit, Map<IPredicate, IRelation> facts) throws Exception {

		/* Measure the number of modified statements in each source code file.
		 * If there are more than `maxChangeComplexity` modified statements,
		 * abort the analysis. */

		/* Iterate through the files in the commit and run the
		 * SourceCodeFileAnalysis on each of them. */
		int complexity = 0;
		for(SourceCodeFileChange sourceCodeFileChange : commit.sourceCodeFileChanges) {

			/* Compute the change complexity for this file.
			 * NOTE: If an exception occurs while analyzing the file, no results
			 * 		 will be returned and the complexity will not be correct. */
			this.analyzeFile(sourceCodeFileChange, facts);

			/* Update the total change complexity. */
			if(srcComplexity.getChangeComplexity() != null) {
				complexity += srcComplexity.getChangeComplexity().removedStatements;
			}
			if(dstComplexity.getChangeComplexity() != null) {
				complexity += dstComplexity.getChangeComplexity().insertedStatements;
				complexity += dstComplexity.getChangeComplexity().updatedStatements;
			}

			/* Reset the change complexity for the next run. */
			srcComplexity.resetComplexity();
			dstComplexity.resetComplexity();

		}

		/* Create a fact with the change complexity information. */
		this.addModifiedStatementCountFact(facts, complexity);

	}

	/**
	 * Adds a change complexity fact to the fact database.
	 * @param keyword The keyword change to record.
	 */
	private void addModifiedStatementCountFact(Map<IPredicate, IRelation> facts, int complexity) {

		/* Get the relation for this predicate from the fact base. */
		IPredicate predicate = Factory.BASIC.createPredicate("ModifiedStatementCount", 3);
		IRelation relation = facts.get(predicate);
		if(relation == null) {

			/* The predicate does not yet exist in the fact base. Create a
			 * relation for the predicate and add it to the fact base. */
			IRelationFactory relationFactory = new SimpleRelationFactory();
			relation = relationFactory.createRelation();
			facts.put(predicate, relation);

		}

		/* Add the new tuple to the relation. */
		ITuple tuple = Factory.BASIC.createTuple(
				Factory.TERM.createString("ClassNA"),
				Factory.TERM.createString("MethodNA"),
				Factory.TERM.createString(String.valueOf(complexity)));
		relation.add(tuple);

		facts.put(predicate, relation);
	}

	/**
	 * Builds a new {@code ComplexityDomainAnalysis}.
	 * @return an analysis for extracting the statement change complexity for
	 * 		   a commit.
	 */
	public static ChangeComplexityDomainAnalysis createComplexityAnalysis() {

		ChangeComplexitySCFA srcSCFA = new ChangeComplexitySCFA(false);
		ChangeComplexitySCFA dstSCFA = new ChangeComplexitySCFA(true);

		ChangeComplexityDomainAnalysis analysis = new ChangeComplexityDomainAnalysis(srcSCFA, dstSCFA);

		return analysis;

	}

}
