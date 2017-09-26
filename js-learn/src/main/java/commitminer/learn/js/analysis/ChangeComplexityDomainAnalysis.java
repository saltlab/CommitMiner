package commitminer.learn.js.analysis;

import java.util.Map;

import org.deri.iris.api.basics.IPredicate;
import org.deri.iris.api.basics.ITuple;
import org.deri.iris.factory.Factory;
import org.deri.iris.storage.IRelation;
import org.deri.iris.storage.IRelationFactory;
import org.deri.iris.storage.simple.SimpleRelationFactory;

import commitminer.analysis.Commit;
import commitminer.analysis.DomainAnalysis;
import commitminer.analysis.SourceCodeFileChange;
import commitminer.js.cfg.JavaScriptCFGFactory;
import commitminer.learn.js.factories.ChangeComplexityFileAnalysisFactory;

public class ChangeComplexityDomainAnalysis extends DomainAnalysis {

	public ChangeComplexityDomainAnalysis(ChangeComplexityFileAnalysisFactory srcAnalysis,
									ChangeComplexityFileAnalysisFactory dstAnalysis) {
		super(srcAnalysis, dstAnalysis, new JavaScriptCFGFactory(), false, false);
	}

	@Override
	public void analyze(Commit commit, Map<IPredicate, IRelation> facts) throws Exception {

		ChangeComplexitySCFA srcComplexity = (ChangeComplexitySCFA)this.srcAnalysisFactory.newInstance();
		ChangeComplexitySCFA dstComplexity = (ChangeComplexitySCFA)this.srcAnalysisFactory.newInstance();

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

}
