package ca.ubc.ece.salt.pangor.analysis;

import java.util.List;

import ca.ubc.ece.salt.gumtree.ast.ClassifiedASTNode;
import ca.ubc.ece.salt.pangor.batch.Commit;
import ca.ubc.ece.salt.pangor.cfg.CFG;

/**
 * Gathers facts (patterns, pre-conditions and anti-patterns) about changes to
 * a source code file.
 */
public abstract class SourceCodeFileAnalysis {

	/** The commit that this source code file was changed in. **/
	protected Commit commit;

	public SourceCodeFileAnalysis(Commit commit) {
		this.commit = commit;
	}

	/**
	 * Perform a single-file analysis.
	 * @param root The script.
	 * @param cfgs The list of CFGs in the script (one for each function plus
	 * 			   one for the script).
	 */
	public abstract void analyze(ClassifiedASTNode root, List<CFG> cfgs) throws Exception;

	/**
	 * Add a pattern to the analysis facts for the commit.
	 * @param pattern The pattern found in the commit.
	 */
	protected void addPattern(Pattern pattern) {
		this.commit.getPatterns().add(pattern);
	}

	/**
	 * Add an anti-pattern to the analysis facts for the commit.
	 * @param antipattern The anti-pattern found in the commit.
	 */
	protected void addAntiPatter(AntiPattern antipattern) {
		this.commit.getAntiPatterns().add(antipattern);
	}

	/**
	 * Add a pre-condition to the analysis facts for the commit.
	 * @param precondition The pre-condition found in the commit.
	 */
	protected void addPreCondition(PreCondition precondition) {
		this.commit.getPreConditions().add(precondition);
	}

}