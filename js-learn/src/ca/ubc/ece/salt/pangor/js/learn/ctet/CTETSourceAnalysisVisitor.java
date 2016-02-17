package ca.ubc.ece.salt.pangor.js.learn.ctet;


import java.util.Map;

import org.deri.iris.api.basics.IPredicate;
import org.deri.iris.api.basics.ITuple;
import org.deri.iris.factory.Factory;
import org.deri.iris.storage.IRelation;
import org.deri.iris.storage.IRelationFactory;
import org.deri.iris.storage.simple.SimpleRelationFactory;
import org.mozilla.javascript.ast.AstNode;
import org.mozilla.javascript.ast.ConditionalExpression;
import org.mozilla.javascript.ast.DoLoop;
import org.mozilla.javascript.ast.ForLoop;
import org.mozilla.javascript.ast.FunctionNode;
import org.mozilla.javascript.ast.IfStatement;
import org.mozilla.javascript.ast.NodeVisitor;
import org.mozilla.javascript.ast.ScriptNode;
import org.mozilla.javascript.ast.SwitchStatement;
import org.mozilla.javascript.ast.WhileLoop;

import ca.ubc.ece.salt.gumtree.ast.ClassifiedASTNode.ChangeType;
import ca.ubc.ece.salt.pangor.analysis.SourceCodeFileChange;
import ca.ubc.ece.salt.pangor.api.KeywordDefinition.KeywordType;
import ca.ubc.ece.salt.pangor.api.KeywordUse.KeywordContext;
import ca.ubc.ece.salt.pangor.js.analysis.utilities.AnalysisUtilities;
import ca.ubc.ece.salt.pangor.js.api.JSAPIUtilities;
import ca.ubc.ece.salt.pangor.pointsto.PointsToPrediction;

/**
 * Inspects scripts and functions for API keywords.
 */
public class CTETSourceAnalysisVisitor implements NodeVisitor {

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

		/* Get the maximum nesting depth of the function

		/* Create the feature vector by visiting the function. */
		CTETSourceAnalysisVisitor visitor = new CTETSourceAnalysisVisitor(facts, scfc,
				AnalysisUtilities.getFunctionName(function), function,  dst);
		function.visit(visitor);
	}


	private CTETSourceAnalysisVisitor(Map<IPredicate, IRelation> facts,
			SourceCodeFileChange scfc, String functionName, ScriptNode root,
			boolean dst) {
		this.facts = facts;
		this.dst = dst;
	}

	@Override
	public boolean visit(AstNode node) {

		KeywordType type = JSAPIUtilities.getTokenType(node);
		KeywordContext context = JSAPIUtilities.getTokenContext(node);

		if(type == KeywordType.UNKNOWN || context == KeywordContext.UNKNOWN) return true;

		this.checkFunctionName(node);
		this.checkFunctionality(node);
		this.checkParameter(node, type, context);
		this.checkElsePart(node);
		this.checkStatements(node);
		this.checkConditionExpression(node);

		return true;

	}

	/**
	 * Adds a CTET fact to the fact database.
	 * @param changeType The change type according to appendix A of
	 * 					 "Classifying Change Types for Qualifying Change Couplings"
	 * @param entityType The entity that is being acted upon by the change. E.g.
	 * 					 in "Appendix of `Mining Software Repair Models for
	 * 					 Reasoning on the Search Space of Automated Program
	 * 					 Fixing". This is essentially the AST node type.
	 */
	private void addCTETFact(String changeType, String entityType) {

		/* Get the relation for this predicate from the fact base. */
		IPredicate predicate = Factory.BASIC.createPredicate("CTET", 5);
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
				Factory.TERM.createString(changeType),
				Factory.TERM.createString(entityType),
				Factory.TERM.createString(getUniqueID().toString()));
		relation.add(tuple);

		this.facts.put(predicate, relation);
	}

	/**
	 * Registers `AdditionalFunctionality`
	 */
	private void checkFunctionality(AstNode node) {

		/* Inserted functionality. */
		if(node instanceof FunctionNode && node.getChangeType() == ChangeType.INSERTED) {
			this.addCTETFact("AdditionalFunctionality", node.getClass().getSimpleName());
		}

	}

	/**
	 * Registers `MethodRenaming` fact.
	 * @param node
	 */
	private void checkFunctionName(AstNode node) {

		if(node instanceof FunctionNode) {
			FunctionNode functionNode = (FunctionNode) node;
			if(functionNode.getMapping() != null &&
			   functionNode.getName() != ((FunctionNode)functionNode.getMapping()).getName()) {
				this.addCTETFact("MethodRenaming", node.getClass().getSimpleName());
			}
		}

	}

	/**
	 * Registers `ParameterInsert` facts.
	 */
	private void checkParameter(AstNode node, KeywordType type, KeywordContext context) {

		/* Parameter insert */
		if(type == KeywordType.PARAMETER && node.getChangeType() == ChangeType.INSERTED) {
			this.addCTETFact("ParameterInsert", "SingleVariableDeclaration");
		}

	}

	/**
	 * Registers `Else-PartInsert` facts.
	 */
	private void checkElsePart(AstNode node) {

		ChangeType change = node.getChangeType();

		if(node instanceof IfStatement) {
			IfStatement ifStatement = (IfStatement) node;
			if(ifStatement.getElsePart().getChangeType() == ChangeType.INSERTED) {
				this.addCTETFact("Else-PartInsert", ifStatement.getClass().getSimpleName());
			}
		}

	}

	/**
	 * Registers `Statement[Insert,Update,OrderingChange,ParentChange]` facts.
	 */
	private void checkStatements(AstNode node) {

		ChangeType change = node.getChangeType();

		if(node.isStatement()) {
			switch(change) {
			case INSERTED:
				this.addCTETFact("StatementInsert", node.getClass().getSimpleName());
				break;
			case UPDATED:
				this.addCTETFact("StatementUpdate", node.getClass().getSimpleName());
				break;
			case MOVED:
				if(node.getParent().getMapping() == ((AstNode)node.getMapping()).getParent()) {
					this.addCTETFact("StatementParentChange", node.getClass().getSimpleName());
				}
				else {
					this.addCTETFact("StatementOrderingChange", node.getClass().getSimpleName());
				}
				break;
			default:
				break;
			}
		}

	}

	/**
	 * Registers `ConditionExpressionChange` facts.
	 */
	private void checkConditionExpression(AstNode node) {

		if(node instanceof IfStatement) {
			IfStatement ifStatement = (IfStatement) node;
			if(ifStatement.getCondition().getChangeType() != ChangeType.UNCHANGED) {
				this.addCTETFact("ConditionExpressionChange", node.getClass().getSimpleName());
			}
		}
		else if(node instanceof ForLoop) {
			ForLoop forLoop = (ForLoop) node;
			if(forLoop.getCondition().getChangeType() != ChangeType.UNCHANGED) {
				this.addCTETFact("ConditionExpressionChange", node.getClass().getSimpleName());
			}
		}
		else if(node instanceof WhileLoop) {
			WhileLoop whileLoop = (WhileLoop) node;
			if(whileLoop.getCondition().getChangeType() != ChangeType.UNCHANGED) {
				this.addCTETFact("ConditionExpressionChange", node.getClass().getSimpleName());
			}
		}
		else if(node instanceof DoLoop) {
			DoLoop doLoop = (DoLoop) node;
			if(doLoop.getCondition().getChangeType() != ChangeType.UNCHANGED) {
				this.addCTETFact("ConditionExpressionChange", node.getClass().getSimpleName());
			}
		}
		else if(node instanceof ConditionalExpression) {
			ConditionalExpression ce = (ConditionalExpression) node;
			if(ce.getTestExpression().getChangeType() != ChangeType.UNCHANGED) {
				this.addCTETFact("ConditionExpressionChange", node.getClass().getSimpleName());
			}
		}
		else if(node instanceof SwitchStatement) {
			SwitchStatement ss = (SwitchStatement) node;
			if(ss.getExpression().getChangeType() != ChangeType.UNCHANGED) {
				this.addCTETFact("ConditionExpressionChange", node.getClass().getSimpleName());
			}
		}

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