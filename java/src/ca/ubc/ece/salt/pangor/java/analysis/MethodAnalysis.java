package ca.ubc.ece.salt.pangor.java.analysis;

import java.util.Map;

import org.deri.iris.api.basics.IPredicate;
import org.deri.iris.storage.IRelation;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.MethodDeclaration;

import ca.ubc.ece.salt.gumtree.ast.ClassifiedASTNode;
import ca.ubc.ece.salt.pangor.analysis.SourceCodeFileChange;
import ca.ubc.ece.salt.pangor.cfg.CFG;

/**
 * An analysis of a Java method.
 *
 * NOTES:
 * 	1. This class only works with the Eclipse JDT AST.
 * 	2. This class is thread-safe.
 */
public abstract class MethodAnalysis {

	/**
	 * An analysis of a Java method. The concrete analysis of the method is
	 * triggered from here.
	 * @param sourceCodeFileChange The source code file that this class was parsed from.
	 * @param compilationUnit The {@code CompilationUnit}. Needed for getting line numbers and
	 * 		  the class name.
	 * @param facts The analysis facts. Register patterns with this structure.
	 * @param cfg The current control flow graph for the method being analyzed.
	 */
	public void analyze(SourceCodeFileChange sourceCodeFileChange,
						CompilationUnit compilationUnit,
						Map<IPredicate, IRelation> facts,
						ClassifiedASTNode root, CFG cfg) throws Exception {

		/* Check we are working with the correct AST type. */
		if(!(root instanceof MethodDeclaration)) throw new IllegalArgumentException("The AST must be parsed from Eclipse JDT.");
		MethodDeclaration method = (MethodDeclaration) root;

		/* Analyze this method with a concrete analysis that recognizes patterns. */
		this.concreteAnalysis(sourceCodeFileChange, compilationUnit, facts,
							  cfg, method);

	}

	/**
	 * Analyzes a method with a concrete analysis that recognizes and registers
	 * patterns.
	 * @param sourceCodeFileChange The source code file that this class was parsed from.
	 * @param The {@code CompilationUnit}. Needed for getting line numbers and
	 * 		  the class name.
	 * @param facts The analysis facts. Register patterns with this structure.
	 * @param cfg The current control flow graph for the method being analyzed.
	 * @param method The method declaration's AST node.
	 */
	protected abstract void concreteAnalysis(SourceCodeFileChange sourceCodeFileChange,
											 CompilationUnit compilationUnit,
											 Map<IPredicate, IRelation> facts,
											 CFG cfg, MethodDeclaration method);

}
