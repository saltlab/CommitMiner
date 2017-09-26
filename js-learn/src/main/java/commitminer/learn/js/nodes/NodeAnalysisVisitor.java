package commitminer.learn.js.nodes;


import java.util.Map;

import org.deri.iris.api.basics.IPredicate;
import org.deri.iris.api.basics.ITuple;
import org.deri.iris.factory.Factory;
import org.deri.iris.storage.IRelation;
import org.deri.iris.storage.IRelationFactory;
import org.deri.iris.storage.simple.SimpleRelationFactory;
import org.mozilla.javascript.ast.AstNode;
import org.mozilla.javascript.ast.NodeVisitor;
import org.mozilla.javascript.ast.ScriptNode;

import ca.ubc.ece.salt.gumtree.ast.ClassifiedASTNode.ChangeType;
import ca.ubc.ece.salt.pangor.analysis.SourceCodeFileChange;
import ca.ubc.ece.salt.pangor.api.KeywordDefinition.KeywordType;
import ca.ubc.ece.salt.pangor.api.KeywordUse.KeywordContext;
import ca.ubc.ece.salt.pangor.js.analysis.utilities.AnalysisUtilities;
import ca.ubc.ece.salt.pangor.pointsto.PointsToPrediction;

/**
 * Inspects scripts and functions for API keywords.
 */
public class NodeAnalysisVisitor implements NodeVisitor {

	/** Store unique IDs for KeywordChange facts. **/
	private static Integer uniqueID = 0;

	/** True if this is a destination file analysis. **/
	private boolean dst;

	/** The fact database. **/
	private Map<IPredicate, IRelation> facts;

	/**
	 * Visits the script or function and returns a feature vector for it.
	 * @param facts
	 * @param scfc
	 * @param function the script or function to visit.
	 * @param packageModel
	 * @return the feature vector for the function.
	 */
	public static void getLearningFacts(Map<IPredicate, IRelation> facts,
			SourceCodeFileChange scfc, ScriptNode function,
			PointsToPrediction packageModel, boolean dst) {

		/* Create the feature vector by visiting the function. */
		NodeAnalysisVisitor visitor = new NodeAnalysisVisitor(facts, scfc,
				AnalysisUtilities.getFunctionName(function), function,  dst);
		function.visit(visitor);
	}


	private NodeAnalysisVisitor(Map<IPredicate, IRelation> facts,
			SourceCodeFileChange scfc, String functionName, ScriptNode root,
			boolean dst) {
		this.facts = facts;
		this.dst = dst;
	}

	@Override
	public boolean visit(AstNode node) {

		/* Always inspect the highest level change since we propagate MOVE,
		 * INSERT and DELETE change types downward and UPDATE change types
		 * upward. */
		if(node.getChangeType() != ChangeType.UNCHANGED
				&& node.getChangeType() != ChangeType.UNKNOWN
				&& node.getChangeType() != node.getParent().getChangeType()) {

			/* Only register REMOVED change types for source file analysis. */
			if((node.getChangeType() == ChangeType.REMOVED && !this.dst) || this.dst) {
				addNodeFact(node);
			}

		}

		return true;

	}

	/**
	 * Adds a node fact to the fact database.
	 * @param node The node that has changed.
	 */
	private void addNodeFact(AstNode node) {

		/* Get the relation for this predicate from the fact base. */
		IPredicate predicate = Factory.BASIC.createPredicate("KeywordChange", 8);
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
				Factory.TERM.createString(KeywordType.UNKNOWN.toString()),
				Factory.TERM.createString(KeywordContext.UNKNOWN.toString()),
				Factory.TERM.createString("unknown"),
				Factory.TERM.createString(node.getChangeType().toString()),
				Factory.TERM.createString(node.getClass().getSimpleName()),
				Factory.TERM.createString(getUniqueID().toString()));
		relation.add(tuple);

		this.facts.put(predicate, relation);
	}

	/**
	 * @return A unique ID to assign to a KeywordChange fact.
	 */
	private static synchronized Integer getUniqueID() {
		Integer id = uniqueID;
		uniqueID = uniqueID + 1;
		return id;
	}

}