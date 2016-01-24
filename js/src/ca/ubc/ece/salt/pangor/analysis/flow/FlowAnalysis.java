package ca.ubc.ece.salt.pangor.analysis.flow;

import org.mozilla.javascript.ast.AstNode;
import org.mozilla.javascript.ast.ScriptNode;

import ca.ubc.ece.salt.pangor.cfg.CFGEdge;
import ca.ubc.ece.salt.pangor.cfg.CFGNode;
import ca.ubc.ece.salt.pangor.js.analysis.FunctionAnalysis;
import ca.ubc.ece.salt.pangor.js.analysis.scope.Scope;

/**
 * Performs a change-sensitive, intra-procedural analysis.
 *
 * This framework provides the analysis framework and the current scope.
 *
 * Loops are executed once.
 *
 * NOTE: This class only works with the Mozilla Rhino AST.
 *
 * @param <LE> The lattice element type that stores the analysis information.
 */
public abstract class FlowAnalysis<LE extends AbstractLatticeElement> extends FunctionAnalysis {

	/**
	 * @param function The function under analysis.
	 * @return an initialized lattice element for the function.
	 */
	public abstract LE entryValue(ScriptNode function);

	/**
	 * Transfer the lattice element over the CFGEdge.
	 * @param edge The edge to transfer over.
	 */
	public abstract void transfer(CFGEdge edge, LE sourceLE, Scope<AstNode> scope);

	/**
	 * Transfer the lattice element over the CFGNode.
	 * @param node The node to transfer over.
	 */
	public abstract void transfer(CFGNode node, LE sourceLE, Scope<AstNode> scope);

	/**
	 * @param le The lattice element to copy.
	 * @return a deep copy of the lattice element.
	 */
	public abstract LE copy(LE le);

}
