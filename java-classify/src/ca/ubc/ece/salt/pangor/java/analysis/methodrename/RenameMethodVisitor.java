package ca.ubc.ece.salt.pangor.java.analysis.methodrename;

import java.util.LinkedList;
import java.util.List;

import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.SimpleName;

import ca.ubc.ece.salt.gumtree.ast.ClassifiedASTNode.ChangeType;

/**
 * Visits a {@code MethodDeclaration} {@code ASTNode}) and looks for patterns
 * related to a rename method refactoring (i.e., updated call sites).
 */
public class RenameMethodVisitor extends ASTVisitor {

	private List<UpdatedCallsite> updatedCallsites;
	private List<String> unchangedCallsites;

	public RenameMethodVisitor() {

		this.updatedCallsites = new LinkedList<UpdatedCallsite>();
		this.unchangedCallsites = new LinkedList<String>();

	}

	/**
	 * @return The updated call sites found in the method body.
	 */
	public List<UpdatedCallsite> getUpdatedCallsites() {
		return this.updatedCallsites;
	}

	/**
	 * @return The unchanged call sites found in the method body.
	 */
	public List<String> getUnchangedCallsites() {
		return this.unchangedCallsites;
	}

	@Override
	public boolean visit(MethodInvocation methodInvocation) {

		SimpleName target = methodInvocation.getName();

		/* If the target name is updated, store a pattern. */
		if(target.getChangeType() == ChangeType.UPDATED) {

			SimpleName oldTarget = (SimpleName)target.getMapping();

			if(!oldTarget.equals(target)) {

				this.updatedCallsites.add(new UpdatedCallsite(
						oldTarget, target,
						oldTarget.getIdentifier(),
						target.getIdentifier()));

			}

		}
		/* If the target name is not updated, store an anti-pattern. */
		else if(target.getChangeType() == ChangeType.UNCHANGED){

			this.unchangedCallsites.add(target.getIdentifier());

		}

		/* Continue searching for call sites within this expression. */
		return true;

	}

	/**
	 * Stores the old name and the new name of an updated call site.
	 */
	public class UpdatedCallsite {

		public SimpleName oldNode;
		public SimpleName newNode;

		public String oldName;
		public String newName;

		public UpdatedCallsite(SimpleName oldNode, SimpleName newNode,
				String oldName, String newName) {
			this.oldNode = oldNode;
			this.newNode = newNode;
			this.oldName = oldName;
			this.newName = newName;
		}

	}

}