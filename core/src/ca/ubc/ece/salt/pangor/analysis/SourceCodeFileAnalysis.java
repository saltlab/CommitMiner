package ca.ubc.ece.salt.pangor.analysis;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import ca.ubc.ece.salt.gumtree.ast.ClassifiedASTNode;
import ca.ubc.ece.salt.pangor.cfg.CFG;

/**
 * Gathers facts (patterns, pre-conditions and anti-patterns) about changes to
 * a source code file.
 */
public abstract class SourceCodeFileAnalysis<A extends Alert> {

	/** Pre-conditions found by the analysis. **/
	private Set<PreCondition> preConditions;

	/** Anti-patterns found by the analysis. **/
	private Set<AntiPattern> antiPatterns;

	/** Patterns found by the analysis. **/
	private List<Pattern<A>> patterns;

	public SourceCodeFileAnalysis() {
		this.preConditions = new HashSet<PreCondition>();
		this.antiPatterns = new HashSet<AntiPattern>();
		this.patterns = new LinkedList<Pattern<A>>();
	}

	/**
	 * Perform a single-file analysis.
	 * @param root The script.
	 * @param cfgs The list of CFGs in the script (one for each function plus
	 * 			   one for the script).
	 */
	public abstract void analyze(ClassifiedASTNode root, List<CFG> cfgs) throws Exception;

	/**
	 * Adds a pre-condition fact.
	 * @param preCondition The pre-condition.
	 */
	protected void addPreCondition(PreCondition preCondition) {
		this.preConditions.add(preCondition);
	}

	/**
	 * @return The set of pre-condition facts generated during the analysis.
	 */
	public Set<PreCondition> getPreConditions() {
		return this.preConditions;
	}

	/**
	 * Adds an anti-pattern fact.
	 * @param antiPattern The anti-pattern.
	 */
	protected void addAntiPattern(AntiPattern antiPattern) {
		this.antiPatterns.add(antiPattern);
	}

	/**
	 * @return The set of anti-pattern facts generated during the analysis.
	 */
	public Set<AntiPattern> getAntiPatterns() {
		return this.antiPatterns;
	}

	/**
	 * Adds a pattern fact.
	 * @param pattern The pattern.
	 */
	protected void addPattern(Pattern<A> pattern) {
		this.patterns.add(pattern);
	}

	/**
	 * @return The set of pattern facts generated during the analysis.
	 */
	public List<Pattern<A>> getPatterns() {
		return this.patterns;
	}

}