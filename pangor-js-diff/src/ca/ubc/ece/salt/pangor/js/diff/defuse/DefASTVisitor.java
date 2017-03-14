package ca.ubc.ece.salt.pangor.js.diff.defuse;

import java.util.HashSet;
import java.util.Set;

import org.mozilla.javascript.Token;
import org.mozilla.javascript.ast.Assignment;
import org.mozilla.javascript.ast.AstNode;
import org.mozilla.javascript.ast.AstRoot;
import org.mozilla.javascript.ast.DoLoop;
import org.mozilla.javascript.ast.ForInLoop;
import org.mozilla.javascript.ast.ForLoop;
import org.mozilla.javascript.ast.FunctionNode;
import org.mozilla.javascript.ast.IfStatement;
import org.mozilla.javascript.ast.InfixExpression;
import org.mozilla.javascript.ast.Name;
import org.mozilla.javascript.ast.NodeVisitor;
import org.mozilla.javascript.ast.ObjectLiteral;
import org.mozilla.javascript.ast.TryStatement;
import org.mozilla.javascript.ast.WhileLoop;
import org.mozilla.javascript.ast.WithStatement;

import ca.ubc.ece.salt.pangor.analysis.flow.abstractdomain.Address;
import ca.ubc.ece.salt.pangor.analysis.flow.abstractdomain.Environment;
import ca.ubc.ece.salt.pangor.analysis.flow.abstractdomain.Identifier;
import ca.ubc.ece.salt.pangor.analysis.flow.abstractdomain.Store;

public class DefASTVisitor implements NodeVisitor {

	/**
	 * The abstract environment to use while looking for defs/uses.
	 */
	private Environment environment;
	
	/**
	 * The abstract store to use while looking for defs/uses.
	 */
	private Store store;

	/**
	 * The set of uses found in the statement, identified by line number,
	 * absolute position and string length.
	 **/
	private Set<DefUseAnnotation> annotations;
	
	/**
	 * Detects uses of the identifier.
	 * @return the set of nodes where the identifier is used.
	 */
	public static Set<DefUseAnnotation> isUsed(Environment environment, Store store, AstNode statement) {

		DefASTVisitor visitor = new DefASTVisitor(environment, store, statement);
		
		if(statement instanceof AstRoot) {
			/* This is the root. Nothing should be flagged. */
			return visitor.annotations;
		}
		else if(statement instanceof FunctionNode) {

			/* This is a function declaration, so only check the function name. */
			FunctionNode function = (FunctionNode) statement;
			Name name = function.getFunctionName();
			if(name != null) name.visit(visitor);

		}
		else {
			statement.visit(visitor);
		}

		return visitor.annotations;

	}

	public DefASTVisitor(Environment environment, Store store, AstNode statement) {
		this.environment = environment;
		this.store = store;
		this.annotations = new HashSet<DefUseAnnotation>();
	}

	@Override
	public boolean visit(AstNode node) {
		
		if(node instanceof FunctionNode) {
			
			/* If this is a named function, we need to register this function as a
			* definition. */
			FunctionNode function = (FunctionNode)node;
			if(function.getFunctionName() != null) {
				Address address = environment.apply(new Identifier(function.getName()));
				if(address != null) {
					annotations.add(new DefUseAnnotation(address, 
							function.getFunctionName().getAbsolutePosition(), 
							function.getFunctionName().toSource().length()));
				}
			}
			
		}
		
		if(node instanceof Assignment) {

			/* If the right hand side is a function literal or object literal,
			 * we need to register this assignment as a definition. */
			Assignment assignment = (Assignment)node;
			if(assignment.getRight() instanceof FunctionNode) {
				/* TODO: Resolve LHS to Address */
			}
			else if(assignment.getRight() instanceof ObjectLiteral) {
				/* TODO: Resolve LHS to Address. */
				/* TODO: Recursively visit the object literal to label objects defined
				* within. */
			}

		}

		return true;

	}

}