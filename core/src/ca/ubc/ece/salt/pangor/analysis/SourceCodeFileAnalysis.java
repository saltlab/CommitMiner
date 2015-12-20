package ca.ubc.ece.salt.pangor.analysis;

import java.util.List;
import java.util.Map;

import org.deri.iris.api.basics.IPredicate;
import org.deri.iris.storage.IRelation;

import ca.ubc.ece.salt.gumtree.ast.ClassifiedASTNode;
import ca.ubc.ece.salt.pangor.cfg.CFG;

/**
 * Gathers facts (patterns, pre-conditions and anti-patterns) about changes to
 * a source code file.
 */
public abstract class SourceCodeFileAnalysis {

	public SourceCodeFileAnalysis() { }

	/**
	 * Perform a single-file analysis.
	 * @param commit The commit information.
	 * @param sourceCodeFileChange The source code file change information.
	 * @param facts Stores the facts (predicates and their relations) from this
	 * 				analysis.
	 * @param root The script.
	 * @param cfgs The list of CFGs in the script (one for each function plus
	 * 			   one for the script).
	 */
	public abstract void analyze(Commit commit, SourceCodeFileChange sourceCodeFileChange, Map<IPredicate, IRelation> facts, ClassifiedASTNode root, List<CFG> cfgs) throws Exception;

}