package ca.ubc.ece.salt.pangor.java.analysis.methodrename;

import org.eclipse.jdt.core.dom.MethodDeclaration;

import ca.ubc.ece.salt.pangor.analysis.classify.ClassifierAlert;
import ca.ubc.ece.salt.pangor.cfg.CFG;
import ca.ubc.ece.salt.pangor.java.analysis.MethodAnalysis;

/**
 * Currently does nothing.
 */
public class RenameMethodSrcAnalysis extends MethodAnalysis<ClassifierAlert> {

	public RenameMethodSrcAnalysis() { }

	@Override
	public void concreteAnalysis(MethodDeclaration method, CFG cfg) { }

}
