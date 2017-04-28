package commitminer.js.diff.ast;

import java.util.List;
import java.util.Map;

import org.deri.iris.api.basics.IPredicate;
import org.deri.iris.api.basics.ITuple;
import org.deri.iris.factory.Factory;
import org.deri.iris.storage.IRelation;
import org.deri.iris.storage.IRelationFactory;
import org.deri.iris.storage.simple.SimpleRelationFactory;
import org.mozilla.javascript.ast.AstNode;
import org.mozilla.javascript.ast.AstRoot;

import commitminer.analysis.SourceCodeFileAnalysis;
import commitminer.analysis.SourceCodeFileChange;
import commitminer.cfg.CFG;
import ca.ubc.ece.salt.gumtree.ast.ClassifiedASTNode;

/**
 * An analysis of a JavaScript file for extracting AST change facts.
 * TODO: What kind of information do we want to extract? We could simply label
 * 		 which statements/lines have been updated in some manner. We could also
 * 		 do it at a node-level granularity. We could also separate the
 * 		 structural changes based on the type of change.
 *
 * NOTES:
 * 	1. This class only works with the Rhino AST.
 * 	2. This class is thread-safe.
 */
public class AstScriptAnalysis extends SourceCodeFileAnalysis {

	@Override
	public void analyze(SourceCodeFileChange sourceCodeFileChange,
			Map<IPredicate, IRelation> facts, ClassifiedASTNode root,
			List<CFG> cfgs) throws Exception {

		/* Check we are working with the correct AST type. */
		if(!(root instanceof AstRoot)) throw new IllegalArgumentException("The AST must be parsed from Eclipse JDT.");
		AstRoot script = (AstRoot) root;

		/* Visit statements and extract statement change facts. */
		List<AstNode> modifiedStatements = AstTreeVisitor.getModifiedStatements(script);


		/* Register the facts. */
		for(AstNode modifiedStatement : modifiedStatements) {

			/* Get the line numbers from parts of the AST that were changed. */
			String lines = AstLineVisitor.getStatementLines(modifiedStatement);

			registerChangeFact(modifiedStatement, facts,
							   sourceCodeFileChange, lines);

		}

	}

	/**
	 * Registers a line change fact.
	 * @param statement The statement in which was changed.
	 * @param changeType How the statement was modified.
	 */
	private static void registerChangeFact(AstNode statement,
									  Map<IPredicate, IRelation> facts,
									  SourceCodeFileChange sourceCodeFileChange,
									  String lines) {

		/* Get the relation for this predicate from the fact base. */
		IPredicate predicate = Factory.BASIC.createPredicate("AST", 5);
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
				Factory.TERM.createString(statement.getVersion().toString()),		// Version
				Factory.TERM.createString(sourceCodeFileChange.repairedFile), 		// File
				Factory.TERM.createString(lines),									// Line #
				Factory.CONCRETE.createInt(statement.getID()),						// Statement ID
				Factory.TERM.createString(statement.getChangeType().toString()));	// Change Type
		relation.add(tuple);

		facts.put(predicate, relation);

	}

}
