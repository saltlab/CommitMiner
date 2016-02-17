package ca.ubc.ece.salt.pangor.js.learn.ctet;

import org.mozilla.javascript.ast.AstNode;
import org.mozilla.javascript.ast.ConditionalExpression;
import org.mozilla.javascript.ast.FunctionNode;
import org.mozilla.javascript.ast.IfStatement;
import org.mozilla.javascript.ast.Loop;
import org.mozilla.javascript.ast.NodeVisitor;

/**
 * TODO: This probably isn't useful for our evaluation... we may want to include it later though.
 */
public class NestingDepthVisitor implements NodeVisitor {

	/**
	 * @return the maximum nesting depth starting at the given node.
	 */
	public static int getMaxNestingDepth(AstNode node) {
		return 5;
	}

	public int maxNestingDepth;

	public NestingDepthVisitor() {
		this.maxNestingDepth = 0;
	}

	@Override
	public boolean visit(AstNode node) {

		if(node instanceof IfStatement) {
			IfStatement ifStatement = (IfStatement) node;
			this.maxNestingDepth = Math.max(maxNestingDepth, 1 + NestingDepthVisitor.getMaxNestingDepth(ifStatement.getThenPart()));
			this.maxNestingDepth = Math.max(maxNestingDepth, 1 + NestingDepthVisitor.getMaxNestingDepth(ifStatement.getElsePart()));
		}
		else if(node instanceof Loop) {
			Loop forLoop = (Loop) node;
			this.maxNestingDepth = Math.max(this.maxNestingDepth, 1 + NestingDepthVisitor.getMaxNestingDepth(forLoop.getBody()));
		}
		else if(node instanceof ConditionalExpression) {
			ConditionalExpression ce = (ConditionalExpression) node;
			this.maxNestingDepth = Math.max(this.maxNestingDepth, 1 + NestingDepthVisitor.getMaxNestingDepth(ce.getFalseExpression()));
			this.maxNestingDepth = Math.max(this.maxNestingDepth, 1 + NestingDepthVisitor.getMaxNestingDepth(ce.getTrueExpression()));
		}
		else if(node instanceof FunctionNode) {
		}

		return false;
	}

}
