package commitminer.js.diff.environment;

import java.util.Map;
import java.util.Set;

import org.mozilla.javascript.ast.AstNode;

import commitminer.analysis.flow.abstractdomain.Addresses;
import commitminer.analysis.flow.abstractdomain.Identifier;
import commitminer.analysis.flow.abstractdomain.State;
import commitminer.cfg.CFGEdge;
import commitminer.cfg.CFGNode;
import commitminer.cfg.ICFGVisitor;
import commitminer.factbase.Annotation;
import commitminer.js.diff.IsUsedVisitor;

/**
 * Extracts facts from a flow analysis.
 */
public class EnvCFGVisitor implements ICFGVisitor {

	/* The fact database we will populate. */
	private EnvFactBase factBase;

	public EnvCFGVisitor(EnvFactBase factBase) {
		this.factBase = factBase;
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
	private void getObjectFacts(AstNode node, Map<Identifier, Addresses> props, State state, String prefix) {
		for(Identifier prop : props.keySet()) {

			/* Get the environment changes. No need to recurse since
			 * properties (currently) do not change. */
			if(node != null) {
				Set<Annotation> annotations = isUsed(node, prop);
				for(Annotation annotation : annotations) {
					factBase.registerVariableChangeDependency(prop, annotation);
				}
			}

		}
	}

	/**
	 * @param node The statement in which the var/prop may be used.
	 * @param identity The var/prop to look for in the statement.
	 * @return the serialized list of lines where the var/prop is used in the statement.
	 */
	private Set<Annotation> isUsed(AstNode statement, Identifier identity) {
		return IsUsedVisitor.isUsed(statement, identity, true);
	}

}