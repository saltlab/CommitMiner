package commitminer.learn.js.statements;


import java.util.Map;

import org.deri.iris.api.basics.IPredicate;
import org.deri.iris.api.basics.ITuple;
import org.deri.iris.factory.Factory;
import org.deri.iris.storage.IRelation;
import org.deri.iris.storage.IRelationFactory;
import org.deri.iris.storage.simple.SimpleRelationFactory;
import org.mozilla.javascript.ast.AstNode;
import org.mozilla.javascript.ast.BreakStatement;
import org.mozilla.javascript.ast.ContinueStatement;
import org.mozilla.javascript.ast.DoLoop;
import org.mozilla.javascript.ast.EmptyStatement;
import org.mozilla.javascript.ast.ExpressionStatement;
import org.mozilla.javascript.ast.ForInLoop;
import org.mozilla.javascript.ast.ForLoop;
import org.mozilla.javascript.ast.FunctionNode;
import org.mozilla.javascript.ast.IfStatement;
import org.mozilla.javascript.ast.LabeledStatement;
import org.mozilla.javascript.ast.NodeVisitor;
import org.mozilla.javascript.ast.ReturnStatement;
import org.mozilla.javascript.ast.ScriptNode;
import org.mozilla.javascript.ast.SwitchStatement;
import org.mozilla.javascript.ast.ThrowStatement;
import org.mozilla.javascript.ast.TryStatement;
import org.mozilla.javascript.ast.VariableDeclaration;
import org.mozilla.javascript.ast.WhileLoop;
import org.mozilla.javascript.ast.WithStatement;

import ca.ubc.ece.salt.gumtree.ast.ClassifiedASTNode.ChangeType;
import commitminer.analysis.SourceCodeFileChange;
import commitminer.api.KeywordDefinition.KeywordType;
import commitminer.api.KeywordUse.KeywordContext;
import commitminer.js.analysis.utilities.AnalysisUtilities;
import commitminer.pointsto.PointsToPrediction;

/**
 * Inspects scripts and functions for API keywords.
 */
public class StatementAnalysisVisitor implements NodeVisitor {

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
		StatementAnalysisVisitor visitor = new StatementAnalysisVisitor(facts, scfc,
				AnalysisUtilities.getFunctionName(function), function,  dst);
		function.visit(visitor);
	}


	private StatementAnalysisVisitor(Map<IPredicate, IRelation> facts,
			SourceCodeFileChange scfc, String functionName, ScriptNode root,
			boolean dst) {
		this.facts = facts;
		this.dst = dst;
	}

	@Override
	public boolean visit(AstNode node) {

		if(node instanceof BreakStatement
				|| node instanceof ContinueStatement
				|| node instanceof EmptyStatement
				|| node instanceof ExpressionStatement
				|| node instanceof IfStatement
				|| node instanceof LabeledStatement
				|| node instanceof ReturnStatement
				|| node instanceof SwitchStatement
				|| node instanceof ThrowStatement
				|| node instanceof TryStatement
				|| node instanceof WithStatement
				|| node instanceof ForLoop
				|| node instanceof ForInLoop
				|| node instanceof WhileLoop
				|| node instanceof DoLoop
				|| node instanceof FunctionNode
				|| (node instanceof VariableDeclaration && node.isStatement())) {

			if(node.getChangeType() != ChangeType.UNCHANGED
				&& node.getChangeType() != ChangeType.UNKNOWN
				&& ((node.getChangeType() == ChangeType.REMOVED && !this.dst) || this.dst)) {

				addStatementFact(node);

			}

		}

		return true;

	}

	/**
	 * Adds a CTET fact to the fact database.
	 * @param node The statement that has changed.
	 */
	private void addStatementFact(AstNode node) {

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