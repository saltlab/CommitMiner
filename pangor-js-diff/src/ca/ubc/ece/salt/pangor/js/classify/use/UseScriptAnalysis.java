package ca.ubc.ece.salt.pangor.js.classify.use;

import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.tuple.Pair;
import org.deri.iris.api.basics.IPredicate;
import org.deri.iris.api.basics.ITuple;
import org.deri.iris.factory.Factory;
import org.deri.iris.storage.IRelation;
import org.deri.iris.storage.IRelationFactory;
import org.deri.iris.storage.simple.SimpleRelationFactory;
import org.mozilla.javascript.ast.AstNode;
import org.mozilla.javascript.ast.AstRoot;

import ca.ubc.ece.salt.gumtree.ast.ClassifiedASTNode;
import ca.ubc.ece.salt.gumtree.ast.ClassifiedASTNode.ChangeType;
import ca.ubc.ece.salt.pangor.analysis.SourceCodeFileAnalysis;
import ca.ubc.ece.salt.pangor.analysis.SourceCodeFileChange;
import ca.ubc.ece.salt.pangor.cfg.CFG;

/**
 * An analysis of a JavaScript file for extracting identifier use facts.
 *
 * NOTES:
 * 	1. This class only works with the Rhino AST.
 * 	2. This class is thread-safe.
 */
public class UseScriptAnalysis extends SourceCodeFileAnalysis {

	@Override
	public void analyze(SourceCodeFileChange sourceCodeFileChange,
			Map<IPredicate, IRelation> facts, ClassifiedASTNode root,
			List<CFG> cfgs) throws Exception {

		/* Check we are working with the correct AST type. */
		if(!(root instanceof AstRoot)) throw new IllegalArgumentException("The AST must be parsed from Eclipse JDT.");
		AstRoot script = (AstRoot) root;

		/* Visit statements and extract use facts. */
		Map<AstNode, List<Pair<ChangeType,String>>> uses = UseTreeVisitor.getUses(script);

		/* Register the facts. */
		for(Map.Entry<AstNode, List<Pair<ChangeType,String>>> entries : uses.entrySet()) {
			for(Pair<ChangeType,String> identifier : entries.getValue()) {
				registerUseFact(entries.getKey(), identifier.getLeft(),
								identifier.getRight(), facts,
								sourceCodeFileChange);
			}
		}

	}

	/**
	 * Registers a Use fact.
	 * @param statement The statement in which the use occurred.
	 * @param identifier The identifier that was used.
	 */
	private static void registerUseFact(AstNode statement,
									  ChangeType changeType, String identifier,
									  Map<IPredicate, IRelation> facts,
									  SourceCodeFileChange sourceCodeFileChange) {

		/* Get the relation for this predicate from the fact base. */
		IPredicate predicate = Factory.BASIC.createPredicate("Use", 6);
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
				Factory.TERM.createString(String.valueOf(statement.getLineno())),	// Line #
				Factory.CONCRETE.createInt(statement.getID()),						// Statement ID
				Factory.TERM.createString(changeType.toString()),					// Change Type
				Factory.TERM.createString(identifier));								// Identifier
		relation.add(tuple);

		facts.put(predicate, relation);

	}

}
