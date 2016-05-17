package ca.ubc.ece.salt.pangor.analysis.flow;

import java.util.HashMap;
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
import ca.ubc.ece.salt.pangor.analysis.flow.abstractdomain.BValue;
import ca.ubc.ece.salt.pangor.analysis.flow.abstractdomain.FunctionClosure;
import ca.ubc.ece.salt.pangor.analysis.flow.abstractdomain.Helpers;
import ca.ubc.ece.salt.pangor.analysis.flow.abstractdomain.InternalFunctionProperties;
import ca.ubc.ece.salt.pangor.analysis.flow.abstractdomain.JSClass;
import ca.ubc.ece.salt.pangor.analysis.flow.abstractdomain.Obj;
import ca.ubc.ece.salt.pangor.analysis.flow.abstractdomain.State;
import ca.ubc.ece.salt.pangor.analysis.flow.factories.StateFactory;
import ca.ubc.ece.salt.pangor.analysis.flow.factories.StoreFactory;
import ca.ubc.ece.salt.pangor.cfg.CFG;

/**
 * Performs a flow analysis on the publicly accessible functions in the
 * source code file.
 */
public class ScriptFlowAnalysis extends SourceCodeFileAnalysis {

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
		state = Helpers.run(cfgMap.get(root), state, StoreFactory.global_binding_Addr);

		// TODO
		/* Analyze the publicly accessible methods that weren't analyzed in
		 * the main analysis. */

		/* Generate facts from the results of the analysis. */
		for(CFG cfg : cfgs) {
			cfg.accept(new ProtectedCFGVisitor(sourceCodeFileChange, facts));
		}

	}

	/**
	 * Recursively analyze publicly accessible functions that have not
	 * already been analyzed.
	 * @param state The end state of the parent function.
	 */
	private void analyzePublic(State state, Map<AstNode, CFG> cfgMap) {

		for(String var : state.env.environment.keySet()) {

			BValue val = state.store.apply(state.env.environment.get(var));

			for(Address a : val.addressAD.addresses) {

				Obj o = state.store.getObj(a);

				/* We may need to analyze this function. */
				if(o.internalProperties.klass == JSClass.CFunction) {
					InternalFunctionProperties ifp = (InternalFunctionProperties)o.internalProperties;
					FunctionClosure fc = (FunctionClosure)ifp.closure;
					if(ifp.closure instanceof FunctionClosure &&
							fc.cfg.getEntryNode().getState() != null) {
						// TODO: Analyze the function
						// TODO: Because more public functions may have been
						//		 added to this object during its execution,
						//		 Recursively look for un-analyzed functions in
						//		 the new state.
					}
				}

				/* Investigate this object. */
				// TODO: Look for objects in properties of o (o.ext).
				// TODO: analyzePublic(state, cfgMap


			}

		}

	}

	/**
	 * Recursively analyze publicly accessible functions that have not
	 * already been analyzed.
	 * @param state The end state of the parent function.
	 */
	private void analyzePublic(State state, Map<String, Address> props, Map<AstNode, CFG> cfgMap) {

		for(String var : state.env.environment.keySet()) {
			BValue val = state.store.apply(state.env.environment.get(var));

			for(Address a : val.addressAD.addresses) {
				Obj o = state.store.getObj(a);

				/* We may need to analyze this function. */
				if(o.internalProperties.klass == JSClass.CFunction) {
					InternalFunctionProperties ifp = (InternalFunctionProperties)o.internalProperties;
					FunctionClosure fc = (FunctionClosure)ifp.closure;
					if(ifp.closure instanceof FunctionClosure &&
							fc.cfg.getEntryNode().getState() != null) {

						/* Analyze the function. */
						State newState = ifp.closure.run(a, argArrayAddr/* TODO */, state.store, state.scratch, state.trace);

						/* Check the function object. */
						// TODO: We ignore this for now. We would have to assume the function is being run as a constructor.

					}
				}

				/* Recursively look for object properties that are functions. */
				analyzePublic(state, o.externalProperties, cfgMap);

			}
		}
	}

}