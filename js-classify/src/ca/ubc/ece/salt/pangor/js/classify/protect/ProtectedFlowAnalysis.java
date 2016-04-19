package ca.ubc.ece.salt.pangor.js.classify.protect;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.Stack;

import org.deri.iris.api.basics.IPredicate;
import org.deri.iris.api.basics.ITuple;
import org.deri.iris.factory.Factory;
import org.deri.iris.storage.IRelation;
import org.deri.iris.storage.IRelationFactory;
import org.deri.iris.storage.simple.SimpleRelationFactory;
import org.mozilla.javascript.ast.AstNode;
import org.mozilla.javascript.ast.ScriptNode;

import ca.ubc.ece.salt.pangor.analysis.SourceCodeFileChange;
import ca.ubc.ece.salt.pangor.analysis.flow.FlowAnalysis;
import ca.ubc.ece.salt.pangor.analysis.flow.IAbstractState;
import ca.ubc.ece.salt.pangor.cfg.CFG;
import ca.ubc.ece.salt.pangor.cfg.CFGEdge;
import ca.ubc.ece.salt.pangor.cfg.CFGNode;
import ca.ubc.ece.salt.pangor.js.analysis.scope.Scope;
import ca.ubc.ece.salt.pangor.js.classify.protect.ProtectedAbstractState.LatticeElement;
import ca.ubc.ece.salt.pangor.pointsto.PointsToPrediction;

/**
 * A change-sensitive analysis that identifies where identifiers are protected in JavaScript.
 */
public class ProtectedFlowAnalysis extends FlowAnalysis {

	@Override
	public ProtectedAbstractState entryValue(ScriptNode function) {
		return new ProtectedAbstractState();
	}

	@Override
	public void createFacts(SourceCodeFileChange sourceCodeFileChange,
			Map<IPredicate, IRelation> facts, CFG cfg, Scope<AstNode> scope,
			PointsToPrediction model) {

		/* Depth first traversal to visit all vertices and edges in the CFG. */
		CFGNode entry = cfg.getEntryNode();

		Stack<CFGNode> stack = new Stack<CFGNode>();
		stack.push(entry);

		Set<CFGNode> visited = new HashSet<CFGNode>();
		visited.add(entry);

		while(!stack.isEmpty()) {
			CFGNode node = stack.pop();

			/* Register facts from the node. */
			registerFacts((AstNode)node.getStatement(), node.getAS(), facts,
						  sourceCodeFileChange);

			for(CFGEdge edge : node.getEdges()) {
				if(!visited.contains(edge.getTo())) {

					/* Register facts from the edge. */
					registerFacts((AstNode)edge.getCondition(), node.getAS(), facts,
								  sourceCodeFileChange);

					stack.push(edge.getTo());

				}
			}
		}

	}

	/**
	 * Register facts from the abstract state {@code ias} at statement {@code statement}.
	 * @param statement The location of the abstract state.
	 * @param ias The abstract state (lattice elements).
	 */
	private void registerFacts(AstNode statement, IAbstractState ias,
							   Map<IPredicate, IRelation> facts,
							   SourceCodeFileChange sourceCodeFileChange) {

		if(!(ias instanceof ProtectedAbstractState)) throw new IllegalArgumentException("Requires ProtectedAbstractState");

		ProtectedAbstractState as = (ProtectedAbstractState) ias;

		for(LatticeElement le : as.latticeElements.keySet()) {
			ProtectedLatticeElement ple = as.latticeElements.get(le);

			IPredicate predicate = Factory.BASIC.createPredicate("SpecialType", 4);
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
					Factory.TERM.createString(le.identifier),
					Factory.TERM.createString(le.type.toString()),
					Factory.TERM.createString(ple.element.toString()),
					Factory.TERM.createString(ple.change.toString()));
			relation.add(tuple);

			facts.put(predicate,  relation);

		}
	}

}