package ca.ubc.ece.salt.pangor.java.analysis;

import java.util.LinkedList;
import java.util.List;

import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.TypeDeclaration;

/**
 * Visits a class (a {@code CompilationUnit} {@code ASTNode}) and builds a
 * list of {@code MethodDeclaration}s to be analyzed by {@code MethodAnalysis}.
 */
public class MethodVisitor extends ASTVisitor {

	List<MethodDeclaration> methodDeclarations;

	public static List<MethodDeclaration> getMethodDeclarations(CompilationUnit compilationUnit) {

		MethodVisitor visitor = new MethodVisitor();
		compilationUnit.accept(visitor);
		return visitor.getMethodDeclarations();

	}

	private MethodVisitor() {
		this.methodDeclarations = new LinkedList<MethodDeclaration>();
	}

	/**
	 * @return The list of method declarations.
	 */
	private List<MethodDeclaration> getMethodDeclarations() {
		return this.methodDeclarations;
	}

	@Override
	public boolean visit(MethodDeclaration methodDeclaration) {

		this.methodDeclarations.add(methodDeclaration);

		/* No need to continue since we don't look at TypeDeclarations. */
		return false;

	}

	@Override
	public boolean visit(TypeDeclaration typeDeclaration) {

		/* Ignore methods inside inner, local and anonymous classes. */
		if(!typeDeclaration.isPackageMemberTypeDeclaration()) {
			return false;
		}

		/* Must be the current class we are analyzing. Continue. */
		return true;

	}

}