package commitminer.js.diff.value;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.deri.iris.api.basics.IPredicate;
import org.deri.iris.api.basics.ITuple;
import org.deri.iris.factory.Factory;
import org.deri.iris.storage.IRelation;
import org.deri.iris.storage.IRelationFactory;
import org.deri.iris.storage.simple.SimpleRelationFactory;
import org.mozilla.javascript.ast.AstNode;

import commitminer.analysis.SourceCodeFileChange;
import commitminer.analysis.flow.abstractdomain.Address;
import commitminer.analysis.flow.abstractdomain.BValue;
import commitminer.analysis.flow.abstractdomain.Identifier;
import commitminer.analysis.flow.abstractdomain.Obj;
import commitminer.analysis.flow.abstractdomain.State;
import commitminer.analysis.flow.abstractdomain.Addresses.LatticeElement;
import commitminer.cfg.CFGEdge;
import commitminer.cfg.CFGNode;
import commitminer.cfg.ICFGVisitor;
import commitminer.js.diff.Annotation;
import commitminer.js.diff.IsUsedVisitor;

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
		if(state != null) getObjectFacts(node, state.env.environment, state, null, new HashSet<Address>());
	}

	/**
	 * Recursively visits objects and extracts facts about environment changes.
	 * @param node The statement or condition at the program point.
	 * @param props The environment or object properties.
	 */
	private void getObjectFacts(AstNode node, Map<Identifier, Address> props, State state, String prefix, Set<Address> visited) {
		for(Identifier prop : props.keySet()) {

			Address addr = props.get(prop);

			/* Avoid circular references. */
			if(visited.contains(addr)) continue;
			visited.add(addr);

			String identifier;
			if(prefix == null) identifier = prop.name;
			else identifier = prefix + "." + prop.name;

			if(identifier.equals("this")) continue;
			if(addr == null) continue;

			BValue val = state.store.apply(addr);

			/* Get the environment changes. No need to recurse since
			 * properties (currently) do not change. */
			if(node != null) {
				Set<Annotation> annotations = isUsed(node, prop);
				for(Annotation annotation : annotations) {
					registerFact(node, prop.name, val.change.toString(), annotation);
				}
			}

			/* Recursively check property values. */
			if(val.addressAD.le == LatticeElement.TOP) continue;
			for(Address propAddr : val.addressAD.addresses) {
				Obj propObj = state.store.getObj(propAddr);
				getObjectFacts(node, propObj.externalProperties, state, identifier, visited);
			}

		}
	}

	/**
	 * @param node The statement in which the var/prop may be used.
	 * @param identity The var/prop to look for in the statement.
	 * @return the serialized list of lines where the var/prop is used in the statement.
	 */
	private Set<Annotation> isUsed(AstNode statement, Identifier identity) {
		return IsUsedVisitor.isUsed(statement, identity, false);
	}

	/**
	 * @param statement The statement for which we are registering a fact.
	 * @param identifier The identifier for which we are registering a fact.
	 * @param cle The change lattice element.
	 */
	private void registerFact(AstNode statement,
							  String identifier,
							  String cle,
							  Annotation annotation) {

		if(statement == null || statement.getID() == null) return;

		IPredicate predicate = Factory.BASIC.createPredicate("Value", 8);
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
				Factory.TERM.createString(annotation.line.toString()),
				Factory.TERM.createString(annotation.absolutePosition.toString()),
				Factory.TERM.createString(annotation.length.toString()),
				Factory.TERM.createString(String.valueOf(statement.getID())),
				Factory.TERM.createString(identifier),
				Factory.TERM.createString(cle));
		relation.add(tuple);

	}

}