package ca.ubc.ece.salt.pangor.java.analysis.methodrename;

import org.eclipse.jdt.core.dom.MethodDeclaration;

import ca.ubc.ece.salt.gumtree.ast.ClassifiedASTNode.ChangeType;
import ca.ubc.ece.salt.pangor.analysis.simple.SimpleAlert;
import ca.ubc.ece.salt.pangor.analysis.simple.SimplePattern;
import ca.ubc.ece.salt.pangor.cfg.CFG;
import ca.ubc.ece.salt.pangor.java.analysis.MethodAnalysis;

/**
 * Performs a simple analysis of a Java method. The analysis registers
 * a pattern for the method, which is the name of the method, if the name is
 * different than the name of the source method.
 */
public class RenameMethodDstAnalysis extends MethodAnalysis<SimpleAlert> {

	public RenameMethodDstAnalysis() { }

	@Override
	public void concreteAnalysis(MethodDeclaration method, CFG cfg) {
		if(method.getChangeType() != ChangeType.INSERTED &&
				method.getChangeType() != ChangeType.REMOVED) {

			MethodDeclaration srcMethod = (MethodDeclaration)method.getMapping();

			String srcName = srcMethod.getName().getIdentifier();
			String dstName = method.getName().getIdentifier();

			if(!srcName.equals(dstName)) {

				this.facts.addPattern(
					new SimplePattern(
						this.commit,
						this.sourceCodeFileChange,
						srcMethod.toString(),
						srcMethod.getName().getIdentifier() + " -> " + method.getName().getIdentifier()));

			}

		}
	}

}
