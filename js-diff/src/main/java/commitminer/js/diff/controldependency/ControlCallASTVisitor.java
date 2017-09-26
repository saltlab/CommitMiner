package commitminer.js.diff.controldependency;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.mozilla.javascript.ast.AstNode;
import org.mozilla.javascript.ast.AstRoot;
import org.mozilla.javascript.ast.DoLoop;
import org.mozilla.javascript.ast.ForInLoop;
import org.mozilla.javascript.ast.ForLoop;
import org.mozilla.javascript.ast.FunctionCall;
import org.mozilla.javascript.ast.FunctionNode;
import org.mozilla.javascript.ast.IfStatement;
import org.mozilla.javascript.ast.NodeVisitor;
import org.mozilla.javascript.ast.TryStatement;
import org.mozilla.javascript.ast.WhileLoop;
import org.mozilla.javascript.ast.WithStatement;

import commitminer.analysis.annotation.Annotation;
import commitminer.analysis.annotation.DependencyIdentifier;
import commitminer.analysis.annotation.GenericDependencyIdentifier;
import commitminer.analysis.flow.abstractdomain.Change;
import commitminer.analysis.flow.abstractdomain.State;

public class ControlCallASTVisitor implements NodeVisitor {

	/**
	 * The set of changed variable annotations found in the statement.
	 **/
	public Set<Annotation> annotations;
	
	/** The abstract state. **/
	@SuppressWarnings("unused")
	private State state;

	/**
	 * Detects uses of the identifier.
	 * @return the set of nodes where the identifier is used.
	 */
	public static Set<Annotation> getAnnotations(State state, AstNode statement) {
		ControlCallASTVisitor visitor = new ControlCallASTVisitor(state);
		
		if(statement instanceof AstRoot) {
			/* This is the root. Nothing should be flagged. */
			return visitor.annotations;
		}
		else if(statement instanceof FunctionNode) {
			/* This is a function declaration. Nothing should be flagged. */
			return visitor.annotations;
		}
		else if(statement != null){
			/* Recursively look for modfied function calls to register as 
			 * CONDEF-DEFs. */
			statement.visit(visitor);
		}

		return visitor.annotations;
	}

	public ControlCallASTVisitor(State state) {
		this.annotations = new HashSet<Annotation>();
		this.state = state;
	}

	@Override
	public boolean visit(AstNode node) {

		/* Register CONDEF-DEF annotations for function calls. */
		if(node instanceof FunctionCall) {
			FunctionCall fc = (FunctionCall) node;
			
			if(Change.convU(fc).le == Change.LatticeElement.CHANGED) {
				if(fc.getID() != null) { // Don't register annotations for abstract nodes
					List<DependencyIdentifier> ids = new LinkedList<DependencyIdentifier>();
					ids.add(new GenericDependencyIdentifier(fc.getID()));
					annotations.add(new Annotation("CONDEP-DEF", ids, fc.getTarget().getLineno(), fc.getTarget().getFixedPosition(), fc.getTarget().getLength()));
				}
			}
		}
		/* Ignore the body of loops, ifs and functions. */
		else if(node instanceof IfStatement) {
			IfStatement ifStatement = (IfStatement) node;
			ifStatement.getCondition().visit(this);
			return false;
		}
		else if(node instanceof WhileLoop) {
			WhileLoop whileLoop = (WhileLoop) node;
			whileLoop.getCondition().visit(this);
			return false;
		}
		else if(node instanceof ForLoop) {
			ForLoop loop = (ForLoop) node;
			loop.getCondition().visit(this);
			loop.getInitializer().visit(this);
			loop.getIncrement().visit(this);
			return false;
		}
		else if(node instanceof ForInLoop) {
			ForInLoop loop = (ForInLoop) node;
			loop.getIteratedObject().visit(this);
			loop.getIterator().visit(this);
			return false;
		}
		else if(node instanceof DoLoop) {
			DoLoop loop = (DoLoop) node;
			loop.getCondition().visit(this);
			return false;
		}
		else if(node instanceof WithStatement) {
			WithStatement with = (WithStatement) node;
			with.getExpression().visit(this);
			return false;
		}
		else if(node instanceof FunctionNode) {
			return false;
		}
		else if(node instanceof TryStatement) {
			return false;
		}

		return true;

	}

}