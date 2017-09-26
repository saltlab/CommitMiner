package commitminer.learn.js.analysis;

import java.util.List;
import java.util.Map;

import org.deri.iris.api.basics.IPredicate;
import org.deri.iris.storage.IRelation;
import org.mozilla.javascript.ast.AstRoot;

import ca.ubc.ece.salt.gumtree.ast.ClassifiedASTNode;
import commitminer.analysis.SourceCodeFileAnalysis;
import commitminer.analysis.SourceCodeFileChange;
import commitminer.cfg.CFG;
import commitminer.learn.js.analysis.ChangeComplexityVisitor.ChangeComplexity;

/**
 * Computes the change complexity (number of modified statements) for a source
 * code file.
 */
public class ChangeComplexitySCFA extends SourceCodeFileAnalysis {

	private ChangeComplexity complexity;

	private boolean dst;

	/**
	 * @param dst True if the destination file is being analyzed.
	 */
	public ChangeComplexitySCFA(boolean dst) {
		this.complexity = null;
		this.dst = dst;
	}

	/**
	 * @return The number of modified statements in the file.
	 */
	public ChangeComplexity getChangeComplexity() {
		return this.complexity;
	}

	/**
	 * Reset the complexity after the class has been used.
	 */
	public void resetComplexity() {
		this.complexity = null;
	}

	@Override
	public void analyze(SourceCodeFileChange sourceCodeFileChange,
			Map<IPredicate, IRelation> facts, ClassifiedASTNode root,
			List<CFG> cfgs) throws Exception {

		this.complexity = ChangeComplexityVisitor.getChangeComplexity((AstRoot)root, this.dst);

	}

}