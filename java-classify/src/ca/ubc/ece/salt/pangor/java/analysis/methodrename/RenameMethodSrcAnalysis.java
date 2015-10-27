package ca.ubc.ece.salt.pangor.java.analysis.methodrename;

import org.eclipse.jdt.core.dom.MethodDeclaration;

import ca.ubc.ece.salt.gumtree.ast.ClassifiedASTNode.ChangeType;
import ca.ubc.ece.salt.pangor.analysis.simple.SimpleAlert;
import ca.ubc.ece.salt.pangor.analysis.simple.SimplePattern;
import ca.ubc.ece.salt.pangor.cfg.CFG;
import ca.ubc.ece.salt.pangor.java.analysis.MethodAnalysis;

/**
 * Performs a simple analysis of a Java method. The analysis registers
 * a pre-condition for the method, which is the name of the method.
 */
public class RenameMethodSrcAnalysis extends MethodAnalysis<SimpleAlert> {

	public RenameMethodSrcAnalysis() { }

	@Override
	public void concreteAnalysis(MethodDeclaration method, CFG cfg) {
		if(method.getChangeType() != ChangeType.INSERTED &&
				method.getChangeType() != ChangeType.REMOVED) {

			this.facts.addPreCondition(
				new SimplePattern(
					this.commit,
					this.sourceCodeFileChange,
					method.toString(),
					method.getName().getIdentifier()));

		}
	}

}
