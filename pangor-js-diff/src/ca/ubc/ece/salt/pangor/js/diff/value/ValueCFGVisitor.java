package ca.ubc.ece.salt.pangor.js.diff.value;

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
import ca.ubc.ece.salt.pangor.analysis.flow.abstractdomain.Identifier;
import ca.ubc.ece.salt.pangor.analysis.flow.abstractdomain.State;
import ca.ubc.ece.salt.pangor.cfg.CFGEdge;
import ca.ubc.ece.salt.pangor.cfg.CFGNode;
import ca.ubc.ece.salt.pangor.cfg.ICFGVisitor;

/**
 * Extracts facts from a flow analysis.
 */
public class ValueCFGVisitor implements ICFGVisitor {

	private SourceCodeFileChange sourceCodeFileChange;

	/* The fact database we will populate. */
	private Map<IPredicate, IRelation> facts;

	public ValueCFGVisitor(SourceCodeFileChange sourceCodeFileChange, Map<IPredicate, IRelation> facts) {
		this.sourceCodeFileChange = sourceCodeFileChange;
		this.facts = facts;
	}

	@Override
	public void visit(CFGNode node) {
		visit((AstNode) node.getStatement(), (State)node.getAfterState());
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
		if(state != null) getObjectFacts(node, state.env.environment, state, null);
	}

	/**
	 * Recursively visits objects and extracts facts about environment changes.
	 * @param node The statement or condition at the program point.
	 * @param props The environment or object properties.
	 */
	private void getObjectFacts(AstNode node, Map<Identifier, Address> props, State state, String prefix) {
		for(Identifier prop : props.keySet()) {

			Address addr = props.get(prop);
			String identifier;
			if(prefix == null) identifier = prop.name;
			else identifier = prefix + "." + prop.name;

			if(identifier.equals("this")) continue;
			if(addr == null) continue;

			// TODO: Why do some of these not exist in the store?
			BValue val = state.store.apply(addr);

			/* Get the environment changes. No need to recurse since
			 * properties (currently) do not change. */
			if(node != null && isUsed(node, prop))
				registerFact(node, prop.name, val.change.toString());

			/* Recursively check property values. */
//			if(val == null && node != null)
//				continue; // This never executes. This is good... but why?
//			if(val == null)
//				continue;
			if(val.addressAD.le == LatticeElement.TOP) continue;
			for(Address objAddr : val.addressAD.addresses) {
				getObjectFacts(node, state.store.getObj(objAddr).externalProperties, state, identifier);
			}

		}
	}

	/**
	 * @param node The statement in which the var/prop may be used.
	 * @param identity The var/prop to look for in the statement.
	 * @return true if the var/prop is used in the statement.
	 */
	private boolean isUsed(AstNode statement, Identifier identity) {
		return IsUsedVisitor.isUsed(statement, identity);
	}

	/**
	 * @param statement The statement for which we are registering a fact.
	 * @param identifier The identifier for which we are registering a fact.
	 * @param cle The change lattice element.
	 */
	private void registerFact(AstNode statement, String identifier, String cle) {

		if(statement == null || statement.getID() == null) return;

		IPredicate predicate = Factory.BASIC.createPredicate("Value", 6);
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
				Factory.TERM.createString(cle));
		relation.add(tuple);

	}

}