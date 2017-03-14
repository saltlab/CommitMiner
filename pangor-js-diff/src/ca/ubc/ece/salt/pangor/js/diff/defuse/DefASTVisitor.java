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
import org.mozilla.javascript.ast.ObjectProperty;
import org.mozilla.javascript.ast.TryStatement;
import org.mozilla.javascript.ast.VariableInitializer;
import org.mozilla.javascript.ast.WhileLoop;
import org.mozilla.javascript.ast.WithStatement;

import ca.ubc.ece.salt.pangor.analysis.flow.abstractdomain.Address;
import ca.ubc.ece.salt.pangor.analysis.flow.abstractdomain.BValue;
import ca.ubc.ece.salt.pangor.analysis.flow.abstractdomain.Environment;
import ca.ubc.ece.salt.pangor.analysis.flow.abstractdomain.Identifier;
import ca.ubc.ece.salt.pangor.analysis.flow.abstractdomain.Obj;
import ca.ubc.ece.salt.pangor.analysis.flow.abstractdomain.State;
import ca.ubc.ece.salt.pangor.analysis.flow.abstractdomain.Store;

public class DefASTVisitor implements NodeVisitor {
	
	/**
	 * The abstract state to use while looking for defs/uses.
	 */
	private State state;

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
	public static Set<DefUseAnnotation> getDefAnnotations(State state, AstNode statement) {

		DefASTVisitor visitor = new DefASTVisitor(state, statement);
		
		if(statement instanceof AstRoot) return visitor.annotations; // Don't visit root
		else statement.visit(visitor);

		return visitor.annotations;

	}

	public DefASTVisitor(State state, AstNode statement) {
		this.state = state;
		this.environment = state.env;
		this.store = state.store;
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
				FunctionNode function = (FunctionNode) assignment.getRight();
				Set<Address> addresses = state.resolveOrCreate(assignment.getLeft());
				for(Address address : addresses) {
					annotations.add(new DefUseAnnotation(address, 
							function.getAbsolutePosition(), 
							"function".length()));
				}
			}
			else if(assignment.getRight() instanceof ObjectLiteral) {
				for(Address address : state.resolveOrCreate(assignment.getLeft())) {
					visitObjectLiteral(store.getObj(address), (ObjectLiteral)assignment.getRight());
				}
			}

		}
		
		if(node instanceof VariableInitializer) {
			
			/* If the right hand side is a function literal or object literal,
			 * we need to register this assignment as a definition. */
			VariableInitializer assignment = (VariableInitializer)node;
			if(assignment.getInitializer() instanceof FunctionNode) {
				FunctionNode function = (FunctionNode) assignment.getInitializer();
				Set<Address> addresses = state.resolveOrCreate(assignment.getTarget());
				for(Address address : addresses) {
					annotations.add(new DefUseAnnotation(address, 
							function.getAbsolutePosition(), 
							"function".length()));
				}
			}
			else if(assignment.getInitializer() instanceof ObjectLiteral) {
				for(Address address : state.resolveOrCreate(assignment.getTarget())) {
					
//					visitObjectLiteral(store.getObj(address), (ObjectLiteral)assignment.getInitializer());
				}
			}
		}

		return true;

	}
	
	/**
	 * Look for function declarations in the object literal.
	 * @param obj
	 * @param ol
	 */
	private void visitObjectLiteral(Obj obj, ObjectLiteral ol) {
		
		for(ObjectProperty op : ol.getElements()) {
			if(op.getRight() instanceof FunctionNode) {
				FunctionNode function = (FunctionNode)op.getRight();
				BValue val = store.apply(obj.apply(op.getLeft().toSource()));
				for(Address address : val.addressAD.addresses) {
					annotations.add(new DefUseAnnotation(address, 
							function.getAbsolutePosition(), 
							"function".length()));
				}
			}
			if(op.getRight() instanceof ObjectLiteral) {
				BValue val = store.apply(obj.apply(op.getLeft().toSource()));
				for(Address address : val.addressAD.addresses) {
					visitObjectLiteral(store.getObj(address), (ObjectLiteral)op.getRight());
				}
			}
		}
		
	}

}