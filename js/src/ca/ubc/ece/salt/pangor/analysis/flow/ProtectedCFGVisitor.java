package ca.ubc.ece.salt.pangor.analysis.flow;

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
import ca.ubc.ece.salt.pangor.analysis.flow.abstractdomain.BValue;
import ca.ubc.ece.salt.pangor.analysis.flow.abstractdomain.Null;
import ca.ubc.ece.salt.pangor.analysis.flow.abstractdomain.State;
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
		visit((AstNode) node.getStatement(), (State)node.getState());
	}

	@Override
	public void visit(CFGEdge edge) {
		visit((AstNode) edge.getCondition(), (State)edge.getState());
	}

	private void visit(AstNode node, State state) {

		for(String var : state.environment.environment.keySet()) {
			Address addr = state.environment.environment.get(var);
			BValue val = state.store.apply(addr);
			if(val.nullAD.le == Null.LatticeElement.BOTTOM)
				registerFact(node, var, "NULL", val.nullAD.le.toString(), "UNCHANGED");
			if(val.undefinedAD.le == Undefined.LatticeElement.BOTTOM)
				registerFact(node, var, "UNDEFINED", val.undefinedAD.le.toString(), "UNCHANGED");
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

		IPredicate predicate = Factory.BASIC.createPredicate("SpecialType", 8);
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