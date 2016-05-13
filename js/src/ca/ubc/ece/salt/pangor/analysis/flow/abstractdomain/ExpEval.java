package ca.ubc.ece.salt.pangor.analysis.flow.abstractdomain;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.math.NumberUtils;
import org.mozilla.javascript.Token;
import org.mozilla.javascript.ast.Assignment;
import org.mozilla.javascript.ast.AstNode;
import org.mozilla.javascript.ast.EmptyStatement;
import org.mozilla.javascript.ast.ExpressionStatement;
import org.mozilla.javascript.ast.FunctionCall;
import org.mozilla.javascript.ast.InfixExpression;
import org.mozilla.javascript.ast.KeywordLiteral;
import org.mozilla.javascript.ast.Name;
import org.mozilla.javascript.ast.NumberLiteral;
import org.mozilla.javascript.ast.StringLiteral;
import org.mozilla.javascript.ast.VariableDeclaration;
import org.mozilla.javascript.ast.VariableInitializer;

import ca.ubc.ece.salt.pangor.analysis.flow.abstractdomain.Scratchpad.Scratch;
import ca.ubc.ece.salt.pangor.analysis.flow.abstractdomain.Str.LatticeElementType;
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
		else if(node instanceof ExpressionStatement) {
			ExpressionStatement es = (ExpressionStatement)node;
			return eval(env, store, scratch, trace, es.getExpression(), selfAddr);
		}
		else if(node instanceof VariableDeclaration) {
			evalVariableDeclaration(env, store, scratch, trace, (VariableDeclaration)node, selfAddr);
			return BValue.bottom();
		}
		else if(node instanceof Name) {
			return store.apply(env.apply(node.toSource()));
		}
		else if(node instanceof FunctionCall) {
			return evalFunctionCall(env, store, scratch, trace, (FunctionCall)node, selfAddr);
		}
		else if(node instanceof Assignment) {
			evalAssignment(env, store, scratch, trace, (Assignment)node, selfAddr);
			return BValue.bottom();
		}
		else if(node instanceof KeywordLiteral) {
			return evalKeywordLiteral(env, store, scratch, trace, (KeywordLiteral)node, selfAddr);
		}
		else if(node instanceof StringLiteral) {
			return evalStringLiteral(env, store, scratch, trace, (StringLiteral)node, selfAddr);
		}
		else if(node instanceof NumberLiteral) {
			return evalNumberLiteral(env, store, scratch, trace, (NumberLiteral)node, selfAddr);
		}
		else if(node instanceof Name) {
			return evalName(env, store, scratch, trace, (Name)node, selfAddr);
		}
		else if(node instanceof InfixExpression) {
			return evalInfixExpression(env, store, scratch, trace, (InfixExpression)node, selfAddr);
		}

		/* We could not evaluate the expression. Return top. */
		return BValue.top();

	}

	/**
	 * Updates the store based on abstract interpretation of assignments.
	 * @param vd The variable declaration. Variables have already been
	 * lifted into the environment.
	 */
	public static void evalVariableDeclaration(Environment env, Store store, Scratchpad scratch,
												 Trace trace, VariableDeclaration vd, Address selfAddr) {
		for(VariableInitializer vi : vd.getVariables()) {
			if(vi.getInitializer() != null) {
				assignInterp(env, store, scratch, trace, selfAddr, vi.getTarget(), vi.getInitializer());
			}
		}
	}

	/**
	 * @param ie The infix expression.
	 * @return the abstract interpretation of the name
	 */
	public static BValue evalInfixExpression(Environment env, Store store, Scratchpad scratch,
									  	   Trace trace, InfixExpression ie, Address selfAddr) {
		/* We assume this is an identifier and attempt to dereference it, since
		 * no other operator is currently supported. */
		BValue val = Helpers.resolveValue(env, store, ie);
		if(val == null) return BValue.top();
		return val;
	}

	/**
	 * @param name
	 * @return the abstract interpretation of the name
	 */
	public static BValue evalName(Environment env, Store store, Scratchpad scratch,
									  	   Trace trace, Name name, Address selfAddr) {
		BValue val = Helpers.resolveValue(env, store, name);
		if(val == null) return BValue.top();
		return val;
	}

	/**
	 * @param numl
	 * @return the abstract interpretation of the number literal
	 */
	public static BValue evalNumberLiteral(Environment env, Store store, Scratchpad scratch,
									  	   Trace trace, NumberLiteral numl, Address selfAddr) {
		return Num.inject(Num.top());
	}

	/**
	 * @param strl The keyword literal.
	 * @return the abstract interpretation of the string literal
	 */
	public static BValue evalStringLiteral(Environment env, Store store, Scratchpad scratch,
									  	   Trace trace, StringLiteral strl, Address selfAddr) {

		Str str = null;
		String val = strl.getValue();
		if(NumberUtils.isNumber(val)) {
			str = new Str(LatticeElementType.SNUM, val);
		}
		else {
			str = new Str(LatticeElementType.SNOTNUMNORSPL, val);
		}

		return Str.inject(str);

	}

	/**
	 * @param kwl The keyword literal.
	 * @return the abstract interpretation of the keyword literal.
	 */
	public static BValue evalKeywordLiteral(Environment env, Store store, Scratchpad scratch,
									  	Trace trace, KeywordLiteral kwl, Address selfAddr) {
		switch(kwl.getType()) {
		case Token.THIS:
			return store.apply(selfAddr);
		case Token.NULL:
			return Null.inject(Null.top());
		case Token.TRUE:
		case Token.FALSE:
			return Bool.inject(Bool.top());
		case Token.DEBUGGER:
		default:
			return BValue.bottom();
		}
	}

	/**
	 * Updates the store based on abstract interpretation of assignments.
	 * @param a The assignment.
	 */
	public static void evalAssignment(Environment env, Store store, Scratchpad scratch,
									  Trace trace, Assignment a, Address selfAddr) {
		assignInterp(env, store, scratch, trace, selfAddr, a.getLeft(), a.getRight());
	}

	/**
	 * Helper function since variable initializers and assignments do the same thing.
	 */
	private static void assignInterp(Environment env, Store store, Scratchpad scratch,
								Trace trace, Address selfAddr, AstNode lhs,
								AstNode rhs) {

		/* Resolve the left hand side to a set of addresses. */
		Set<Address> addrs = Helpers.resolve(env, store, lhs);

		/* Resolve the right hand side to a value. */
		BValue val = eval(env, store, scratch, trace, rhs, selfAddr);

		/* Update the values in the store. */
		// TODO: Is this correct? We should probably only do a strong update if
		//		 there is only one address. Otherwise we don't know which one
		//		 to update.
		for(Address addr : addrs) {
			store = store.strongUpdate(addr, val);
		}

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
			BValue funVal = Helpers.resolveValue(env, store, fc.getTarget());
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
