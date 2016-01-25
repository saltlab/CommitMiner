package ca.ubc.ece.salt.pangor.js.learn.analysis;


import java.util.Map;

import org.deri.iris.api.basics.IPredicate;
import org.deri.iris.api.basics.ITuple;
import org.deri.iris.factory.Factory;
import org.deri.iris.storage.IRelation;
import org.deri.iris.storage.IRelationFactory;
import org.deri.iris.storage.simple.SimpleRelationFactory;
import org.mozilla.javascript.Token;
import org.mozilla.javascript.ast.AstNode;
import org.mozilla.javascript.ast.AstRoot;
import org.mozilla.javascript.ast.BreakStatement;
import org.mozilla.javascript.ast.ContinueStatement;
import org.mozilla.javascript.ast.FunctionNode;
import org.mozilla.javascript.ast.InfixExpression;
import org.mozilla.javascript.ast.KeywordLiteral;
import org.mozilla.javascript.ast.Name;
import org.mozilla.javascript.ast.NewExpression;
import org.mozilla.javascript.ast.NodeVisitor;
import org.mozilla.javascript.ast.NumberLiteral;
import org.mozilla.javascript.ast.ReturnStatement;
import org.mozilla.javascript.ast.ScriptNode;
import org.mozilla.javascript.ast.StringLiteral;
import org.mozilla.javascript.ast.TryStatement;
import org.mozilla.javascript.ast.UnaryExpression;
import org.mozilla.javascript.ast.VariableDeclaration;

import ca.ubc.ece.salt.gumtree.ast.ClassifiedASTNode.ChangeType;
import ca.ubc.ece.salt.pangor.analysis.SourceCodeFileChange;
import ca.ubc.ece.salt.pangor.api.KeywordDefinition.KeywordType;
import ca.ubc.ece.salt.pangor.api.KeywordUse;
import ca.ubc.ece.salt.pangor.api.KeywordUse.KeywordContext;
import ca.ubc.ece.salt.pangor.js.analysis.utilities.AnalysisUtilities;
import ca.ubc.ece.salt.pangor.js.analysis.utilities.SpecialTypeAnalysisUtilities;
import ca.ubc.ece.salt.pangor.js.api.JSAPIUtilities;
import ca.ubc.ece.salt.pangor.pointsto.PointsToPrediction;

/**
 * Inspects scripts and functions for API keywords.
 */
public class LearningAnalysisVisitor implements NodeVisitor {

	/** True if this is a destination file analysis. **/
	private boolean dst;

	/** The fact database. **/
	private Map<IPredicate, IRelation> facts;

	/** The root of the function or script we are visiting. **/
	private ScriptNode root;

	/**
	 * Utility for predicting points-to relationships between keywords and
	 * methods/fields/events in packages.
	 */
	private PointsToPrediction packageModel;

	/**
	 * If true, will visit function signatures and bodies. This should be true
	 * when doing the initial keyword extraction (at the file level) and false
	 * when collecting features at the function level.
	 */
	private boolean visitFunctions;

	/**
	 * Visits the script and returns a feature vector containing only the
	 * keywords in the script. The feature vector will not contain package
	 * associations for the keywords. Unlike {@code getFunctionFeatureVector},
	 * this method extracts keywords from the entire script (i.e., it visits
	 * function declarations and bodies).
	 * @param script The script to extract keywords from.
	 * @return A feature vector containing the keywords extracted from the
	 * 		   script.
	 */
	public static void getLearningFacts(Map<IPredicate, IRelation> facts,
			SourceCodeFileChange scfc, AstRoot script, boolean dst) {

		/* Create the feature vector by visiting the function. */
		LearningAnalysisVisitor visitor = new LearningAnalysisVisitor(facts, scfc,
				AnalysisUtilities.getFunctionName(script), script, null, true,
				dst);
		script.visit(visitor);
	}

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
		LearningAnalysisVisitor visitor = new LearningAnalysisVisitor(facts, scfc,
				AnalysisUtilities.getFunctionName(function), function, packageModel,
				false, dst);
		function.visit(visitor);
	}


	private LearningAnalysisVisitor(Map<IPredicate, IRelation> facts,
			SourceCodeFileChange scfc, String functionName, ScriptNode root,
			PointsToPrediction packageModel, boolean visitFunctions,
			boolean dst) {
		this.facts = facts;
		this.packageModel = packageModel;
		this.root = root;
		this.visitFunctions = visitFunctions;
		this.dst = dst;
	}

	@Override
	public boolean visit(AstNode node) {

		/* Check for keywords. */
		this.registerKeyword(node, node.getChangeType());

		/* Stop if this is a function declaration. */
		if(!this.visitFunctions && node instanceof FunctionNode && node != this.root) {
			return false;
		}

		return true;

	}

	/**
	 * If the node is a potential keyword (Name, StringLiteral or NumberLiteral),
	 * get the node's context and look up the most likely artifact in a package
	 * that the keyword points to.
	 *
	 * @param node The node to check.
	 * @param changeType How the node has been modified (inserted, removed,
	 * 					 updated, etc.)
	 */
	private void registerKeyword(AstNode node, ChangeType changeType) {

		String token = "";

		KeywordType type = JSAPIUtilities.getTokenType(node);
		KeywordContext context = JSAPIUtilities.getTokenContext(node);

		if(type == KeywordType.UNKNOWN || context == KeywordContext.UNKNOWN) return;

		/*
		 * ChangeType.MOVED is causing a lot of noise, specially because when a
		 * function is inserted in a file, all functions "below" it are tagged
		 * as MOVED, and all keywords within this function are also marked as
		 * MOVED. For now, we relabel MOVED change types as UNCHANGED.
		 */
		if (changeType == ChangeType.MOVED)
			changeType = ChangeType.UNCHANGED;

		/* Add a typeof keyword if we're checking if this node is truthy or
		 * falsey. */
		if(SpecialTypeAnalysisUtilities.isFalsey(node)) {

			KeywordUse keyword = null;
			if(this.packageModel != null) {
				keyword = this.packageModel.getKeyword(type, context, "falsey", changeType);
			}
			else {
				keyword = new KeywordUse(type, context, "falsey", changeType);
				keyword.apiPackage = "global";
			}

			if(keyword != null) {
				this.addKeywordChangeFact(keyword);
			}

		}

		/* Get the relevant keyword from the node. */
		if(node instanceof ReturnStatement) {
			token = "return";
		}
		else if(node instanceof BreakStatement) {
			token = "break";
		}
		else if(node instanceof ContinueStatement) {
			token = "continue";
		}
		else if (node instanceof VariableDeclaration) {
			token = "var";
		}
		else if (node instanceof NewExpression) {
			token = "new";
		}
		else if (node instanceof TryStatement) {
			token = "try";
		}
		else if(node instanceof Name) {
			Name name = (Name) node;
			token = name.getIdentifier();
			if(token.matches("e|err|error|exception")) {
				type = KeywordType.RESERVED;
				token = "error";
			}
			else if(token.matches("cb|callb|callback")) {
				type = KeywordType.RESERVED;
				token = "callback";
			}
		}
		else if(node instanceof KeywordLiteral) {
			KeywordLiteral kl = (KeywordLiteral) node;
			token = kl.toSource();
		}
		else if(node instanceof NumberLiteral) {
			NumberLiteral nl = (NumberLiteral) node;
			try {
				if(Double.parseDouble(nl.getValue()) == 0.0) {
					token = "zero";
				}
			}
			catch (NumberFormatException ignore) { }
		}
		else if(node instanceof StringLiteral) {
			StringLiteral sl = (StringLiteral) node;
			if(sl.getValue().isEmpty()) {
				token = "blank";
			}
			else {
				token = sl.getValue();
			}
		}
		else if(node instanceof UnaryExpression) {
			UnaryExpression ue = (UnaryExpression) node;
			switch (ue.getOperator()) {
			case Token.TYPEOF:
				token = "typeof";
				break;
			}
		}
		else if(node instanceof InfixExpression) {
			InfixExpression ie = (InfixExpression) node;
			if(ie.getType() == Token.SHEQ || ie.getType() == Token.SHNE) {
				if(SpecialTypeAnalysisUtilities.getSpecialType(ie.getLeft()) != null ||
				   SpecialTypeAnalysisUtilities.getSpecialType(ie.getRight()) != null) {
					/* Then we consider it a 'typeof' keyword. */
					token = "typeof";
				}
			}
		}

		/* Insert the token into the feature vector if it is a keyword. */
		KeywordUse keyword = null;

		if(this.packageModel != null) {
			keyword = this.packageModel.getKeyword(type, context, token, changeType);
		}
		else {
			keyword = new KeywordUse(type, context, token, changeType);
		}

		/* Add the keyword to the feature vector. */
		if(keyword != null) {
			this.addKeywordChangeFact(keyword);
		}

	}

	/**
	 * Adds a KeywordUse fact to the fact database.
	 * @param keyword The keyword change to record.
	 */
	private void addKeywordChangeFact(KeywordUse keyword) {

		/* Check the change type against the analysis we're doing. */
		if(this.dst && !(keyword.changeType == ChangeType.INSERTED
				|| keyword.changeType == ChangeType.UPDATED)) return;
		if(!this.dst && !(keyword.changeType == ChangeType.REMOVED)) return;

		/* Get the relation for this predicate from the fact base. */
		IPredicate predicate = Factory.BASIC.createPredicate("KeywordChange", 5);
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
				Factory.TERM.createString(keyword.type.toString()),
				Factory.TERM.createString(keyword.context.toString()),
				Factory.TERM.createString(keyword.getPackageName()),
				Factory.TERM.createString(keyword.changeType.toString()),
				Factory.TERM.createString(keyword.keyword));
		relation.add(tuple);

		this.facts.put(predicate, relation);
	}

}