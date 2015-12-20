package ca.ubc.ece.salt.pangor.analysis.simple;

import java.util.List;
import java.util.Map;

import org.deri.iris.api.basics.IPredicate;
import org.deri.iris.api.basics.ITuple;
import org.deri.iris.api.factory.IBasicFactory;
import org.deri.iris.api.factory.ITermFactory;
import org.deri.iris.factory.Factory;
import org.deri.iris.storage.IRelation;
import org.deri.iris.storage.IRelationFactory;
import org.deri.iris.storage.simple.SimpleRelationFactory;

import ca.ubc.ece.salt.gumtree.ast.ClassifiedASTNode;
import ca.ubc.ece.salt.pangor.analysis.Commit;
import ca.ubc.ece.salt.pangor.analysis.SourceCodeFileAnalysis;
import ca.ubc.ece.salt.pangor.analysis.SourceCodeFileChange;
import ca.ubc.ece.salt.pangor.cfg.CFG;

/**
 * Performs a simple analysis of a source code file. The analysis registers
 * a pattern for each file, which is the type name of the root node.
 */
public class SimpleSrcFileAnalysis extends SourceCodeFileAnalysis {

	public SimpleSrcFileAnalysis() { }

	@Override
	public void analyze(Commit commit, SourceCodeFileChange sourceCodeFileChange, Map<IPredicate, IRelation> facts, ClassifiedASTNode root, List<CFG> cfgs) throws Exception {

		/* The factories we need to create the predicates. */
		IBasicFactory basicFactory = Factory.BASIC;
		ITermFactory termFactory = Factory.TERM;

		/* Create the predicate for this analysis. */
		IPredicate predicate = basicFactory.createPredicate("SourceRoot", 2);

		/* Get the relation for this predicate from the fact base. */
		IRelation relation = facts.get(predicate);
		if(relation == null) {

			/* The predicate does not yet exist in the fact base. Create a
			 * relation for the predicate and add it to the fact base. */
			IRelationFactory relationFactory = new SimpleRelationFactory();
			relation = relationFactory.createRelation();
			facts.put(predicate, relation);

		}

		/* Add the new tuple to the relation. */
		ITuple tuple = basicFactory.createTuple(termFactory.createString(sourceCodeFileChange.getFileName()), termFactory.createString(root.getASTNodeType()));
		relation.add(tuple);

	}

}
