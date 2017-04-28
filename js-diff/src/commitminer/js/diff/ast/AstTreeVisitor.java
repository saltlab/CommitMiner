package commitminer.js.diff.ast;

import java.util.LinkedList;
import java.util.List;

import org.mozilla.javascript.ast.AstNode;
import org.mozilla.javascript.ast.AstRoot;
import org.mozilla.javascript.ast.BreakStatement;
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

/**
 * A visitor for finding statement-level changes. This class is used for
 * comparing the (line-level) results of CommitMiner to the (line-level)
 * results of Gumtree.
 */
public class AstTreeVisitor implements NodeVisitor {

    private List<AstNode> modifiedStatements;

    /**
     * @param script The script to generate change facts for.
     * @return A list of modified statements.
     */
    public static List<AstNode> getModifiedStatements(AstRoot script) {
    	AstTreeVisitor visitor = new AstTreeVisitor();
    	if(script == null) return visitor.modifiedStatements;
    	script.visit(visitor);
    	return visitor.modifiedStatements;
    }

    public AstTreeVisitor() {
        this.modifiedStatements = new LinkedList<AstNode>();
    }

    @Override
	public boolean visit(AstNode node) {

    	/* We only consider statements. So simply add all statement instances
    	 * to the list except for blocks. */

		if (node instanceof IfStatement
				|| node instanceof VariableDeclaration
				|| node instanceof ExpressionStatement
				|| node instanceof WhileLoop
				|| node instanceof DoLoop
				|| node instanceof ForLoop
				|| node instanceof ForInLoop
				|| node instanceof SwitchStatement
				|| node instanceof WithStatement
				|| node instanceof TryStatement
				|| node instanceof BreakStatement
				|| node instanceof ContinueStatement
				|| node instanceof ReturnStatement
				|| node instanceof ThrowStatement
				|| node instanceof FunctionNode) {
    		this.modifiedStatements.add(node);
    	}

        return true;

    }

}
