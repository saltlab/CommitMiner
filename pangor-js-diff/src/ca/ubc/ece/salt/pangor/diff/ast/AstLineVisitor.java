package ca.ubc.ece.salt.pangor.diff.ast;

import java.util.HashSet;
import java.util.Set;

import org.mozilla.javascript.ast.AstNode;
import org.mozilla.javascript.ast.DoLoop;
import org.mozilla.javascript.ast.ForInLoop;
import org.mozilla.javascript.ast.ForLoop;
import org.mozilla.javascript.ast.FunctionNode;
import org.mozilla.javascript.ast.IfStatement;
import org.mozilla.javascript.ast.NodeVisitor;
import org.mozilla.javascript.ast.TryStatement;
import org.mozilla.javascript.ast.WhileLoop;
import org.mozilla.javascript.ast.WithStatement;

import ca.ubc.ece.salt.gumtree.ast.ClassifiedASTNode.ChangeType;

public class AstLineVisitor implements NodeVisitor {

	/** Stores the line numbers the statement covers, since statements may
	 * span multiple lines. **/
	private Set<Integer> lines;

	/**
	 * Get the line numbers of lines over which the statement spans.
	 *
	 * Blocks and scopes, which themselves contain statements, are not
	 * included, as they are contained in separate nodes in the CFG.
	 * @return The serialized set of lines the statement spans.
	 */
	public static String getStatementLines(AstNode statement) {
		AstLineVisitor visitor = new AstLineVisitor();
		statement.visit(visitor);
		String serial = "{";
		for(Integer line : visitor.lines) {
			serial += line + ",";
		}
		if(serial.length() > 1) serial = serial.substring(0, serial.length() - 1);
		serial += "}";

		return serial;
	}

	public AstLineVisitor() {
		this.lines = new HashSet<Integer>();
	}

	@Override
	public boolean visit(AstNode node) {

		/* Add lines spanned by the statement which have been modified. */
		if(node.getChangeType() != ChangeType.UNCHANGED
				|| node.getChangeType() != ChangeType.UNKNOWN) {
			this.lines.add(node.getLineno());
		}

		/* Ignore the body of loops, ifs and functions. */
		if(node instanceof IfStatement) {
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
