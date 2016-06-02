package ca.ubc.ece.salt.pangor.analysis.flow;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.deri.iris.api.basics.IPredicate;
import org.deri.iris.storage.IRelation;
import org.mozilla.javascript.ast.AstNode;
import org.mozilla.javascript.ast.FunctionNode;
import org.mozilla.javascript.ast.ScriptNode;

import ca.ubc.ece.salt.gumtree.ast.ClassifiedASTNode;
import ca.ubc.ece.salt.pangor.analysis.SourceCodeFileAnalysis;
import ca.ubc.ece.salt.pangor.analysis.SourceCodeFileChange;
import ca.ubc.ece.salt.pangor.analysis.flow.abstractdomain.Address;
import ca.ubc.ece.salt.pangor.analysis.flow.abstractdomain.BValue;
import ca.ubc.ece.salt.pangor.analysis.flow.abstractdomain.Change;
import ca.ubc.ece.salt.pangor.analysis.flow.abstractdomain.FunctionClosure;
import ca.ubc.ece.salt.pangor.analysis.flow.abstractdomain.Helpers;
import ca.ubc.ece.salt.pangor.analysis.flow.abstractdomain.Identifier;
import ca.ubc.ece.salt.pangor.analysis.flow.abstractdomain.InternalFunctionProperties;
import ca.ubc.ece.salt.pangor.analysis.flow.abstractdomain.InternalObjectProperties;
import ca.ubc.ece.salt.pangor.analysis.flow.abstractdomain.JSClass;
import ca.ubc.ece.salt.pangor.analysis.flow.abstractdomain.Obj;
import ca.ubc.ece.salt.pangor.analysis.flow.abstractdomain.State;
import ca.ubc.ece.salt.pangor.analysis.flow.factories.StateFactory;
import ca.ubc.ece.salt.pangor.analysis.flow.factories.StoreFactory;
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
		state = Helpers.run(cfgMap.get(root), state, StoreFactory.global_binding_Addr);

		/* Analyze the publicly accessible methods that weren't analyzed in
		 * the main analysis.
		 * NOTE: Only one level deep. Does not recursively check constructors. */
		analyzePublic(state, state.env.environment, StoreFactory.global_binding_Addr, cfgMap);

		/* Generate facts from the results of the analysis. */
		for(CFG cfg : cfgs) {
			for(ICFGVisitorFactory cfgVF : cfgVisitorFactories) {
				cfg.accept(cfgVF.newInstance(sourceCodeFileChange, facts));
			}
		}

	}

	/**
	 * Analyze publicly accessible functions that have not already been
	 * analyzed. This is currently not done recursively, because we would have
	 * to decide whether or not to run the function as a constructor.
	 * @param state The end state of the parent function.
	 */
	private void analyzePublic(State state, Map<Identifier, Address> props, Address selfAddr, Map<AstNode, CFG> cfgMap) {

		for(Identifier var : props.keySet()) {
			BValue val = state.store.apply(props.get(var));

			for(Address objAddr : val.addressAD.addresses) {
				Obj obj = state.store.getObj(objAddr);

				/* We may need to analyze this function. */
				if(obj.internalProperties.klass == JSClass.CFunction) {

					InternalFunctionProperties ifp = (InternalFunctionProperties)obj.internalProperties;
					FunctionClosure fc = (FunctionClosure)ifp.closure;

					if(ifp.closure instanceof FunctionClosure &&
							fc.cfg.getEntryNode().getState() == null) {

						/* Create the argument object. */
						Address argAddr = createTopArgObject(state, (FunctionNode)fc.cfg.getEntryNode().getStatement());

						/* Analyze the function. */
						@SuppressWarnings("unused")
						State newState = ifp.closure.run(selfAddr, argAddr, state.store, state.scratch, state.trace, state.control);

						/* Check the function object. */
						// TODO: We ignore this for now. We would have to assume the function is being run as a constructor.

					}
				}

				/* Recursively look for object properties that are functions. */
				analyzePublic(state, obj.externalProperties, props.get(var), cfgMap);

			}
		}
	}

	/**
	 * Creates an arg object where each argument corresponds to a parameter
	 * and each argument value is BValue.TOP.
	 * @param state
	 * @param f The function
	 * @return
	 */
	private Address createTopArgObject(State state, FunctionNode f) {

		/* Create the argument object. */
		Map<Identifier, Address> ext = new HashMap<Identifier, Address>();

		int i = 0;
		for(AstNode param : f.getParams()) {

			BValue argVal = BValue.top(Change.convU(param), Change.u());
			state.store = Helpers.addProp(f.getID(), String.valueOf(i), argVal,
										  ext, state.store, state.trace);

			i++;
		}

		InternalObjectProperties internal = new InternalObjectProperties(
				Address.inject(StoreFactory.Arguments_Addr, Change.convU(f), Change.u()), JSClass.CFunction);
		Obj argObj = new Obj(ext, internal);

		/* Add the argument object to the store. */
		Address argAddr = state.trace.makeAddr(f.getID(), "");
		state.store = state.store.alloc(argAddr, argObj);

		return argAddr;

	}

}