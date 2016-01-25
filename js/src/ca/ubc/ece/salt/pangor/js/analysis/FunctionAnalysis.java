package ca.ubc.ece.salt.pangor.js.analysis;

import java.util.Map;

import org.deri.iris.api.basics.IPredicate;
import org.deri.iris.storage.IRelation;
import org.mozilla.javascript.ast.AstNode;

import ca.ubc.ece.salt.pangor.analysis.SourceCodeFileChange;
import ca.ubc.ece.salt.pangor.cfg.CFG;
import ca.ubc.ece.salt.pangor.js.analysis.scope.Scope;
import ca.ubc.ece.salt.pangor.pointsto.PointsToPrediction;

/**
 * An analysis of a JavaScript function.
 *
 * NOTES:
 * 	1. This class only works with the Rhino AST.
 * 	2. This class is thread-safe.
 */
public abstract class FunctionAnalysis {

	/**
	 * An analysis of a JavaScript function. The concrete analysis of the function is
	 * triggered from here.
	 * @param sourceCodeFileChange The source code file that this class was parsed from.
	 * @param facts The analysis facts. Register patterns with this structure.
	 * @param cfg The current control flow graph for the method being analyzed.
	 * @param scope The function declaration's AST node.
	 */
	public abstract void analyze(SourceCodeFileChange sourceCodeFileChange,
						Map<IPredicate, IRelation> facts,
						CFG cfg, Scope<AstNode> scope,
						PointsToPrediction model) throws Exception;

}