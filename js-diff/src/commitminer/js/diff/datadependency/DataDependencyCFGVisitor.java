package commitminer.js.diff.datadependency;

import java.util.Set;

import org.mozilla.javascript.ast.AstNode;

import commitminer.analysis.annotation.Annotation;
import commitminer.analysis.annotation.AnnotationFactBase;
import commitminer.analysis.flow.abstractdomain.State;
import commitminer.cfg.CFGEdge;
import commitminer.cfg.CFGNode;
import commitminer.cfg.ICFGVisitor;

/**
 * Extracts facts from a flow analysis.
 */
public class DataDependencyCFGVisitor implements ICFGVisitor {

	/* The fact database we will populate. */
	private AnnotationFactBase factBase;

	public DataDependencyCFGVisitor(AnnotationFactBase factBase) {
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
	 * value changes.
	 */
	private void visit(AstNode node, State state) {
		if(state != null) {
			Set<Annotation> annotations = DataDependencyASTVisitor.getAnnotations(state, node);
			factBase.registerAnnotationFacts(annotations);
		}
	}

}