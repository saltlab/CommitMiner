package commitminer.test.value;

import java.util.Map;

import org.deri.iris.api.basics.IPredicate;
import org.deri.iris.api.basics.ITuple;
import org.deri.iris.factory.Factory;
import org.deri.iris.storage.IRelation;
import org.deri.iris.storage.IRelationFactory;
import org.deri.iris.storage.simple.SimpleRelationFactory;
import org.mozilla.javascript.ast.AstNode;

import commitminer.analysis.SourceCodeFileChange;
import commitminer.analysis.flow.abstractdomain.Address;
import commitminer.analysis.flow.abstractdomain.Addresses;
import commitminer.analysis.flow.abstractdomain.BValue;
import commitminer.analysis.flow.abstractdomain.Identifier;
import commitminer.analysis.flow.abstractdomain.State;
import commitminer.analysis.flow.abstractdomain.Addresses.LatticeElement;
import commitminer.cfg.CFGEdge;
import commitminer.cfg.CFGNode;
import commitminer.cfg.ICFGVisitor;

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
		if(state != null) getEnvironmentFacts(node, state.env.environment, state, null);
	}

	/**
	 * Visits variables in the environment and extracts facts.
	 * @param node The statement or condition at the program point.
	 * @param props The environment or object properties.
	 */
	private void getEnvironmentFacts(AstNode node, Map<Identifier, Addresses> props, State state, String prefix) {
		for(Map.Entry<Identifier, Addresses> entry : props.entrySet()) {
			for(Address addr : entry.getValue().addresses) {
				getPropertyFacts(node, entry.getKey(), addr, state, prefix);
			}
		}
	}

	/**
	 * Visits objects in the store and extracts facts.
	 * @param node The statement or condition at the program point.
	 * @param props The environment or object properties.
	 */
	private void getObjectFacts(AstNode node, Map<Identifier, Address> props, State state, String prefix) {
		for(Map.Entry<Identifier, Address> entry : props.entrySet()) {
			getPropertyFacts(node, entry.getKey(), entry.getValue(), state, prefix);
		}
	}

	/**
	 * Recursively extracts facts from objects.
	 * @param node The statement or condition at the program point.
	 * @param props The environment or object properties.
	 */
	private void getPropertyFacts(AstNode node, Identifier prop, Address addr, State state, String prefix) {

		String identifier;
		if(prefix == null) identifier = prop.name;
		else identifier = prefix + "." + prop.name;

		if(identifier.equals("this")) return;
		if(addr == null) return;

		BValue val = state.store.apply(addr);

		/* Get the environment changes. No need to recurse since
		 * properties (currently) do not change. */
		registerFact(node, prop.name, val.change.toString());

		/* Recursively check property values. */
		if(val.addressAD.le == LatticeElement.TOP) return;
		for(Address objAddr : val.addressAD.addresses) {
			getObjectFacts(node, state.store.getObj(objAddr).externalProperties, state, identifier);
		}

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