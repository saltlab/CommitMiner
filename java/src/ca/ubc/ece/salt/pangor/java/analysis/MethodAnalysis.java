package ca.ubc.ece.salt.pangor.java.analysis;

import org.eclipse.jdt.core.dom.MethodDeclaration;

import ca.ubc.ece.salt.gumtree.ast.ClassifiedASTNode;
import ca.ubc.ece.salt.pangor.analysis.Alert;
import ca.ubc.ece.salt.pangor.analysis.Commit;
import ca.ubc.ece.salt.pangor.analysis.Facts;
import ca.ubc.ece.salt.pangor.analysis.SourceCodeFileChange;
import ca.ubc.ece.salt.pangor.cfg.CFG;

/**
 * An analysis of a Java Method.
 *
 * NOTE: Not thread safe.
 * NOTE: This class only works with the Eclipse JDT AST.
 */
public abstract class MethodAnalysis<A extends Alert> {

	/** The commit that this class was modified in. **/
	protected Commit commit;

	/** The source code file that this class was parsed from. **/
	protected SourceCodeFileChange sourceCodeFileChange;

	/** The analysis facts. Register patterns with this structure. **/
	protected Facts<A> facts;

	/**
	 * The current control flow graph for the method being analyzed.
	 */
	protected CFG cfg;

	public MethodAnalysis() {
		this.commit = null;
		this.sourceCodeFileChange = null;
		this.facts = null;
	}

	/**
	 * An analysis of a Java method. The concrete analysis of the method is
	 * triggered from here.
	 */
	public void analyze(Commit commit, SourceCodeFileChange sourceCodeFileChange,
			Facts<A> facts, ClassifiedASTNode root, CFG cfg) throws Exception {

		this.commit = commit;
		this.sourceCodeFileChange = sourceCodeFileChange;
		this.facts = facts;

		/* Check we are working with the correct AST type. */
		if(!(root instanceof MethodDeclaration)) throw new IllegalArgumentException("The AST must be parsed from Eclipse JDT.");
		MethodDeclaration method = (MethodDeclaration) root;

		/* Analyze this method with a concrete analysis that recognizes patterns. */
		this.concreteAnalysis(method, cfg);

	}

	/**
	 * Analyzes a method with a concrete analysis that recognizes and registers
	 * patterns.
	 * @param method The method declaration's AST node.
	 * @param cfg The control flow graph for the method.
	 */
	public abstract void concreteAnalysis(MethodDeclaration method, CFG cfg);

}
