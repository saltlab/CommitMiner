package ca.ubc.ece.salt.pangor.js.learn.analysis;

import java.util.List;
import java.util.Map;

import org.deri.iris.api.basics.IPredicate;
import org.deri.iris.storage.IRelation;
import org.mozilla.javascript.ast.AstRoot;

import ca.ubc.ece.salt.gumtree.ast.ClassifiedASTNode;
import ca.ubc.ece.salt.pangor.analysis.SourceCodeFileAnalysis;
import ca.ubc.ece.salt.pangor.analysis.SourceCodeFileChange;
import ca.ubc.ece.salt.pangor.cfg.CFG;
import ca.ubc.ece.salt.pangor.js.learn.analysis.ChangeComplexityVisitor.ChangeComplexity;

/**
 * Computes the change complexity (number of modified statements) for a source
 * code file.
 */
public class ChangeComplexitySCFA extends SourceCodeFileAnalysis {

	private ChangeComplexity complexity;

	public ChangeComplexitySCFA() {
		this.complexity = null;
	}

	/**
	 * @return The number of modified statements in the file.
	 */
	public ChangeComplexity getChangeComplexity() {
		return this.complexity;
	}

	@Override
	public void analyze(SourceCodeFileChange sourceCodeFileChange,
			Map<IPredicate, IRelation> facts, ClassifiedASTNode root,
			List<CFG> cfgs) throws Exception {

		this.complexity = ChangeComplexityVisitor.getChangeComplexity((AstRoot)root);

	}

}