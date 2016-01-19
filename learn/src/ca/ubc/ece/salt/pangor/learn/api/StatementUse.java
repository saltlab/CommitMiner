package ca.ubc.ece.salt.pangor.learn.api;

import ca.ubc.ece.salt.gumtree.ast.ClassifiedASTNode.ChangeType;

/**
 * Stores a statement and how it was changed by the commit.
 */
public class StatementUse {

	/**
	 * The type of the statement. Should be a string representation of the AST
	 * statement node.
	 */
	public String type;

	/** How this keyword was modified from the source to the destination file. **/
	public ChangeType changeType;

	public StatementUse(String type, ChangeType changeType) {
		this.type = type;
		this.changeType = changeType;
	}

	@Override
	public String toString() {
		return "STATEMENT:" + type + ":" + changeType.name();
	}

}