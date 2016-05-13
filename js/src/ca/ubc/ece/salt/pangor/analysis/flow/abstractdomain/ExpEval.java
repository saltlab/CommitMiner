package ca.ubc.ece.salt.pangor.analysis.flow.abstractdomain;

import java.util.HashMap;
import java.util.Map;

import org.mozilla.javascript.ast.Assignment;
import org.mozilla.javascript.ast.AstNode;
import org.mozilla.javascript.ast.EmptyStatement;
import org.mozilla.javascript.ast.ExpressionStatement;
import org.mozilla.javascript.ast.FunctionCall;
import org.mozilla.javascript.ast.Name;

import ca.ubc.ece.salt.pangor.analysis.flow.abstractdomain.Scratchpad.Scratch;
import ca.ubc.ece.salt.pangor.analysis.flow.factories.StoreFactory;
import ca.ubc.ece.salt.pangor.analysis.flow.trace.Trace;

public class ExpEval {

	/**
	 * Evaluate an expression to a BValue.
	 * @param env The current environment.
	 * @param store The current store.
	 * @param trace The current execution trace.
	 * @param node The expression to evaluate.
	 * @return The value of the expression.
	 */
	public static BValue eval(Environment env, Store store, Scratchpad scratch,
							  Trace trace, AstNode node, Address selfAddr) {

		if(node instanceof EmptyStatement) {
			return BValue.bottom();
		}
		if(node instanceof ExpressionStatement) {
			ExpressionStatement es = (ExpressionStatement)node;
			return eval(env, store, scratch, trace, es.getExpression(), selfAddr);
		}
		else if(node instanceof Name) {
			return store.apply(env.apply(node.toSource()));
		}
		else if(node instanceof FunctionCall) {
			return evalFunctionCall(env, store, scratch, trace, (FunctionCall)node, selfAddr);
		}
		else if(node instanceof Assignment) {

		}

		/* We could not evaluate the expression. Return top. */
		return BValue.top();

	}

	/**
	 * Transfer the abstract domains over the assignment.
	 * @param a The assignment.
	 */
	public static void evalAssignment(Environment env, Store store, Scratchpad scratch,
									  Trace trace, Assignment a, Address selfAddr) {

		AstNode lhs = a.getLeft();
		AstNode rhs = a.getRight();

		/* Resolve the left hand side to a set of addresses (. */
		BValue val = Helpers.resolve(env, store, lhs);

	}

	/**
	 * Evaluate a function call expression to a BValue.
	 * @param fc The function call.
	 * @return The return value of the function call.
	 */
	public static BValue evalFunctionCall(Environment env, Store store, Scratchpad scratch,
										  Trace trace, FunctionCall fc, Address selfAddr) {

			/* Create the argument object. */
			Map<String, Address> ext = new HashMap<String, Address>();
			int i = 0;
			for(AstNode arg : fc.getArguments()) {
				BValue argVal = eval(env, store, scratch, trace, arg, selfAddr);
				Helpers.addProp(arg.getID(), String.valueOf(i), argVal,
								ext, store, trace);
				i++;
			}

			InternalObjectProperties internal = new InternalObjectProperties(
					Address.inject(StoreFactory.Arguments_Addr), JSClass.CFunction);
			Obj argObj = new Obj(ext, internal, ext.keySet());

			/* Add the argument object to the store. */
			Address argAddr = trace.makeAddr(fc.getID());
			store = store.alloc(argAddr, argObj);

			/* Attempt to resolve the function and it's parent object. */
			BValue funVal = Helpers.resolve(env, store, fc.getTarget());
			BValue objVal = Helpers.resolveSelf(env, store, fc.getTarget());

			/* If the function is not a member variable, it is local and we
			 * use the object of the currently executing function as self. */
			Address objAddr = trace.toAddr("this");
			if(objVal == null) objAddr = selfAddr;
			else store = store.alloc(objAddr, objVal);

			if(funVal == null) {
				/* If the function was not resolved, we assume the (local)
				 * state is unchanged, but add BValue.TOP as the return value. */
				return BValue.top();
			}
			else {
				/* Call the function and get a join of the new states. */
				State retState = Helpers.applyClosure(funVal, objAddr, argAddr, store,
													  scratch, trace);
				return retState.scratchpad.apply(Scratch.RETVAL);
			}

	}

}
