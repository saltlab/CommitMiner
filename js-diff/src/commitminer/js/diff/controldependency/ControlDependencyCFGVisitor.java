package commitminer.js.diff.controldependency;

import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.mozilla.javascript.ast.AstNode;
import org.mozilla.javascript.ast.FunctionNode;

import commitminer.analysis.annotation.DependencyIdentifier;
import commitminer.analysis.flow.abstractdomain.State;
import commitminer.cfg.CFGEdge;
import commitminer.cfg.CFGNode;
import commitminer.cfg.ICFGVisitor;
import commitminer.js.annotation.Annotation;
import commitminer.js.annotation.AnnotationFactBase;

/**
 * Extracts facts from a flow analysis.
 */
public class ControlDependencyCFGVisitor implements ICFGVisitor {

	/* The fact database we will populate. */
	private AnnotationFactBase factBase;

	public ControlDependencyCFGVisitor(AnnotationFactBase factBase) {
		this.factBase = factBase;
	}

	@Override
	public void visit(CFGNode node) {
		visit((AstNode) node.getStatement(), (State)node.getBeforeState());
		visitStatement((AstNode) node.getStatement(), (State)node.getBeforeState());
	}

	@Override
	public void visit(CFGEdge edge) {
		visit((AstNode) edge.getCondition(), (State)edge.getBeforeState());
		visitCondition((AstNode) edge.getCondition(), (State)edge.getBeforeState());
	}

	/**
	 * Visit an AstNode (a statement or condition) and extract facts about
	 * control-call changes.
	 */
	private void visit(AstNode node, State state) {
		if(state != null && node instanceof FunctionNode) {
			/* Register CONDEP-USE annotations for control dependent statements. */
			if(state.control.getDependency().isChanged() 
					&& node.isStatement() 
					&& node.getLineno() > 0) {
				List<DependencyIdentifier> ids = new LinkedList<DependencyIdentifier>();
				ids.add(state.control.getDependency());
				factBase.registerAnnotationFact(new Annotation("CONDEF-USE", ids, node.getLineno(), node.getFixedPosition(), node.getLength()));
			}
		}
		if(state != null) {
			Set<Annotation> annotations = ControlCallASTVisitor.getAnnotations(state, node);
			factBase.registerAnnotationFacts(annotations);
		}
	}

	/**
	 * Visit an AstNode (a statement) and extract facts about control-condition
	 * changes.
	 */
	private void visitStatement(AstNode node, State state) {
		if(state != null) {
			Set<Annotation> annotations = ControlConditionASTVisitor.getDefAnnotations(state, node);
			factBase.registerAnnotationFacts(annotations);
		}
	}
	
	/**
	 * Visit an AstNode (a condition) and extract facts about control-condition
	 * changes.
	 */
	private void visitCondition(AstNode node, State state) {
		if(state != null) {
			Set<Annotation> annotations = ControlConditionASTVisitor.getUseAnnotations(state, node);
			factBase.registerAnnotationFacts(annotations);
		}
	}

}