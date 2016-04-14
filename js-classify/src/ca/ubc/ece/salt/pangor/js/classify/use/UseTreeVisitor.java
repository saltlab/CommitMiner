package ca.ubc.ece.salt.pangor.js.classify.use;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.mozilla.javascript.Token;
import org.mozilla.javascript.ast.ArrayLiteral;
import org.mozilla.javascript.ast.Assignment;
import org.mozilla.javascript.ast.AstNode;
import org.mozilla.javascript.ast.AstRoot;
import org.mozilla.javascript.ast.ConditionalExpression;
import org.mozilla.javascript.ast.ElementGet;
import org.mozilla.javascript.ast.FunctionCall;
import org.mozilla.javascript.ast.InfixExpression;
import org.mozilla.javascript.ast.NodeVisitor;
import org.mozilla.javascript.ast.ObjectProperty;
import org.mozilla.javascript.ast.VariableInitializer;

import ca.ubc.ece.salt.gumtree.ast.ClassifiedASTNode.ChangeType;
import ca.ubc.ece.salt.pangor.js.analysis.utilities.AnalysisUtilities;

/**
 * A visitor for finding identifier uses.
 *
 * An identifier is used if it is passed as an argument, if one one of its
 * fields or methods are accessed or if it is dereferenced in an expression.
 */
public class UseTreeVisitor implements NodeVisitor {

    private Map<AstNode, List<Pair<ChangeType,String>>> usedIdentifiers;

    /**
     * @param script The script to generate use facts for.
     * @return A map of statements and their identifier uses.
     */
    public static Map<AstNode, List<Pair<ChangeType,String>>> getUses(AstRoot script) {
    	UseTreeVisitor visitor = new UseTreeVisitor();
    	if(script == null) return visitor.usedIdentifiers;
    	script.visit(visitor);
    	return visitor.usedIdentifiers;
    }

    public UseTreeVisitor() {
        this.usedIdentifiers = new HashMap<AstNode, List<Pair<ChangeType,String>>>();
    }

    @Override
	public boolean visit(AstNode node) {

    	/* The strategy here is to find all identifier uses in the file, and
    	 * for each identifier, backtrack to get the statement. */

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

        // TODO: Track the change type. We'll need to store more than the
        //		 identifer string in the value.
        if(changeType == ChangeType.MOVED || changeType == ChangeType.UNCHANGED) {
            String identifier = AnalysisUtilities.getIdentifier(node);

            if(identifier != null) {

            	/* Get the statement associated with this use. */
            	AstNode statement = getStatement(node);

            	/* May be the first identifier used in the statement. */
            	List<Pair<ChangeType, String>> identifiers = this.usedIdentifiers.get(statement);

            	if(identifiers == null) {
            		identifiers = new LinkedList<Pair<ChangeType, String>>();
					this.usedIdentifiers.put(statement, identifiers);
            	}

            	/* Add the identifier as a use. */
            	if(identifiers.contains(identifier)) identifiers.add(Pair.of(changeType, identifier));

            }

        }
    }

    /**
     * @param node The top level node for the identifier.
     * @return The closest ancestor that is a statement.
     */
    private static AstNode getStatement(AstNode node) {
		while(!node.isStatement()) node = node.getParent();
		return node;
    }

    /**
     * Returns true if the operator represents an operation where the
     * identifiers are dereferenced.
     * @param tokenType
     * @return
     */
    private static boolean isUseOperator(int tokenType) {
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
