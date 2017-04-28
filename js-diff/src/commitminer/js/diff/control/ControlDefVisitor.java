package commitminer.js.diff.control;

import java.util.HashSet;
import java.util.Set;

import org.mozilla.javascript.ast.AstNode;
import org.mozilla.javascript.ast.AstRoot;
import org.mozilla.javascript.ast.DoLoop;
import org.mozilla.javascript.ast.ForInLoop;
import org.mozilla.javascript.ast.ForLoop;
import org.mozilla.javascript.ast.FunctionCall;
import org.mozilla.javascript.ast.FunctionNode;
import org.mozilla.javascript.ast.IfStatement;
import org.mozilla.javascript.ast.InfixExpression;
import org.mozilla.javascript.ast.Name;
import org.mozilla.javascript.ast.NodeVisitor;
import org.mozilla.javascript.ast.TryStatement;
import org.mozilla.javascript.ast.WhileLoop;
import org.mozilla.javascript.ast.WithStatement;

import ca.ubc.ece.salt.gumtree.ast.ClassifiedASTNode.ChangeType;

public class ControlDefVisitor implements NodeVisitor {

	/**
	 * The set of uses found in the statement, identified by line number,
	 * absolute position and string length.
	 **/
	public Set<AstNode> matches;

	/**
	 * Detects control flow change definitions in the statement.
	 * @param statement The statement to check.
	 * @param definitions The definitions to look for.
	 * @return the set of nodes where the identifier is used.
	 */
	public static Set<AstNode> getDefs(AstNode statement) {
		ControlDefVisitor visitor = new ControlDefVisitor(statement);

		if(statement instanceof AstRoot) {
			/* This is the root. Nothing should be flagged. */
			return visitor.matches;
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

		return visitor.matches;
	}

	public ControlDefVisitor(AstNode statement) {
		this.matches = new HashSet<AstNode>();
	}

	@Override
	public boolean visit(AstNode node) {

		/* If this is an inserted function call, it is a control change def. */
		if(node instanceof FunctionCall)
			check((FunctionCall)node);

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

	/**
	 * If this node is changed, it is a control flow change def.
	 */
	private void check(FunctionCall node) {

		if(node.getChangeType() != ChangeType.UNCHANGED
				&& node.getChangeType() != ChangeType.UNKNOWN) {
			// TODO: Find the rightmost node in the target.
			AstNode target = node.getTarget();
			while(target instanceof InfixExpression) {
				InfixExpression ie = (InfixExpression)target;
				target = ie.getRight();
			}

			this.matches.add(target);
		}
	}

}
