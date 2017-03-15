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
import org.mozilla.javascript.ast.FunctionCall;
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
import ca.ubc.ece.salt.pangor.analysis.flow.abstractdomain.InternalFunctionProperties;
import ca.ubc.ece.salt.pangor.analysis.flow.abstractdomain.Obj;
import ca.ubc.ece.salt.pangor.analysis.flow.abstractdomain.State;
import ca.ubc.ece.salt.pangor.analysis.flow.abstractdomain.Store;

public class UseASTVisitor implements NodeVisitor {
	
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
	public static Set<DefUseAnnotation> getUseAnnotations(State state, AstNode statement) {

		UseASTVisitor visitor = new UseASTVisitor(state, statement);
		
		if(statement instanceof AstRoot) return visitor.annotations; // Don't visit root
		else statement.visit(visitor);

		return visitor.annotations;

	}

	public UseASTVisitor(State state, AstNode statement) {
		this.state = state;
		this.environment = state.env;
		this.store = state.store;
		this.annotations = new HashSet<DefUseAnnotation>();
	}

	@Override
	public boolean visit(AstNode node) {

		/* Only register uses of function calls for now. */
		if(node instanceof FunctionCall) {
			
			AstNode target = ((FunctionCall) node).getTarget();
			
			Set<Address> addresses = state.resolveOrCreate(target);
			
			for(Address address : addresses) {
				BValue val = store.apply(address);
				if(val == null) continue;
				for(Address objAddr : val.addressAD.addresses) {
					Obj obj = store.getObj(objAddr);
					
					if(obj.internalProperties instanceof InternalFunctionProperties) { 
						this.annotations.add(new DefUseAnnotation(
								objAddr, 
								target.getAbsolutePosition(), 
								target.toSource().length()));
					}
				}
			}

		}
		/* Ignore the body of loops, ifs and functions. */
		else if(node instanceof FunctionNode) {
			return false;
		}
		else if(node instanceof IfStatement) {
			IfStatement ifStatement = (IfStatement) node;
			ifStatement.getCondition().visit(this);
			return false;
		}
		else if(node instanceof WhileLoop) {
			WhileLoop whileLoop = (WhileLoop) node;
			whileLoop.getCondition().visit(this);
			return false;
		}
		else if(node instanceof ForLoop) {
			ForLoop loop = (ForLoop) node;
			loop.getCondition().visit(this);
			loop.getInitializer().visit(this);
			loop.getIncrement().visit(this);
			return false;
		}
		else if(node instanceof ForInLoop) {
			ForInLoop loop = (ForInLoop) node;
			loop.getIteratedObject().visit(this);
			loop.getIterator().visit(this);
			return false;
		}
		else if(node instanceof DoLoop) {
			DoLoop loop = (DoLoop) node;
			loop.getCondition().visit(this);
			return false;
		}
		else if(node instanceof WithStatement) {
			WithStatement with = (WithStatement) node;
			with.getExpression().visit(this);
			return false;
		}
		else if(node instanceof TryStatement || node instanceof FunctionNode) {
			return false;
		}
		
		return true;

	}
	
}