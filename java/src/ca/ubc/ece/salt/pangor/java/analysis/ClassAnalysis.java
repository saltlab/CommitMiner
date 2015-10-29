package ca.ubc.ece.salt.pangor.java.analysis;

import java.util.List;

import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.MethodDeclaration;

import ca.ubc.ece.salt.gumtree.ast.ClassifiedASTNode;
import ca.ubc.ece.salt.pangor.analysis.Alert;
import ca.ubc.ece.salt.pangor.analysis.Commit;
import ca.ubc.ece.salt.pangor.analysis.Facts;
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
 * NOTE: This class only works with the Eclipse JDT AST.
 */
public class ClassAnalysis<A extends Alert> extends SourceCodeFileAnalysis<A> {

	/** The control flow graphs generated for the methods in the class. */
	protected List<CFG> cfgs;

	/** The analysis that inspects individual methods (and their CFGs). **/
	protected MethodAnalysis<A> methodAnalysis;

	/** The commit that this class was modified in. **/
	protected Commit commit;

	/** The source code file that this class was parsed from. **/
	protected SourceCodeFileChange sourceCodeFileChange;

	/** The analysis facts. Register patterns with this structure. **/
	protected Facts<A> facts;

	public ClassAnalysis(MethodAnalysis<A> methodAnalysis) {
		this.methodAnalysis = methodAnalysis;
	}

	@Override
	public void analyze(Commit commit,
			SourceCodeFileChange sourceCodeFileChange, Facts<A> facts,
			ClassifiedASTNode root, List<CFG> cfgs) throws Exception {

		this.commit = commit;
		this.sourceCodeFileChange = sourceCodeFileChange;
		this.facts = facts;
		this.cfgs = cfgs;

		/* Check we are working with the correct AST type. */

		if(!(root instanceof CompilationUnit)) throw new IllegalArgumentException("The AST must be parsed from Eclipse JDT.");
		CompilationUnit subjectClass = (CompilationUnit) root;

		/* Analyze each of the methods in this class. */

		List<MethodDeclaration> methodDeclarations = MethodVisitor.getMethodDeclarations(subjectClass);

		for(MethodDeclaration methodDeclaration : methodDeclarations) {
			this.methodAnalysis.analyze(commit, sourceCodeFileChange,
					subjectClass, facts, methodDeclaration,
					getFunctionCFG(methodDeclaration));
		}

	}

	/**
	 * Find the CFG for the method declaration.
	 * @param node a {@code MethodDeclaration} node.
	 * @return the CFG for the script or function.
	 */
	private CFG getFunctionCFG(ClassifiedASTNode node) {

		for(CFG cfg : this.cfgs) {
			if(cfg.getEntryNode().getStatement() == node) return cfg;
		}

		return null;

	}

}
