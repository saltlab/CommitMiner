package ca.ubc.ece.salt.pangor.js.learn.ctet;

import java.util.List;
import java.util.Map;

import org.deri.iris.api.basics.IPredicate;
import org.deri.iris.storage.IRelation;
import org.mozilla.javascript.ast.AstRoot;

import ca.ubc.ece.salt.gumtree.ast.ClassifiedASTNode;
import ca.ubc.ece.salt.pangor.analysis.SourceCodeFileAnalysis;
import ca.ubc.ece.salt.pangor.analysis.SourceCodeFileChange;
import ca.ubc.ece.salt.pangor.cfg.CFG;

/**
 * An analysis of a JavaScript file for extracting <Change Type, Entity Type>
 * pairs.
 *
 * NOTES:
 * 	1. This class only works with the Rhino AST.
 * 	2. This class is thread-safe.
 */
public class CTETScriptAnalysis extends SourceCodeFileAnalysis {

	@Override
	public void analyze(SourceCodeFileChange sourceCodeFileChange,
						Map<IPredicate, IRelation> facts,
						ClassifiedASTNode root,
						List<CFG> cfgs) throws Exception {

		/* Check we are working with the correct AST type. */
		if(!(root instanceof AstRoot)) throw new IllegalArgumentException("The AST must be parsed from Eclipse JDT.");
		AstRoot script = (AstRoot) root;

		CTETSourceAnalysisVisitor.getLearningFacts(facts, sourceCodeFileChange, script, null, true);

	}

}