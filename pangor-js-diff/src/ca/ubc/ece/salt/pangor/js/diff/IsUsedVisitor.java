package ca.ubc.ece.salt.pangor.js.diff;

import java.util.HashSet;
import java.util.Set;

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

import ca.ubc.ece.salt.pangor.analysis.flow.abstractdomain.Identifier;

public class IsUsedVisitor implements NodeVisitor {

	/**
	 * The set of uses found in the statement, identified by line number,
	 * absolute position and string length.
	 **/
	public Set<Annotation> annotations;

	/** The identifier to look for. **/
	private Identifier identity;

	/**
	 * Detects uses of the identifier.
	 * @return the set of nodes where the identifier is used.
	 */
	public static Set<Annotation> isUsed(AstNode statement, Identifier identity) {
		IsUsedVisitor visitor = new IsUsedVisitor(statement, identity);

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

	public IsUsedVisitor(AstNode statement, Identifier identity) {
		this.annotations = new HashSet<Annotation>();
		this.identity = identity;
	}

	@Override
	public boolean visit(AstNode node) {

		if(node instanceof InfixExpression || node instanceof Name) {
			if(node.toSource().equals(identity.name)) {
				/* This idenfier is being used. */
				this.annotations.add(new Annotation(node.getLineno(), node.getAbsolutePosition(), node.toSource().length()));
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