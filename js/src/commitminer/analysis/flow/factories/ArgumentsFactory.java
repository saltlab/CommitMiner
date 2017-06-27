package commitminer.analysis.flow.factories;

import java.util.HashMap;
import java.util.Map;
import java.util.Stack;

import org.deri.iris.api.basics.IPredicate;
import org.deri.iris.storage.IRelation;
import org.mozilla.javascript.ast.AstNode;

import commitminer.analysis.flow.abstractdomain.Address;
import commitminer.analysis.flow.abstractdomain.Change;
import commitminer.analysis.flow.abstractdomain.Closure;
import commitminer.analysis.flow.abstractdomain.Control;
import commitminer.analysis.flow.abstractdomain.DefinerIDs;
import commitminer.analysis.flow.abstractdomain.Property;
import commitminer.analysis.flow.abstractdomain.InternalFunctionProperties;
import commitminer.analysis.flow.abstractdomain.InternalObjectProperties;
import commitminer.analysis.flow.abstractdomain.JSClass;
import commitminer.analysis.flow.abstractdomain.NativeClosure;
import commitminer.analysis.flow.abstractdomain.Num;
import commitminer.analysis.flow.abstractdomain.Obj;
import commitminer.analysis.flow.abstractdomain.Scratchpad;
import commitminer.analysis.flow.abstractdomain.State;
import commitminer.analysis.flow.abstractdomain.Store;
import commitminer.analysis.flow.trace.Trace;
import commitminer.cfg.CFG;


public class ArgumentsFactory {
	
	private static final Integer ARG_DEFINER_ID = -2;

	Store store;
	Map<AstNode, CFG> cfgs;

	public ArgumentsFactory(Store store, Map<AstNode, CFG> cfgs) {
		this.store = store;
		this.cfgs = cfgs;
	}

	public Obj Arguments_Obj() {
		Map<String, Property> ext = new HashMap<String, Property>();
		store = Helpers.addProp("prototype", ARG_DEFINER_ID, Address.inject(StoreFactory.Object_proto_Addr, Change.u(), Change.u(), DefinerIDs.bottom()), ext, store);
		store = Helpers.addProp("length", ARG_DEFINER_ID, Num.inject(Num.top(Change.u()), Change.u(), DefinerIDs.bottom()), ext, store);

		NativeClosure closure = new NativeClosure() {
				@Override
				public State run(Map<IPredicate, IRelation> facts, 
								 Address selfAddr, 
								 Store store, Scratchpad scratchpad,
								 Trace trace, Control control,
								 Stack<Address> callStack) {
					return new State(facts, store, null, scratchpad, trace, control,
									 selfAddr, cfgs, callStack);
				}
			};

		Stack<Closure> closures = new Stack<Closure>();
		closures.push(closure);

		InternalObjectProperties internal = new InternalFunctionProperties(closures, JSClass.CArguments);

		return new Obj(ext, internal);
	}

}