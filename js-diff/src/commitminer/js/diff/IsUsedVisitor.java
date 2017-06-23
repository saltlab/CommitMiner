package commitminer.js.diff;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.mozilla.javascript.Token;
import org.mozilla.javascript.ast.AstNode;
import org.mozilla.javascript.ast.AstRoot;
import org.mozilla.javascript.ast.DoLoop;
import org.mozilla.javascript.ast.ForInLoop;
import org.mozilla.javascript.ast.ForLoop;
import org.mozilla.javascript.ast.FunctionNode;
import org.mozilla.javascript.ast.IfStatement;
import org.mozilla.javascript.ast.InfixExpression;
import org.mozilla.javascript.ast.Name;
import org.mozilla.javascript.ast.NodeVisitor;
import org.mozilla.javascript.ast.TryStatement;
import org.mozilla.javascript.ast.WhileLoop;
import org.mozilla.javascript.ast.WithStatement;

import commitminer.analysis.annotation.DependencyIdentifier;
import commitminer.analysis.flow.abstractdomain.Property;
import commitminer.js.annotation.Annotation;

public class IsUsedVisitor implements NodeVisitor {

	/**
	 * The set of uses found in the statement, identified by line number,
	 * absolute position and string length.
	 **/
	public Set<Annotation> annotations;

	/** The identifier to look for. **/
	private String identity;
	
	/** Are we looking for variables? **/
	private boolean variable;

	/**
	 * Detects uses of the identifier.
	 * @return the set of nodes where the identifier is used.
	 */
	public static Set<Annotation> isUsed(AstNode statement, String identity, boolean variable) {
		IsUsedVisitor visitor = new IsUsedVisitor(statement, identity, variable);
		
		if(statement instanceof AstRoot) {
			/* This is the root. Nothing should be flagged. */
			return visitor.annotations;
		}
		else if(statement instanceof FunctionNode) {
			/* This is a function declaration, so only check the parameters
			 * and the function name. */

			FunctionNode function = (FunctionNode) statement;
			for(AstNode param : function.getParams()) {
				param.visit(visitor);
			}

			Name name = function.getFunctionName();
			if(name != null) name.visit(visitor);

		}
		else {
			statement.visit(visitor);
		}

		return visitor.annotations;
	}

	public IsUsedVisitor(AstNode statement, String identity, boolean variable) {
		this.annotations = new HashSet<Annotation>();
		this.identity = identity;
		this.variable = variable;
	}

	@Override
	public boolean visit(AstNode node) {

		if(node instanceof InfixExpression || node instanceof Name) {
			if(node.toSource().equals(identity)) {

				if(!this.variable || node.getParent() != null
					&& !(node.getParent().getType()== Token.GETPROP
					|| node.getParent().getType()== Token.GETPROPNOWARN)) {

					/* This idenfier is being used. */
					List<DependencyIdentifier> placeholder = new LinkedList<DependencyIdentifier>();
					this.annotations.add(new Annotation(identity, placeholder, node.getLineno(), node.getAbsolutePosition(), node.toSource().length()));

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
		else if(node instanceof TryStatement || node instanceof FunctionNode) {
			return false;
		}

		return true;

	}

}