package commitminer.js.diff.defenvironment;

import java.util.Set;

import org.mozilla.javascript.ast.AstNode;

import commitminer.analysis.flow.abstractdomain.State;
import commitminer.cfg.CFGEdge;
import commitminer.cfg.CFGNode;
import commitminer.cfg.ICFGVisitor;
import commitminer.js.annotation.Annotation;
import commitminer.js.annotation.AnnotationFactBase;

/**
 * Extracts facts from a flow analysis.
 */
public class DefEnvCFGVisitor implements ICFGVisitor {

	/* The fact database we will populate. */
	private AnnotationFactBase factBase;

	public DefEnvCFGVisitor(AnnotationFactBase factBase) {
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
		if(state != null) {
			Set<Annotation> annotations = DefEnvASTVisitor.getAnnotations(state.env, node);
			factBase.registerAnnotationFacts(annotations);
		}
	}

}