package ca.ubc.ece.salt.pangor.java.analysis;

import java.util.List;
import java.util.Map;

import org.deri.iris.api.basics.IPredicate;
import org.deri.iris.storage.IRelation;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.MethodDeclaration;

import ca.ubc.ece.salt.gumtree.ast.ClassifiedASTNode;
import ca.ubc.ece.salt.pangor.analysis.SourceCodeFileAnalysis;
import ca.ubc.ece.salt.pangor.analysis.SourceCodeFileChange;
import ca.ubc.ece.salt.pangor.cfg.CFG;

/**
 * An analysis of a Java Class.
 *
 * An analysis of each of the methods in the class will be triggered from here.
 * This class can also be extended to analyze member variables, the class
 * signature and structural aspects of the class.
 *
 * NOTES:
 * 	1. This class only works with the Eclipse JDT AST.
 * 	2. This class is thread-safe.
 */
public class ClassAnalysis extends SourceCodeFileAnalysis {

	/** The analysis that inspects individual methods (and their CFGs). **/
	protected List<MethodAnalysis> methodAnalyses;

	public ClassAnalysis(List<MethodAnalysis> methodAnalyses) {
		this.methodAnalyses = methodAnalyses;
	}

	@Override
	public void analyze(SourceCodeFileChange sourceCodeFileChange,
						Map<IPredicate, IRelation> facts,
						ClassifiedASTNode root,
						List<CFG> cfgs) throws Exception {

		/* Check we are working with the correct AST type. */
		if(!(root instanceof CompilationUnit)) throw new IllegalArgumentException("The AST must be parsed from Eclipse JDT.");
		CompilationUnit subjectClass = (CompilationUnit) root;

		/* Analyze each of the methods in this class. */
		List<MethodDeclaration> methodDeclarations = MethodVisitor.getMethodDeclarations(subjectClass);
		for(MethodDeclaration methodDeclaration : methodDeclarations) {
			for(MethodAnalysis methodAnalysis : this.methodAnalyses) {
				methodAnalysis.analyze(sourceCodeFileChange,
									   subjectClass, facts, methodDeclaration,
									   getFunctionCFG(methodDeclaration, cfgs));
			}
		}

	}

	/**
	 * Find the CFG for the method declaration.
	 * @param node a {@code MethodDeclaration} node.
	 * @return the CFG for the script or function.
	 */
	private CFG getFunctionCFG(ClassifiedASTNode node, List<CFG> cfgs) {

		for(CFG cfg : cfgs) {
			if(cfg.getEntryNode().getStatement() == node) return cfg;
		}

		return null;

	}

}
