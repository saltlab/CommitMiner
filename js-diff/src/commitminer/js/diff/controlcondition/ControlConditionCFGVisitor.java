package commitminer.js.diff.controlcondition;

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
public class ControlConditionCFGVisitor implements ICFGVisitor {

	/* The fact database we will populate. */
	private AnnotationFactBase factBase;

	public ControlConditionCFGVisitor(AnnotationFactBase factBase) {
		this.factBase = factBase;
	}

	@Override
	public void visit(CFGNode node) {
		visitStatement((AstNode) node.getStatement(), (State)node.getBeforeState());
	}

	@Override
	public void visit(CFGEdge edge) {
		visitCondition((AstNode) edge.getCondition(), (State)edge.getBeforeState());

	}

	/**
	 * Visit an AstNode (a statement or condition) and extract facts about
	 * control-call changes.
	 */
	private void visitStatement(AstNode node, State state) {
		if(state != null) {
			Set<Annotation> annotations = ControlConditionASTVisitor.getDefAnnotations(state, node);
			factBase.registerAnnotationFacts(annotations);
		}
	}
	
	private void visitCondition(AstNode node, State state) {
		if(state != null) {
			Set<Annotation> annotations = ControlConditionASTVisitor.getUseAnnotations(state, node);
			factBase.registerAnnotationFacts(annotations);
		}
	}

}