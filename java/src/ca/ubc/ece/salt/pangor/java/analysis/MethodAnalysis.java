package ca.ubc.ece.salt.pangor.java.analysis;

import java.util.List;

import org.eclipse.jdt.core.dom.MethodDeclaration;

import ca.ubc.ece.salt.gumtree.ast.ClassifiedASTNode;
import ca.ubc.ece.salt.pangor.cfg.CFG;

/**
 * An analysis of a Java Method.
 *
 * NOTE: This class only works with the Eclipse JDT AST.
 *
 */
public abstract class MethodAnalysis {

	/**
	 * The class analysis that is executing this method analysis. Use the
	 * class analysis to register facts.
	 */
	protected ClassAnalysis classAnalysis;

	/**
	 * The current control flow graph for the method being analyzed.
	 */
	protected CFG cfg;

	public MethodAnalysis(ClassAnalysis classAnalysis) {
		this.classAnalysis = classAnalysis;
	}

	public void analyze(ClassifiedASTNode root, List<CFG> cfgs) throws Exception {

		/* Check we are working with the correct AST type. */
		if(!(root instanceof MethodDeclaration)) throw new IllegalArgumentException("The AST must be parsed from Eclipse JDT.");
		MethodDeclaration method = (MethodDeclaration) root;

		/* TODO: Analyze this method in this class. */

	}

}
