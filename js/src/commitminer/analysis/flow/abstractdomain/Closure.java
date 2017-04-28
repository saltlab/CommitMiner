package commitminer.analysis.flow.abstractdomain;

import java.util.Map;
import java.util.Stack;

import org.deri.iris.api.basics.IPredicate;
import org.deri.iris.storage.IRelation;

import commitminer.analysis.flow.trace.Trace;

/**
 * A native (builtin) function. The abstract interpretation of the function is
 * specified in Java, usually as a lambda expression.
 */
public abstract class Closure {

	/**
	 * @param facts The fact database for facts generated during analysis.
	 * @param selfAddr The value of the 'this' variable (a set of objects).
	 * @param argArrayAddr The address of the argument array object.
	 * @param store Main memory.
	 * @param scratchpad Scratchpad memory.
	 * @param trace The execution trace.
	 * @param control Tracks control flow changes.
	 * @param callStack Tracks the current call stack.
	 * @return The new state after executing the function.
	 */
	public abstract State run(Map<IPredicate, IRelation> facts,
							  Address selfAddr, Address argArrayAddr,
							  Store store, Scratchpad scratchpad,
							  Trace trace, Control control,
							  Stack<Address> callStack);

}