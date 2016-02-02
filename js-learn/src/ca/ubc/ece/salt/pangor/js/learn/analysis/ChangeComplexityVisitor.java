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
import org.mozilla.javascript.ast.SwitchStatement;
import org.mozilla.javascript.ast.ThrowStatement;
import org.mozilla.javascript.ast.TryStatement;
import org.mozilla.javascript.ast.VariableDeclaration;
import org.mozilla.javascript.ast.WhileLoop;
import org.mozilla.javascript.ast.WithStatement;

import ca.ubc.ece.salt.gumtree.ast.ClassifiedASTNode.ChangeType;

/**
 * Computes the change complexity score for a script. The change complexity
 * score is the number of statements that have been modified (i.e., inserted,
 * removed or updated). Moved statements are not considered modified.
 */
public class ChangeComplexityVisitor implements NodeVisitor {

	private ChangeComplexity changeComplexity;

	/** True if this is the destination file analysis. **/
	private boolean dst;

	public static ChangeComplexity getChangeComplexity(AstRoot root, boolean dst) {

		ChangeComplexityVisitor visitor = new ChangeComplexityVisitor(dst);
		root.visit(visitor);
		return visitor.changeComplexity;

	}

	public ChangeComplexityVisitor(boolean dst) {
		this.changeComplexity = new ChangeComplexity();
		this.dst = dst;
	}

	@Override
	public boolean visit(AstNode node) {

		/* If a statement has been inserted, removed or updated, increment
		 * the complexity. */
		if((node instanceof VariableDeclaration && node.isStatement() == true) ||
				node instanceof ExpressionStatement ||
				node instanceof ReturnStatement ||
				node instanceof BreakStatement ||
				node instanceof ContinueStatement ||
				node instanceof ThrowStatement ||
				node instanceof IfStatement ||
				node instanceof WithStatement ||
				node instanceof TryStatement ||
				node instanceof CatchClause ||
				node instanceof SwitchStatement ||
				node instanceof DoLoop ||
				node instanceof ForInLoop ||
				node instanceof ForLoop ||
				node instanceof WhileLoop ||
				node instanceof FunctionNode) {
			changeTypeModified(node);
		}

		return true;

	}

	/**
	 * Checks if a statement has changes (i.e., is inserted, removed or updated).
	 * @param node The statement
	 * @return
	 */
	private void changeTypeModified(AstNode node) {
		switch(node.getChangeType()) {
		case INSERTED:
			this.changeComplexity.insertedStatements++;
			return;
		case REMOVED:
			this.changeComplexity.removedStatements++;
			return;
		case UPDATED:
			ChangeType mappedChangeType = node.getMapping().getChangeType();
			if(mappedChangeType == ChangeType.UPDATED) {
				/* The update happened in both files. */
				this.changeComplexity.updatedStatements++;
			}
			else if(this.dst) {
				/* The update only happend in the destination file. */
				this.changeComplexity.insertedStatements++;
			}
			else {
				/* The update only happened in the source file. */
				this.changeComplexity.removedStatements++;
			}
			return;
		case MOVED:
		case UNCHANGED:
		case UNKNOWN:
		default:
			return;
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