package ca.ubc.ece.salt.pangor.js.learn.analysis;

import org.mozilla.javascript.ast.AstNode;
import org.mozilla.javascript.ast.AstRoot;
import org.mozilla.javascript.ast.Block;
import org.mozilla.javascript.ast.NodeVisitor;

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
		if(node.isStatement() && !(node instanceof Block)) {
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