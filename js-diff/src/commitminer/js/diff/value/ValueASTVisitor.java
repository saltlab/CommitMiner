package commitminer.js.diff.value;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.mozilla.javascript.ast.AstNode;
import org.mozilla.javascript.ast.AstRoot;
import org.mozilla.javascript.ast.DoLoop;
import org.mozilla.javascript.ast.ForInLoop;
import org.mozilla.javascript.ast.ForLoop;
import org.mozilla.javascript.ast.FunctionNode;
import org.mozilla.javascript.ast.IfStatement;
import org.mozilla.javascript.ast.Name;
import org.mozilla.javascript.ast.NodeVisitor;
import org.mozilla.javascript.ast.PropertyGet;
import org.mozilla.javascript.ast.TryStatement;
import org.mozilla.javascript.ast.WhileLoop;
import org.mozilla.javascript.ast.WithStatement;

import ca.ubc.ece.salt.gumtree.ast.ClassifiedASTNode.ChangeType;
import commitminer.analysis.annotation.DependencyIdentifier;
import commitminer.analysis.annotation.GenericDependencyIdentifier;
import commitminer.analysis.flow.abstractdomain.BValue;
import commitminer.analysis.flow.abstractdomain.Change;
import commitminer.analysis.flow.abstractdomain.Environment;
import commitminer.analysis.flow.abstractdomain.State;
import commitminer.analysis.flow.abstractdomain.Store;
import commitminer.analysis.flow.abstractdomain.Variable;
import commitminer.js.annotation.Annotation;

public class ValueASTVisitor implements NodeVisitor {

	/**
	 * The set of changed variable annotations found in the statement.
	 **/
	public Set<Annotation> annotations;

	/** The abstract environment. **/
	private Environment env;
	
	/** The abstract store. **/
	private Store store;

	/**
	 * Detects uses of the identifier.
	 * @return the set of nodes where the identifier is used.
	 */
	public static Set<Annotation> getAnnotations(State state, AstNode statement) {
		ValueASTVisitor visitor = new ValueASTVisitor(state);
		
		if(statement instanceof AstRoot) {
			/* This is the root. Nothing should be flagged. */
			return visitor.annotations;
		}
		else if(statement instanceof FunctionNode) {
			/* This is a function declaration, so only check the parameters
			 * and the function name. */

			FunctionNode function = (FunctionNode) statement;
			for(AstNode param : function.getParams()) {
				param.visit(visitor);
			}

			Name name = function.getFunctionName();
			if(name != null) name.visit(visitor);

			/* Register a value-def for the function if needed. */
			if(function.getChangeType() == ChangeType.INSERTED
					|| function.getChangeType() == ChangeType.UPDATED) {
				
				List<DependencyIdentifier> ids = new LinkedList<DependencyIdentifier>();
				ids.add(new GenericDependencyIdentifier(function.getID()));
				visitor.annotations.add(new Annotation("VAL-DEF", ids, function.getLineno(), function.getAbsolutePosition(), 8));

			}

		}
		else if(statement != null){
			statement.visit(visitor);
		}

		return visitor.annotations;
	}

	public ValueASTVisitor(State state) {
		this.annotations = new HashSet<Annotation>();
		this.env = state.env;
		this.store = state.store;
	}

	@Override
	public boolean visit(AstNode node) {

		if(node instanceof Name) {

			Variable var = env.environment.get(node.toSource());
			if(var != null) {
				
				BValue val = store.apply(var.addresses);
				if(val.change.le == Change.LatticeElement.CHANGED
						|| val.change.le == Change.LatticeElement.TOP) {

					List<DependencyIdentifier> ids = new LinkedList<DependencyIdentifier>();
					ids.add(val);

					this.annotations.add(new Annotation("VAL-USE", ids, node.getLineno(), node.getAbsolutePosition(), node.getLength()));
				}
				
			}

		}
		/* Inspect the variable part of a property access. */
		else if(node instanceof PropertyGet) {
			// TODO: Change this to look for properties to highlight.
			
			PropertyGet pg = (PropertyGet) node;
			
			/* TODO: Write a function that can resolve the variable and each
			 * property, and label each one that has changes. */
			
			pg.getLeft().visit(this);

			return false;
		}
		/* Ignore the body of loops, ifs and functions. */
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
		else if(node instanceof FunctionNode) {
			return false;
		}
		else if(node instanceof TryStatement) {
			return false;
		}

		return true;

	}

}