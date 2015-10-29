package ca.ubc.ece.salt.pangor.java.analysis.methodrename;

import org.eclipse.jdt.core.dom.MethodDeclaration;

import ca.ubc.ece.salt.gumtree.ast.ClassifiedASTNode.ChangeType;
import ca.ubc.ece.salt.pangor.analysis.classify.ClassifierAlert;
import ca.ubc.ece.salt.pangor.cfg.CFG;
import ca.ubc.ece.salt.pangor.java.analysis.MethodAnalysis;
import ca.ubc.ece.salt.pangor.java.analysis.methodrename.RenameMethodVisitor.UpdatedCallsite;

/**
 * Looks for the following facts:
 * 	1. Pattern = A method name was changed in a method declaration.
 *  2. PreCondition = A method name was changed in a method declaration.
 *  3. Pattern = A method name was changed in a call site.
 *  4. Anti-pattern = A method name was unchanged in a method declaration.
 *  5. Anti-pattern = A method name was unchanged in a call site.
 */
public class RenameMethodDstAnalysis extends MethodAnalysis<ClassifierAlert> {

	public RenameMethodDstAnalysis() { }

	@Override
	public void concreteAnalysis(MethodDeclaration method, CFG cfg) {

		/* Add a method rename pattern and pre-condition if this method was renamed. */
		if(method.getChangeType() == ChangeType.UPDATED) {

			MethodDeclaration srcMethod = (MethodDeclaration)method.getMapping();

			String srcName = srcMethod.getName().getIdentifier();
			String dstName = method.getName().getIdentifier();

			RenameMethodPattern pattern = new RenameMethodPattern(
												this.commit,
												this.sourceCodeFileChange,
												srcName, dstName);

			this.facts.addPattern(pattern);
			this.facts.addPreCondition(pattern);

		}
		/* Add a method rename anti-pattern if this method was not renamed. */
		else if(method.getChangeType() == ChangeType.UNCHANGED) {
			this.facts.addAntiPattern(
					new RenameMethodAntiPattern(
							method.getName().getIdentifier()));
		}

		/* Get patterns from call sites. */
		RenameMethodVisitor visitor = new RenameMethodVisitor();
		method.accept(visitor);

		/* Add the facts generated in the visitor. */
		for(UpdatedCallsite updated : visitor.getUpdatedCallsites()) {

			int oldLine = this.compilationUnit.getLineNumber(updated.oldNode.getStartPosition()-1);
			int newLine = this.compilationUnit.getLineNumber(updated.newNode.getStartPosition()-1);

			this.facts.addPattern(
					new UpdateCallsitePattern(
							this.commit,
							this.sourceCodeFileChange,
							method.getName().getIdentifier(),
							oldLine, newLine,
							updated.oldName, updated.newName));
		}
		for(String unchanged : visitor.getUnchangedCallsites()) {
			this.facts.addAntiPattern(
					new RenameMethodAntiPattern(
							unchanged));
		}

	}

}