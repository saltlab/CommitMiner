package ca.ubc.ece.salt.pangor.analysis.flow.abstractdomain;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.math.NumberUtils;
import org.mozilla.javascript.Token;
import org.mozilla.javascript.ast.Assignment;
import org.mozilla.javascript.ast.AstNode;
import org.mozilla.javascript.ast.FunctionCall;
import org.mozilla.javascript.ast.FunctionNode;
import org.mozilla.javascript.ast.InfixExpression;
import org.mozilla.javascript.ast.KeywordLiteral;
import org.mozilla.javascript.ast.Name;
import org.mozilla.javascript.ast.NumberLiteral;
import org.mozilla.javascript.ast.ObjectLiteral;
import org.mozilla.javascript.ast.ObjectProperty;
import org.mozilla.javascript.ast.StringLiteral;

import ca.ubc.ece.salt.pangor.analysis.flow.abstractdomain.Scratchpad.Scratch;
import ca.ubc.ece.salt.pangor.analysis.flow.factories.StoreFactory;

public class ExpEval {

	public State state;

	public ExpEval(State state) {
		this.state = state;
	}

	/**
	 * Evaluate an expression to a BValue.
	 * @param node The expression to evaluate.
	 * @return The value of the expression.
	 */
	public BValue eval(AstNode node) {

		if(node instanceof Name) {
			return evalName((Name)node);
		}
		else if(node instanceof InfixExpression) {
			return evalInfixExpression((InfixExpression)node);
		}
		else if(node instanceof KeywordLiteral) {
			return evalKeywordLiteral((KeywordLiteral)node);
		}
		else if(node instanceof StringLiteral) {
			return evalStringLiteral((StringLiteral)node);
		}
		else if(node instanceof NumberLiteral) {
			return evalNumberLiteral((NumberLiteral)node);
		}
		else if(node instanceof ObjectLiteral) {
			return evalObjectLiteral((ObjectLiteral)node);
		}
		else if(node instanceof FunctionNode) {
			return evalFunctionNode((FunctionNode)node);
		}

		/* We could not evaluate the expression. Return top. */
		return BValue.top(Change.convU(node), Change.convU(node)); // TODO: The type may not actually have changed. Need to check the old BValue somehow.

	}

	/**
	 * Creates a new function from a function definition.
	 * @param f The function definition.
	 * @return A BValue that points to the new function object.
	 */
	public BValue evalFunctionNode(FunctionNode f){
		Closure closure = new FunctionClosure(state.cfgs.get(f), state.env, state.cfgs);
		Address addr = state.trace.makeAddr(f.getID(), "");
		state.store = Helpers.createFunctionObj(closure, state.store, state.trace, addr, f.getID());
		return Address.inject(addr, Change.convU(f), Change.convU(f)); // TODO: The type may not actually have changed. Need to check the old BValue somehow.
	}

	/**
	 * Creates a new object from an object literal.
	 * @param ol The object literal.
	 * @return A BValue that points to the new object literal.
	 */
	public BValue evalObjectLiteral(ObjectLiteral ol) {
		Map<Identifier, Address> ext = new HashMap<Identifier, Address>();
		InternalObjectProperties in = new InternalObjectProperties();

		for(ObjectProperty property : ol.getElements()) {
			AstNode prop = property.getLeft();
			String propName = null;
			if(prop instanceof Name) propName = prop.toSource();
			else if(prop instanceof StringLiteral) propName = ((StringLiteral)prop).getValue();
			else if(prop instanceof NumberLiteral) propName = ((NumberLiteral)prop).getValue();
			BValue propVal = this.eval(property.getRight());
			Address propAddr = state.trace.makeAddr(property.getID(), "");
			state.store = state.store.alloc(propAddr, propVal);
			if(propName != null) ext.put(new Identifier(propName, Change.u()), propAddr);
		}

		Obj obj = new Obj(ext, in);
		Address objAddr = state.trace.makeAddr(ol.getID(), "");
		state.store = state.store.alloc(objAddr, obj);

		return Address.inject(objAddr, Change.convU(ol), Change.convU(ol)); // TODO: The type may not actually have changed. Need to check the old BValue somehow.
	}

	/**
	 * @param ie The infix expression.
	 * @return the abstract interpretation of the name
	 */
	public BValue evalInfixExpression(InfixExpression ie) {

		/* If this is an assignment, we need to interpret it through state. */
		switch(ie.getType()) {
		case Token.ASSIGN:
			/* We need to interpret this assignment and propagate the value
			 * left. */
			state.interpretAssignment((Assignment)ie);
			return this.eval(ie.getLeft());
		case Token.DOT:
		default:
			/* This is an identifier.. so we attempt to dereference it. */
			BValue val = Helpers.resolveValue(state.env, state.store, ie);
			if(val == null) return BValue.top(Change.convU(ie), Change.convU(ie)); // TODO: The type may not actually have changed. Need to check the old BValue somehow.
			return val;
		}

	}

	/**
	 * @param name
	 * @return the abstract interpretation of the name
	 */
	public BValue evalName(Name name) {
		BValue val = Helpers.resolveValue(state.env, state.store, name);
		if(val == null) return BValue.top(Change.convU(name), Change.convU(name)); // TODO: The type may not actually have changed. Need to check the old BValue somehow.
		return val;
	}

	/**
	 * @param numl
	 * @return the abstract interpretation of the number literal
	 */
	public BValue evalNumberLiteral(NumberLiteral numl) {
		return Num.inject(new Num(Num.LatticeElement.VAL, numl.getValue(), Change.convU(numl)), Change.convU(numl)); // TODO: The type may not actually have changed. Need to check the old BValue somehow.
	}

	/**
	 * @param strl The keyword literal.
	 * @return the abstract interpretation of the string literal
	 */
	public BValue evalStringLiteral(StringLiteral strl) {

		Str str = null;
		String val = strl.getValue();
		Change change = Change.conv(strl);
		if(val.equals("")) str = new Str(Str.LatticeElement.SBLANK, change);
		else if(NumberUtils.isNumber(val)) {
			str = new Str(Str.LatticeElement.SNUMVAL, val, change);
		}
		else {
			str = new Str(Str.LatticeElement.SNOTNUMNORSPLVAL, val, change);
		}

		return Str.inject(str, change);

	}

	/**
	 * @param kwl The keyword literal.
	 * @return the abstract interpretation of the keyword literal.
	 */
	public BValue evalKeywordLiteral(KeywordLiteral kwl) {
		Change change = Change.conv(kwl);
		switch(kwl.getType()) {
		case Token.THIS:
			return state.store.apply(state.selfAddr);
		case Token.NULL:
			return Null.inject(Null.top(change), change);
		case Token.TRUE:
			return Bool.inject(new Bool(Bool.LatticeElement.TRUE, change), change);
		case Token.FALSE:
			return Bool.inject(new Bool(Bool.LatticeElement.FALSE, change), change);
		case Token.DEBUGGER:
		default:
			return BValue.bottom(change, change);
		}
	}

	/**
	 * Evaluate a function call expression to a BValue.
	 * @param fc The function call.
	 * @return The return value of the function call.
	 */
	public State evalFunctionCall(FunctionCall fc) {

		/* Create the argument object. */
		Map<Identifier, Address> ext = new HashMap<Identifier, Address>();
		int i = 0;
		for(AstNode arg : fc.getArguments()) {
			BValue argVal = eval(arg);
			state.store = Helpers.addProp(arg.getID(), String.valueOf(i), argVal,
							ext, state.store, state.trace);
			i++;
		}

		InternalObjectProperties internal = new InternalObjectProperties(
				Address.inject(StoreFactory.Arguments_Addr, Change.u(), Change.u()), JSClass.CFunction);
		Obj argObj = new Obj(ext, internal);

		/* Add the argument object to the state.store. */
		Address argAddr = state.trace.makeAddr(fc.getID(), "");
		state.store = state.store.alloc(argAddr, argObj);

		/* Attempt to resolve the function and it's parent object. */
		BValue funVal = Helpers.resolveValue(state.env, state.store, fc.getTarget());
		BValue objVal = Helpers.resolveSelf(state.env, state.store, fc.getTarget());

		/* If the function is not a member variable, it is local and we
		 * use the object of the currently executing function as self. */
		Address objAddr = state.trace.toAddr("this");
		if(objVal == null) objAddr = state.selfAddr;
		else state.store = state.store.alloc(objAddr, objVal);

		if(funVal == null) {
			/* If the function was not resolved, we assume the (local)
			 * state is unchanged, but add BValue.TOP as the return value. */
			state.scratch = state.scratch.strongUpdate(Scratch.RETVAL, BValue.top(Change.top(), Change.top()));
			return new State(state.store, state.env, state.scratch, state.trace, state.control, state.selfAddr, state.cfgs);
		}
		else {
			/* Call the function and get a join of the new states. */
			return Helpers.applyClosure(funVal, objAddr, argAddr, state.store,
												  state.scratch, state.trace, state.control);
		}

	}

}