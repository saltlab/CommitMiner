package ca.ubc.ece.salt.pangor.js.classify.protect;

import java.util.Map;

import org.deri.iris.api.basics.IPredicate;
import org.deri.iris.api.basics.ITuple;
import org.deri.iris.factory.Factory;
import org.deri.iris.storage.IRelation;
import org.deri.iris.storage.IRelationFactory;
import org.deri.iris.storage.simple.SimpleRelationFactory;
import org.mozilla.javascript.ast.AstNode;

import ca.ubc.ece.salt.pangor.analysis.SourceCodeFileChange;
import ca.ubc.ece.salt.pangor.analysis.flow.abstractdomain.Address;
import ca.ubc.ece.salt.pangor.analysis.flow.abstractdomain.Addresses.LatticeElement;
import ca.ubc.ece.salt.pangor.analysis.flow.abstractdomain.BValue;
import ca.ubc.ece.salt.pangor.analysis.flow.abstractdomain.Bool;
import ca.ubc.ece.salt.pangor.analysis.flow.abstractdomain.Identifier;
import ca.ubc.ece.salt.pangor.analysis.flow.abstractdomain.Null;
import ca.ubc.ece.salt.pangor.analysis.flow.abstractdomain.Num;
import ca.ubc.ece.salt.pangor.analysis.flow.abstractdomain.State;
import ca.ubc.ece.salt.pangor.analysis.flow.abstractdomain.Str;
import ca.ubc.ece.salt.pangor.analysis.flow.abstractdomain.Undefined;
import ca.ubc.ece.salt.pangor.cfg.CFGEdge;
import ca.ubc.ece.salt.pangor.cfg.CFGNode;
import ca.ubc.ece.salt.pangor.cfg.ICFGVisitor;

/**
 * Extracts facts from a flow analysis.
 */
public class ProtectedCFGVisitor implements ICFGVisitor {

	private SourceCodeFileChange sourceCodeFileChange;

	/* The fact database we will populate. */
	private Map<IPredicate, IRelation> facts;

	public ProtectedCFGVisitor(SourceCodeFileChange sourceCodeFileChange, Map<IPredicate, IRelation> facts) {
		this.sourceCodeFileChange = sourceCodeFileChange;
		this.facts = facts;
	}

	@Override
	public void visit(CFGNode node) {
		visit((AstNode) node.getStatement(), (State)node.getBeforeState());
	}

	@Override
	public void visit(CFGEdge edge) {
		visit((AstNode) edge.getCondition(), (State)edge.getBeforeState());
	}

	/**
	 * Visit an AstNode (a statement or condition) and extract facts about
	 * identifier protection.
	 */
	private void visit(AstNode node, State state) {
		// TODO: There are currently situations in which a function is not
		//		 analyzed... for example a function that is passed as an
		//		 argument to an un-resolvable function. We need to handle
		//		 these.
		if(state != null) getObjectFacts(node, state.env.environment, state, null);
	}

	/**
	 * Recursively visits objects and extracts facts about identifier
	 * protection.
	 * @param node The statement or condition at the program point.
	 * @param props The environment or object properties.
	 */
	private void getObjectFacts(AstNode node, Map<Identifier, Address> props, State state, String prefix) {
		for(Identifier prop : props.keySet()) {

			Address addr = props.get(prop);
			String identifier;
			if(prefix == null) identifier = prop.name;
			else identifier = prefix + "." + prop;

			BValue val = state.store.apply(addr);
			if(val.nullAD.le == Null.LatticeElement.BOTTOM)
				registerFact(node, identifier, "NULL", val.nullAD.le.toString(), val.nullAD.change.toString());
			if(val.undefinedAD.le == Undefined.LatticeElement.BOTTOM)
				registerFact(node, identifier, "UNDEFINED", val.undefinedAD.le.toString(), val.undefinedAD.change.toString());
			if(Str.notBlank(val.stringAD))
				registerFact(node, identifier, "BLANK", val.stringAD.toString(), val.stringAD.change.toString());
			if(Num.notZero(val.numberAD))
				registerFact(node, identifier, "ZERO", val.numberAD.toString(), val.numberAD.change.toString());
			if(Num.notNaN(val.numberAD))
				registerFact(node, identifier, "NAN", val.numberAD.toString(), val.numberAD.change.toString());
			if(Bool.notFalse(val.booleanAD))
				registerFact(node, identifier, "FALSE", val.booleanAD.toString(), val.booleanAD.change.toString());

			/* Recursively check property values. */
			if(val.addressAD.le == LatticeElement.TOP) continue;
			for(Address objAddr : val.addressAD.addresses) {
				getObjectFacts(node, state.store.getObj(objAddr).externalProperties, state, identifier);
			}

		}
	}

	/**
	 * @param statement The statement for which we are registering a fact.
	 * @param identifier The identifer for which we are registering a fact.
	 * @param ad The abstract domain of the fact.
	 * @param tle The type lattice element.
	 * @param cle The change lattice element.
	 */
	private void registerFact(AstNode statement, String identifier, String ad, String tle, String cle) {

		if(statement == null || statement.getID() == null) return;

		IPredicate predicate = Factory.BASIC.createPredicate("Protected", 8);
		IRelation relation = facts.get(predicate);
		if(relation == null) {
			IRelationFactory relationFactory = new SimpleRelationFactory();
			relation = relationFactory.createRelation();
			facts.put(predicate, relation);
		}

		/* Add the new tuple to the relation. */
		ITuple tuple = Factory.BASIC.createTuple(
				Factory.TERM.createString(statement.getVersion().toString()),
				Factory.TERM.createString(sourceCodeFileChange.repairedFile),
				Factory.TERM.createString(String.valueOf(statement.getLineno())),
				Factory.TERM.createString(String.valueOf(statement.getID())),
				Factory.TERM.createString(identifier),
				Factory.TERM.createString(ad),
				Factory.TERM.createString(tle),
				Factory.TERM.createString(cle));
		relation.add(tuple);

		facts.put(predicate,  relation);

	}

}