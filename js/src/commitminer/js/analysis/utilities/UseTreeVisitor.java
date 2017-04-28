package commitminer.js.analysis.utilities;

import java.util.HashSet;
import java.util.Set;

import org.apache.commons.lang3.ArrayUtils;
import org.mozilla.javascript.Token;
import org.mozilla.javascript.ast.ArrayLiteral;
import org.mozilla.javascript.ast.Assignment;
import org.mozilla.javascript.ast.AstNode;
import org.mozilla.javascript.ast.ConditionalExpression;
import org.mozilla.javascript.ast.ElementGet;
import org.mozilla.javascript.ast.FunctionCall;
import org.mozilla.javascript.ast.FunctionNode;
import org.mozilla.javascript.ast.InfixExpression;
import org.mozilla.javascript.ast.NodeVisitor;
import org.mozilla.javascript.ast.ObjectProperty;
import org.mozilla.javascript.ast.VariableInitializer;

import ca.ubc.ece.salt.gumtree.ast.ClassifiedASTNode.ChangeType;

/**
 * A visitor for finding identifier uses that were either moved or unchanged.
 *
 * An identifier is used if it is passed as an argument, if one one of its
 * fields or methods are accessed or if it is dereferenced in an expression.
 * @author qhanam
 */
public class UseTreeVisitor implements NodeVisitor {

    private Set<String> usedIdentifiers;

    /**
     * @param statement The statement or block to find variable uses in.
     * @return A list of all the identifiers that are used in the statement.
     */
    public static Set<String> getSpecialTypeChecks(AstNode statement) {
    	UseTreeVisitor visitor = new UseTreeVisitor();
    	if(statement == null) return visitor.usedIdentifiers;
    	statement.visit(visitor);
    	return visitor.usedIdentifiers;
    }

    public UseTreeVisitor() {
        this.usedIdentifiers = new HashSet<String>();
    }

    /**
     * @return the list of identifiers that were used.
     */
    public Set<String> getUsedIdentifiers() {
    	return this.usedIdentifiers;
    }

    @Override
	public boolean visit(AstNode node) {

        /* Investigate the subtrees. */
    	if (node instanceof FunctionNode) {
    		/* Do not visit function declarations. */
    		return false;
    	}
        if (node instanceof Assignment || node instanceof ObjectProperty) {

            AstNode right = ((InfixExpression)node).getRight();
            this.check(right);

        }
        else if (node instanceof VariableInitializer) {

        	AstNode right = ((VariableInitializer)node).getInitializer();
        	this.check(right);

        }
        else if (node instanceof ElementGet) {

        	AstNode element = ((ElementGet)node).getElement();
        	this.check(element);

        }
        else if (node instanceof InfixExpression) {
            InfixExpression ie = (InfixExpression) node;

            /* Only check if it is a use operator (for a field or function dereference). */
            if(UseTreeVisitor.isUseOperator(ie.getOperator())) {
                AstNode left = ie.getLeft();
                this.check(left);

                if(ie.getOperator() != Token.DOT
                   && ie.getOperator() != Token.GETPROP
                   && ie.getOperator() != Token.GETPROPNOWARN)
                {
                    AstNode right = ie.getRight();
                    this.check(right);
                }
            }
        }
        else if (node instanceof FunctionCall) {

            FunctionCall call = (FunctionCall) node;
            for(AstNode argument : call.getArguments()) {
                this.check(argument);
            }
            this.check(call.getTarget());

        }
        else if (node instanceof ConditionalExpression) {

            ConditionalExpression ce = (ConditionalExpression) node;
            this.check(ce.getTrueExpression());
            this.check(ce.getFalseExpression());

        }
        else if (node instanceof ArrayLiteral) {
        	ArrayLiteral literal = (ArrayLiteral) node;
        	for(AstNode element : literal.getElements()) {
                this.check(element);
        	}
        }

        return true;
    }

    /**
     * Checks if the AstNode is an identifier that is in the list of
     * identifiers that were checked in the parent. If they match,
     * the identifier has been used, so remove it from the list of
     * checked identifiers.
     * @param node
     */
    private void check(AstNode node) {
    	if(node == null) return;

        ChangeType changeType = node.getChangeType();

        if(changeType == ChangeType.MOVED || changeType == ChangeType.UNCHANGED) {
            String identifier = AnalysisUtilities.getIdentifier(node);

            if(identifier != null) {
            	this.usedIdentifiers.add(identifier);
            }

        }
    }

    /**
     * Returns true if the operator represents an operation where the
     * identifiers are dereferenced.
     * @param tokenType
     * @return
     */
    public static boolean isUseOperator(int tokenType) {
        int[] useOperators = new int[] { Token.GETPROP, Token.GETPROPNOWARN,
        								 Token.BITOR, Token.BITXOR, Token.BITAND,
        								 Token.ADD, Token.SUB , Token.MUL,
        								 Token.DIV , Token.MOD, Token.GETELEM,
        								 Token.SETELEM, Token.ASSIGN_BITOR,
        								 Token.ASSIGN_BITXOR,
        								 Token.ASSIGN_BITAND , Token.ASSIGN_LSH,
                                         Token.ASSIGN_RSH , Token.ASSIGN_ADD,
                                         Token.ASSIGN_SUB , Token.ASSIGN_MUL,
                                         Token.ASSIGN_DIV, Token.ASSIGN_MOD,
                                         Token.DOT, Token.INC, Token.DEC };
        return ArrayUtils.contains(useOperators, tokenType);
    }

}
