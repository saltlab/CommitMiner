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
	 * @param arbObj The argument array object.
	 * @param store Main memory.
	 * @param scratchpad Scratchpad memory.
	 * @param trace The execution trace.
	 * @param control Tracks control flow changes.
	 * @param callStack Tracks the current call stack.
	 * @return The new state after executing the function.
	 */
	public abstract State run(Map<IPredicate, IRelation> facts,
							  Address selfAddr, 
							  Store store, Scratchpad scratchpad,
							  Trace trace, Control control,
							  Stack<Address> callStack);
	
	/**
	 * Compare states to determine if the closure needs to be re-analyzed.
	 * @param envClo The environment for the closure, including parameters.
	 * @param storeClo The store for the closure, including parameter values.
	 * @param controlClo The control change environment for the closure.
	 * @return {@code true} if the states are not equivalent and the function needs
	 * 		   to be re-analyzed. {@code false} otherwise.
	 */
	protected boolean reAnalyze(Environment envClo, Store storeClo, Control controlClo,
								Environment envCall, Store storeCall, Control controlCall) {
		
		/* TODO: Make one of these that compares the state, and one that compares only the change state. */

		return false;
		
	}

}