package ca.ubc.ece.salt.pangor.js.learn.analysis;

import org.mozilla.javascript.ast.AstNode;
import org.mozilla.javascript.ast.AstRoot;
import org.mozilla.javascript.ast.BreakStatement;
import org.mozilla.javascript.ast.CatchClause;
import org.mozilla.javascript.ast.ContinueStatement;
import org.mozilla.javascript.ast.DoLoop;
import org.mozilla.javascript.ast.ExpressionStatement;
import org.mozilla.javascript.ast.ForInLoop;
import org.mozilla.javascript.ast.ForLoop;
import org.mozilla.javascript.ast.FunctionNode;
import org.mozilla.javascript.ast.IfStatement;
import org.mozilla.javascript.ast.NodeVisitor;
import org.mozilla.javascript.ast.ReturnStatement;
import org.mozilla.javascript.ast.SwitchCase;
import org.mozilla.javascript.ast.SwitchStatement;
import org.mozilla.javascript.ast.ThrowStatement;
import org.mozilla.javascript.ast.TryStatement;
import org.mozilla.javascript.ast.VariableDeclaration;
import org.mozilla.javascript.ast.WhileLoop;
import org.mozilla.javascript.ast.WithStatement;

/**
 * Computes the change complexity score for a script. The change complexity
 * score is the number of statements that have been modified (i.e., inserted,
 * removed or updated). Moved statements are not considered modified.
 */
public class ChangeComplexityVisitor implements NodeVisitor {

	private ChangeComplexity changeComplexity;

	public static ChangeComplexity getChangeComplexity(AstRoot root) {

		ChangeComplexityVisitor visitor = new ChangeComplexityVisitor();
		root.visit(visitor);
		return visitor.changeComplexity;

	}

	public ChangeComplexityVisitor() {
		this.changeComplexity = new ChangeComplexity();
	}

	@Override
	public boolean visit(AstNode node) {

		if((node instanceof VariableDeclaration && ((VariableDeclaration)node).isStatement())
				|| node instanceof ExpressionStatement
				|| node instanceof ReturnStatement
				|| node instanceof BreakStatement
				|| node instanceof ContinueStatement
				|| node instanceof ThrowStatement) {
			this.checkSubExpression(node);
		}
		else if(node instanceof IfStatement) {
			IfStatement ifStatement = (IfStatement) node;
			if(!changeTypeModified(ifStatement)) this.checkSubExpression(ifStatement.getCondition());
		}
		else if(node instanceof WithStatement) {
			WithStatement withStatement = (WithStatement) node;
			if(!changeTypeModified(withStatement)) this.checkSubExpression(withStatement.getExpression());
		}
		else if(node instanceof TryStatement) {
			changeTypeModified(node);
		}
		else if(node instanceof CatchClause) {
			CatchClause clause = (CatchClause) node;
			if(!changeTypeModified(clause)) this.checkSubExpression(clause.getCatchCondition());
		}
		else if(node instanceof SwitchStatement) {
			SwitchStatement switchStatement = (SwitchStatement) node;
			if(!changeTypeModified(switchStatement)) this.checkSubExpression(switchStatement.getExpression());
		}
		else if(node instanceof SwitchCase) {
			changeTypeModified(node);
		}
		else if(node instanceof DoLoop) {
			DoLoop doLoop = (DoLoop) node;
			if(!changeTypeModified(doLoop)) this.checkSubExpression(doLoop.getCondition());
		}
		else if(node instanceof ForInLoop) {
			ForInLoop loop = (ForInLoop) node;
			if(!changeTypeModified(loop)) this.checkSubExpression(loop);
		}
		else if(node instanceof ForLoop) {
			ForLoop loop = (ForLoop) node;
			if(!changeTypeModified(loop)) this.checkSubExpression(loop);
		}
		else if(node instanceof WhileLoop) {
			WhileLoop loop = (WhileLoop) node;
			if(!changeTypeModified(loop)) this.checkSubExpression(loop);
		}
		else if(node instanceof FunctionNode) {
			FunctionNode function = (FunctionNode) node;
			if(!changeTypeModified(function)) {
				for(AstNode param : function.getParams()) {
					if(this.checkSubExpression(param)) {
						break;
					}
				}
			}
		}


		return true;
	}

	/**
	 * Checks if a statement has changes (i.e., is inserted, removed or updated).
	 * @param node The statement
	 * @return
	 */
	private boolean changeTypeModified(AstNode node) {
		switch(node.getChangeType()) {
		case INSERTED:
			this.changeComplexity.insertedStatements++;
			return true;
		case REMOVED:
			this.changeComplexity.removedStatements++;
			return true;
		case UPDATED:
			this.changeComplexity.updatedStatements++;
			return true;
		case MOVED:
		case UNCHANGED:
		case UNKNOWN:
		default:
			return false;
		}

	}

	/**
	 * Checks if a statement has changes (i.e., is inserted, removed or updated).
	 * @param node The statement
	 * @return true if the statement was modified.
	 */
	public boolean checkSubExpression(AstNode node) {
		if(node == null) return false;
		ExpressionChangeVisitor visitor = new ExpressionChangeVisitor();
		node.visit(visitor);
		if(visitor.isModified) return true;
		return false;
	}

	/**
	 * Checks if a statement has changes (i.e., is inserted, removed or updated).
	 */
	private class ExpressionChangeVisitor implements NodeVisitor {

		private boolean isModified;

		public ExpressionChangeVisitor() {
			this.isModified = false;
		}

		@Override
		public boolean visit(AstNode node) {

			/* Do not visit FunctionNodes. */
			if(node instanceof FunctionNode) return false;

			if(changeTypeModified(node)) {
				this.isModified = true;
				return false;
			}

			return true;

		}

	}

	/**
	 * Stores the change complexity by the number of updated, inserted and
	 * removed statements.
	 */
	public class ChangeComplexity {
		public int updatedStatements;
		public int insertedStatements;
		public int removedStatements;

		public ChangeComplexity() {
			this.updatedStatements = 0;
			this.insertedStatements = 0;
			this.removedStatements = 0;
		}
	}

}