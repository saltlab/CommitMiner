package ca.ubc.ece.salt.pangor.analysis.flow;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import org.deri.iris.api.basics.IPredicate;
import org.deri.iris.storage.IRelation;
import org.mozilla.javascript.ast.AstNode;
import org.mozilla.javascript.ast.ScriptNode;

import ca.ubc.ece.salt.gumtree.ast.ClassifiedASTNode;
import ca.ubc.ece.salt.pangor.analysis.SourceCodeFileAnalysis;
import ca.ubc.ece.salt.pangor.analysis.SourceCodeFileChange;
import ca.ubc.ece.salt.pangor.analysis.flow.abstractdomain.Address;
import ca.ubc.ece.salt.pangor.analysis.flow.abstractdomain.Helpers;
import ca.ubc.ece.salt.pangor.analysis.flow.abstractdomain.State;
import ca.ubc.ece.salt.pangor.analysis.flow.factories.StateFactory;
import ca.ubc.ece.salt.pangor.cfg.CFG;
import ca.ubc.ece.salt.pangor.cfg.ICFGVisitorFactory;

/**
 * Performs a flow analysis on the publicly accessible functions in the
 * source code file.
 */
public class ScriptFlowAnalysis extends SourceCodeFileAnalysis {

	private List<ICFGVisitorFactory> cfgVisitorFactories;

	/**
	 * @param cfgVisitors extract facts from the CFG after the analysis is complete.
	 */
	public ScriptFlowAnalysis(List<ICFGVisitorFactory> cfgVisitorFactories) {
		this.cfgVisitorFactories = cfgVisitorFactories;
	}

	@Override
	public void analyze(SourceCodeFileChange sourceCodeFileChange,
			Map<IPredicate, IRelation> facts, ClassifiedASTNode root,
			List<CFG> cfgs) throws Exception {

		/* Build a map of AstNodes to CFGs. Used for inter-proc CFA. */
		Map<AstNode, CFG> cfgMap = new HashMap<AstNode, CFG>();
		for(CFG cfg : cfgs) {
			cfgMap.put((AstNode)cfg.getEntryNode().getStatement(), cfg);
		}

		/* Create the initial state. */
		State state = StateFactory.createInitialState((ScriptNode) root, cfgMap);

		/* Perform the initial analysis and get the publicly accessible methods. */
		state = Helpers.run(cfgMap.get(root), state);

		/* Analyze the publicly accessible methods that weren't analyzed in
		 * the main analysis.
		 * NOTE: Only one level deep. Does not recursively check constructors. */
		Helpers.analyzePublic(state, state.env.environment, state.selfAddr, cfgMap, new HashSet<Address>(), null);

		/* Generate facts from the results of the analysis. */
		for(CFG cfg : cfgs) {
			for(ICFGVisitorFactory cfgVF : cfgVisitorFactories) {
				cfg.accept(cfgVF.newInstance(sourceCodeFileChange, facts));
			}
		}

	}

}