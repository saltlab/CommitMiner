package ca.ubc.ece.salt.pangor.js.learn;

import java.util.Map;

import org.deri.iris.api.basics.IPredicate;
import org.deri.iris.storage.IRelation;
import org.mozilla.javascript.ast.AstNode;
import org.mozilla.javascript.ast.ScriptNode;

import ca.ubc.ece.salt.gumtree.ast.ClassifiedASTNode.ChangeType;
import ca.ubc.ece.salt.pangor.analysis.SourceCodeFileChange;
import ca.ubc.ece.salt.pangor.cfg.CFG;
import ca.ubc.ece.salt.pangor.js.analysis.FunctionAnalysis;
import ca.ubc.ece.salt.pangor.js.analysis.scope.Scope;
import ca.ubc.ece.salt.pangor.js.learn.analysis.LearningAnalysisVisitor;
import ca.ubc.ece.salt.pangor.js.learn.api.JSAPIFactory;
import ca.ubc.ece.salt.pangor.learn.pointsto.PointsToPrediction;

public class LearningFunctionAnalysis extends FunctionAnalysis {

	/** True if this is a destination file analysis. **/
	private boolean dst;

	public LearningFunctionAnalysis(boolean dst) {
		this.dst = dst;
	}

	@Override
	public void analyze(SourceCodeFileChange sourceCodeFileChange,
			Map<IPredicate, IRelation> facts, CFG cfg, Scope<AstNode> scope)
			throws Exception {

		/* Initialize the points-to analysis. It may take some time to build the package model.
		 *
		 *  NOTE: In the future, it might be useful to put this inside
		 *  	  ScopeAnalysis so all analyses have access to detailed
		 *   	  points-to info (for APIs at least). */

		PointsToPrediction packageModel = new PointsToPrediction(JSAPIFactory.buildTopLevelAPI(),
				/*TODO: Build a model with a new visitor.*/null);

		/* If the function was inserted or deleted, there is nothing to do. We
		 * only want functions that were repaired. Class-level repairs are left
		 * for later. */
		if(scope.getScope().getChangeType() != ChangeType.INSERTED &&
		   scope.getScope().getChangeType() != ChangeType.REMOVED) {

           /* Visit the function to extract features. */
			LearningAnalysisVisitor.getLearningFacts(facts, sourceCodeFileChange,
					(ScriptNode)scope.getScope(), packageModel, this.dst);

		}

	}

}