package ca.ubc.ece.salt.pangor.analysis;

import java.util.List;

import ca.ubc.ece.salt.gumtree.ast.ClassifiedASTNode;
import ca.ubc.ece.salt.pangor.cfg.CFG;

/**
 * Gathers facts (patterns, pre-conditions and anti-patterns) about changes to
 * a source code file.
 */
public abstract class SourceCodeFileAnalysis<A extends Alert> {

	public SourceCodeFileAnalysis() { }

	/**
	 * Perform a single-file analysis.
	 * @param facts Stores the facts from this analysis.
	 * @param root The script.
	 * @param cfgs The list of CFGs in the script (one for each function plus
	 * 			   one for the script).
	 */
	public abstract void analyze(Facts<A> facts, ClassifiedASTNode root, List<CFG> cfgs) throws Exception;

}