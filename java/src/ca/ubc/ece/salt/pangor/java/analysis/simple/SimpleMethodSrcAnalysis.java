package ca.ubc.ece.salt.pangor.java.analysis.simple;

import java.util.Map;

import org.deri.iris.api.basics.IPredicate;
import org.deri.iris.factory.Factory;
import org.deri.iris.storage.IRelation;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.MethodDeclaration;

import ca.ubc.ece.salt.gumtree.ast.ClassifiedASTNode;
import ca.ubc.ece.salt.gumtree.ast.ClassifiedASTNode.ChangeType;
import ca.ubc.ece.salt.pangor.analysis.SourceCodeFileChange;
import ca.ubc.ece.salt.pangor.analysis.Utilities;
import ca.ubc.ece.salt.pangor.cfg.CFG;
import ca.ubc.ece.salt.pangor.java.analysis.MethodAnalysis;

/**
 * Performs a simple analysis of a Java method. The analysis registers
 * a pre-condition for the method, which is the name of the method.
 */
public class SimpleMethodSrcAnalysis extends MethodAnalysis {

	@Override
	protected void concreteAnalysis(SourceCodeFileChange sourceCodeFileChange,
									CompilationUnit compilationUnit,
									Map<IPredicate, IRelation> facts,
									ClassifiedASTNode root, CFG cfg,
									MethodDeclaration method) {

		if(method.getChangeType() != ChangeType.INSERTED &&
				method.getChangeType() != ChangeType.REMOVED) {

			/* This method exists in both versions. Add the fact to the
			 * database. This will be our pre-condition. */
			Utilities.addFact(facts, "MethodExists",
					Factory.TERM.createString(sourceCodeFileChange.getFileName()),
					Factory.TERM.createString(method.getName().getIdentifier()));

		}
	}

}
