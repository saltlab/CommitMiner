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
 * a pattern for the method, which is the name of the method, if the name is
 * different than the name of the source method.
 */
public class SimpleMethodDstAnalysis extends MethodAnalysis {

	@Override
	protected void concreteAnalysis(SourceCodeFileChange sourceCodeFileChange,
									CompilationUnit compilationUnit,
									Map<IPredicate, IRelation> facts,
									ClassifiedASTNode root, CFG cfg,
									MethodDeclaration method) {

		if(method.getChangeType() != ChangeType.INSERTED &&
				method.getChangeType() != ChangeType.REMOVED) {

			MethodDeclaration srcMethod = (MethodDeclaration)method.getMapping();

			String srcName = srcMethod.getName().getIdentifier();
			String dstName = method.getName().getIdentifier();

			if(!srcName.equals(dstName)) {

				/* This method has been renamed. Add the fact to the database. */
				Utilities.addFact(facts, "DstMethodRename",
						Factory.TERM.createString(sourceCodeFileChange.getFileName()),
						Factory.TERM.createString(srcMethod.getName().getIdentifier()),
						Factory.TERM.createString(method.getName().getIdentifier()));

			}

		}

	}

}
