package ca.ubc.ece.salt.pangor.js.diff.control;

import java.util.Map;
import java.util.Set;

import org.deri.iris.api.basics.IPredicate;
import org.deri.iris.api.basics.ITuple;
import org.deri.iris.factory.Factory;
import org.deri.iris.storage.IRelation;
import org.deri.iris.storage.IRelationFactory;
import org.deri.iris.storage.simple.SimpleRelationFactory;
import org.mozilla.javascript.ast.AstNode;
import org.mozilla.javascript.ast.FunctionNode;
import org.mozilla.javascript.ast.Name;

import ca.ubc.ece.salt.gumtree.ast.ClassifiedASTNode.ChangeType;
import ca.ubc.ece.salt.pangor.analysis.SourceCodeFileChange;
import ca.ubc.ece.salt.pangor.analysis.flow.abstractdomain.Address;
import ca.ubc.ece.salt.pangor.analysis.flow.abstractdomain.Identifier;
import ca.ubc.ece.salt.pangor.analysis.flow.abstractdomain.State;
import ca.ubc.ece.salt.pangor.cfg.CFGEdge;
import ca.ubc.ece.salt.pangor.cfg.CFGNode;
import ca.ubc.ece.salt.pangor.cfg.ICFGVisitor;
import ca.ubc.ece.salt.pangor.js.diff.Annotation;

/**
 * Extracts facts from a flow analysis.
 */
public class ControlCFGVisitor implements ICFGVisitor {

	private SourceCodeFileChange sourceCodeFileChange;

	/* The fact database we will populate. */
	private Map<IPredicate, IRelation> facts;

	public ControlCFGVisitor(SourceCodeFileChange sourceCodeFileChange, Map<IPredicate, IRelation> facts) {
		this.sourceCodeFileChange = sourceCodeFileChange;
		this.facts = facts;
	}

	@Override
	public void visit(CFGNode node) {
		visit((AstNode) node.getStatement(), (State)node.getBeforeState());
	}

	@Override
	public void visit(CFGEdge edge) {

		/* If the condition has changed, it is a control flow change def. */
		if(edge.getCondition() != null) {
			AstNode condition = (AstNode)edge.getCondition();
			if(condition.getChangeType() != ChangeType.UNCHANGED
				&& condition.getChangeType() != ChangeType.UNKNOWN
				&& condition.getVersion() != null) {
				registerDefinitionFact(condition);
			}
		}

		visit((AstNode) edge.getCondition(), (State)edge.getBeforeState());

	}

	/**
	 * Visit an AstNode (a statement or condition) and extract facts about
	 * identifier protection.
	 */
	private void visit(AstNode node, State state) {
		if(state != null) getObjectFacts(node, state.env.environment, state, null);
	}

	/**
	 * Recursively visits objects and extracts facts about environment changes.
	 * @param node The statement or condition at the program point.
	 * @param props The environment or object properties.
	 */
	private void getObjectFacts(AstNode node, Map<Identifier, Address> props, State state, String prefix) {

		if(node == null || node.getID() == null) return;

		/* Look for control flow change definitions. */
		Set<AstNode> definitions = ControlDefVisitor.getDefs(node);
		for(AstNode definition : definitions) {
			registerDefinitionFact(definition);
		}

		/* If the branch change set is non-empty, this statement is affected
		 * by a control flow change. */
		if(!state.control.conditions.isEmpty()) {
			registerStatementFact(node, "Change:CHANGED");
		}

	}

	/**
	 * @param definition The control flow change definition
	 */
	private void registerDefinitionFact(AstNode definition) {

		/* Create an annotation for this definition. */
		Annotation annotation = new Annotation(
				definition.getLineno(),
				definition.getAbsolutePosition(),
				definition.toSource().length());

		registerFact(definition, annotation, "Change:CHANGED", "CONTROL-DEF");

	}

	/**
	 * @param statement The statement for which we are registering a fact.
	 * @param ad The abstract domain of the fact.
	 * @param cle The change lattice element.
	 */
	private void registerStatementFact(AstNode statement, String cle) {

		Annotation annotation;

		if(statement instanceof FunctionNode) {
			/* If this is a function definition, only highlight the 'function'
			 * part. */
			FunctionNode function = (FunctionNode)statement;
			Name name = function.getFunctionName();

			int length = 8;

			if(name != null) {
				length = name.getAbsolutePosition()
						+ function.getFunctionName().length()
						- statement.getAbsolutePosition();
			}

			annotation = new Annotation(statement.getLineno(),
					statement.getAbsolutePosition(),
					length);
		}
		else {
			/* Highlight the whole statement. */
			annotation = new Annotation(statement.getLineno(),
					statement.getAbsolutePosition(),
					statement.getLength());
		}

		registerFact(statement, annotation, cle, "CONTROL");

	}

	private void registerFact(AstNode node, Annotation annotation,
							  String cle, String subType) {

		IPredicate predicate = Factory.BASIC.createPredicate("Control", 8);
		IRelation relation = facts.get(predicate);
		if(relation == null) {
			IRelationFactory relationFactory = new SimpleRelationFactory();
			relation = relationFactory.createRelation();
			facts.put(predicate, relation);
		}

		/* Add the new tuple to the relation. */
		ITuple tuple = Factory.BASIC.createTuple(
				Factory.TERM.createString(node.getVersion().toString()),
				Factory.TERM.createString(sourceCodeFileChange.repairedFile),
				Factory.TERM.createString(annotation.line.toString()),
				Factory.TERM.createString(annotation.absolutePosition.toString()),
				Factory.TERM.createString(annotation.length.toString()),
				Factory.TERM.createString(String.valueOf(node.getID())),
				Factory.TERM.createString(subType),
				Factory.TERM.createString(cle));
		relation.add(tuple);

	}

}