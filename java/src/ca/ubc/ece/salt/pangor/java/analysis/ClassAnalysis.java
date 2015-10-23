package ca.ubc.ece.salt.pangor.java.analysis;

import java.util.List;

import org.eclipse.jdt.core.dom.CompilationUnit;

import ca.ubc.ece.salt.gumtree.ast.ClassifiedASTNode;
import ca.ubc.ece.salt.pangor.analysis.SourceCodeFileAnalysis;
import ca.ubc.ece.salt.pangor.cfg.CFG;

/**
 * An analysis of a Java Class.
 *
 * An analysis of each of the methods in the class will be triggered from here.
 * This class can also be extended to analyze member variables, the class
 * signature and structural aspects of the class.
 *
 * NOTE: This class only works with the Eclipse JDT AST.
 */
public class ClassAnalysis extends SourceCodeFileAnalysis {

	protected List<CFG> cfgs;

	public ClassAnalysis() { }

	@Override
	public void analyze(ClassifiedASTNode root, List<CFG> cfgs) throws Exception {

		/* Check we are working with the correct AST type. */
		if(!(root instanceof CompilationUnit)) throw new IllegalArgumentException("The AST must be parsed from Eclipse JDT.");
		CompilationUnit subjectClass = (CompilationUnit) root;

		this.cfgs = cfgs;

		/* TODO: Analyze each of the methods in this class. */

	}

}
