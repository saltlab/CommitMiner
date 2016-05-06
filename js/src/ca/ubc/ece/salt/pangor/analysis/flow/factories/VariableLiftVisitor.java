package ca.ubc.ece.salt.pangor.analysis.flow.factories;

import java.util.LinkedList;
import java.util.List;

import org.mozilla.javascript.ast.AstNode;
import org.mozilla.javascript.ast.FunctionNode;
import org.mozilla.javascript.ast.InfixExpression;
import org.mozilla.javascript.ast.Name;
import org.mozilla.javascript.ast.NodeVisitor;
import org.mozilla.javascript.ast.ScriptNode;
import org.mozilla.javascript.ast.VariableDeclaration;
import org.mozilla.javascript.ast.VariableInitializer;

/**
 * Helper class to discover variable declarations to be lifted.
 */
public class VariableLiftVisitor implements NodeVisitor {

	/**
	 * @return The list of parameters and variables declared in the script or
	 * 		   function.
	 */
	public static List<String> getVariableDeclarations(ScriptNode script) {
		VariableLiftVisitor visitor = new VariableLiftVisitor(script);
		script.visit(visitor);
		return visitor.variableDeclarations;
	}

	/** The function or script under analysis. **/
	private ScriptNode script;

	/** The list of variables to be lifted. **/
	private List<String> variableDeclarations;

	private VariableLiftVisitor(ScriptNode script) {

		this.script = script;
		this.variableDeclarations = new LinkedList<String>();

		/* Get the parameters if this is a function. */
		if(script instanceof FunctionNode) {
			FunctionNode function = (FunctionNode) script;
			for(AstNode param : function.getParams()) {
				if(param instanceof Name){
					this.variableDeclarations.add(param.toSource());
				}
				else if(param instanceof InfixExpression) {
					InfixExpression ie = (InfixExpression) param;
					this.variableDeclarations.add(ie.getLeft().toSource());
				}
			}
		}

	}

	@Override
	public boolean visit(AstNode node) {

		/* Environment is limited to function scope. */
		if(node instanceof ScriptNode) {
			if(node != this.script) return false;
		}
		/* Add variable declarations need to be lifted. */
		else if(node instanceof VariableDeclaration) {
			VariableDeclaration vd = (VariableDeclaration)node;
			for(VariableInitializer vi : vd.getVariables()) {
				if(vi.getTarget() instanceof Name)
					this.variableDeclarations.add(vi.getTarget().toSource());
			}
		}

		return true;

	}

}