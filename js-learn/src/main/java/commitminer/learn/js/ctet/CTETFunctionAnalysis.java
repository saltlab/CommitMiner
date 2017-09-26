package commitminer.learn.js.ctet;

import java.util.Map;

import org.deri.iris.api.basics.IPredicate;
import org.deri.iris.storage.IRelation;
import org.mozilla.javascript.ast.AstNode;
import org.mozilla.javascript.ast.ScriptNode;

import ca.ubc.ece.salt.gumtree.ast.ClassifiedASTNode.ChangeType;
import commitminer.analysis.SourceCodeFileChange;
import commitminer.cfg.CFG;
import commitminer.js.analysis.FunctionAnalysis;
import commitminer.js.analysis.scope.Scope;
import commitminer.pointsto.PointsToPrediction;

public class CTETFunctionAnalysis extends FunctionAnalysis {

	/** True if this is a destination file analysis. **/
	private boolean dst;

	public CTETFunctionAnalysis(boolean dst) {
		this.dst = dst;
	}

	@Override
	public void analyze(SourceCodeFileChange sourceCodeFileChange,
			Map<IPredicate, IRelation> facts, CFG cfg, Scope<AstNode> scope,
			PointsToPrediction model)
			throws Exception {

		/* If the function was inserted or deleted, there is nothing to do. We
		 * only want functions that were repaired. Class-level repairs are left
		 * for later. */
		if(scope.getScope().getChangeType() != ChangeType.INSERTED &&
		   scope.getScope().getChangeType() != ChangeType.REMOVED) {

           /* Visit the function to extract features. */
			CTETSourceAnalysisVisitor.getLearningFacts(facts, sourceCodeFileChange,
					(ScriptNode)scope.getScope(), model, this.dst);

		}

	}

}