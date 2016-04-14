package ca.ubc.ece.salt.pangor.js.classify.sth;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.deri.iris.api.basics.IPredicate;
import org.deri.iris.api.basics.ITuple;
import org.deri.iris.factory.Factory;
import org.deri.iris.storage.IRelation;
import org.deri.iris.storage.IRelationFactory;
import org.deri.iris.storage.simple.SimpleRelationFactory;

import ca.ubc.ece.salt.pangor.analysis.Commit;
import ca.ubc.ece.salt.pangor.analysis.DomainAnalysis;
import ca.ubc.ece.salt.pangor.analysis.SourceCodeFileAnalysis;
import ca.ubc.ece.salt.pangor.js.analysis.FunctionAnalysis;
import ca.ubc.ece.salt.pangor.js.analysis.ScriptAnalysis;
import ca.ubc.ece.salt.pangor.js.cfg.JavaScriptCFGFactory;

public class STHDomainAnalysis extends DomainAnalysis {

	/**
	 * @param srcAnalysis the source analysis the anlaysis runs to extract domain facts
	 * @param dstAnalysis the destination analysis the anlaysis runs to extract domain facts
	 */
	private STHDomainAnalysis(SourceCodeFileAnalysis srcAnalysis,
							 SourceCodeFileAnalysis dstAnalysis) {
		super(srcAnalysis, dstAnalysis, new JavaScriptCFGFactory(), true);
	}

	@Override
	protected boolean preAnalysis(Commit commit, Map<IPredicate, IRelation> facts) throws Exception {

		/* Create and add the relation for this predicate to the fact base. */
		IPredicate predicate = Factory.BASIC.createPredicate("Stronger", 2);
		IRelationFactory relationFactory = new SimpleRelationFactory();
		IRelation relation = relationFactory.createRelation();
		facts.put(predicate, relation);

		/* Define all stonger than relations. We will also need to define a
		 * transitive rule though the data set constructor */

		strongerThan(relation, "NO_VALUE", "FALSEY");
		strongerThan(relation, "EMPTY", "FALSEY");

		strongerThan(relation, "NULL", "NO_VALUE");
		strongerThan(relation, "UNDEFINED", "NO_VALUE");
		strongerThan(relation, "NAN", "NO_VALUE");
		strongerThan(relation, "BLANK", "NO_VALUE");
		strongerThan(relation, "ZERO", "NO_VALUE");
		strongerThan(relation, "EMPTY_ARRAY", "NO_VALUE");
		strongerThan(relation, "FUNCTION", "NO_VALUE");

		strongerThan(relation, "NULL", "EMPTY");
		strongerThan(relation, "UNDEFINED", "EMPTY");
		strongerThan(relation, "NAN", "EMPTY");
		strongerThan(relation, "BLANK", "EMPTY");
		strongerThan(relation, "ZERO", "EMPTY");
		strongerThan(relation, "EMPTY_ARRAY", "EMPTY");
		strongerThan(relation, "FUNCTION", "EMPTY");

		ITuple tuple = Factory.BASIC.createTuple(
				Factory.TERM.createString("FALSEY"),
				Factory.TERM.createString("NO_VALUE"));
		relation.add(tuple);

		facts.put(predicate, relation);
		return true;
	}

	/**
	 * Creates a new fact "special type A is stronger than special type B"
	 * @param relation The relation to add the fact to.
	 * @param stronger The stronger special type.
	 * @param weaker The weaker special type.
	 */
	private void strongerThan(IRelation relation, String stronger, String weaker) {
		ITuple tuple = Factory.BASIC.createTuple(
				Factory.TERM.createString(stronger),
				Factory.TERM.createString(weaker));
		relation.add(tuple);
	}


	/**
	 * Builds a new {@code LearningAnalysis}
	 * @param maxChangeComplexity The maximum number of statements that can change in a commit.
	 * @return an analysis for extracting facts in the learning domain.
	 */
	public static STHDomainAnalysis createLearningAnalysis() {

		List<FunctionAnalysis> srcFunctionAnalyses = new LinkedList<FunctionAnalysis>();
		List<FunctionAnalysis> dstFunctionAnalyses = new LinkedList<FunctionAnalysis>();

		srcFunctionAnalyses.add(new STHFlowAnalysis());
		dstFunctionAnalyses.add(new STHFlowAnalysis());

		SourceCodeFileAnalysis srcSCFA = new ScriptAnalysis(srcFunctionAnalyses);
		SourceCodeFileAnalysis dstSCFA = new ScriptAnalysis(dstFunctionAnalyses);

		STHDomainAnalysis analysis = new STHDomainAnalysis(srcSCFA, dstSCFA);

		return analysis;
	}

}
