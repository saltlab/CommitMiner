package commitminer.js.analysis.utilities;

import java.util.LinkedList;
import java.util.List;

import org.mozilla.javascript.Token;
import org.mozilla.javascript.ast.AstNode;
import org.mozilla.javascript.ast.InfixExpression;
import org.mozilla.javascript.ast.NodeVisitor;

/**
 * Generates a set of identifiers from an "OR" conditional expression. 
 * @author qhanam
 */
public class IdentifiersTreeVisitor implements NodeVisitor {
    
    public List<String> variableIdentifiers;
    
    public IdentifiersTreeVisitor() {
        this.variableIdentifiers = new LinkedList<String>();
    }
    
    public boolean visit(AstNode node) {

        String identifier = AnalysisUtilities.getIdentifier(node);
        if(identifier != null) {
            this.variableIdentifiers.add(identifier);
            return false;
        }

        if (node instanceof InfixExpression) {
            InfixExpression ie = (InfixExpression) node;
            
            /* If this is not a use operator, check that neither side
             * is an identifier. */
            if(ie.getOperator() == Token.OR) {
                this.visit(ie.getLeft());
                this.visit(ie.getRight());
            }
        } 
        
        return false;
    }

}
