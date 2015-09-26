package ca.ubc.ece.salt.pangor.analysis.promises;

import java.util.List;

import org.mozilla.javascript.ast.AstNode;
import org.mozilla.javascript.ast.AstRoot;
import org.mozilla.javascript.ast.FunctionNode;
import org.mozilla.javascript.ast.NodeVisitor;

import ca.ubc.ece.salt.pangor.analysis.classify.ClassifierDataSet;
import ca.ubc.ece.salt.pangor.analysis.scope.Scope;
import ca.ubc.ece.salt.pangor.analysis.scope.ScopeAnalysis;
import ca.ubc.ece.salt.pangor.batch.AnalysisMetaInformation;
import ca.ubc.ece.salt.pangor.cfg.CFG;
import ca.ubc.ece.salt.pangor.classify.alert.ClassifierAlert;

public class PromisesSourceAnalysis extends ScopeAnalysis<ClassifierAlert, ClassifierDataSet> {

	/** Set to true if the analysis determines the pre-conditoins are met. **/
	private boolean meetsPreConditions;

	public PromisesSourceAnalysis(ClassifierDataSet dataSet,
			AnalysisMetaInformation ami) {
		super(dataSet, ami);
	}

	/**
	 * @return True if the pre-conditions are met for the refactoring.
	 */
	public boolean meetsPreConditions() {
		return this.meetsPreConditions;
	}

	@Override
	public void analyze(AstRoot root, List<CFG> cfgs) throws Exception {
		super.analyze(root, cfgs);

		/* Look at each function. */
	}

	@Override
	public void analyze(AstRoot srcRoot, List<CFG> srcCFGs, AstRoot dstRoot, List<CFG> dstCFGs) throws Exception {
		super.analyze(srcRoot, srcCFGs, dstRoot, dstCFGs);

		/* Look at each function. */
	}

	/**
	 * @param scope The function to inspect.
	 */
	private void inspectFunctions(Scope scope) {

		/* Visit the function and look for REF_PROM patterns. */
		if (scope.scope instanceof FunctionNode) {
			FunctionNode function = (FunctionNode) scope.scope;
			this.meetsPreConditions = meetsPreConditions(function);
		} else {
			this.meetsPreConditions = meetsPreConditions(scope.scope);
		}

		for (Scope child : scope.children) {
			inspectFunctions(child);
		}

	}

	/**
	 * Checks the if the preconditions for a callback to promise
	 * refactoring exists.
	 * @param body The script or body of a function.
	 * @return True if this function meets the pre-conditions for a
	 * 		   callback to promise refactoring.
	 */
	private boolean meetsPreConditions(AstNode body) {
		PromisesDestinationVisitor visitor = new PromisesDestinationVisitor();
		body.visit(visitor);
		return visitor.meetsPreConditions;
	}

	/**
	 * Visits a function to find anti-patterns or pre-conditions for a callback
	 * to promises refactoring.
	 */
	private class PromisesDestinationVisitor implements NodeVisitor {

		public boolean meetsPreConditions;

		public PromisesDestinationVisitor() {
			this.meetsPreConditions = false;
		}

		@Override
		public boolean visit(AstNode arg0) {
			// TODO
			return false;
		}

	}

}
