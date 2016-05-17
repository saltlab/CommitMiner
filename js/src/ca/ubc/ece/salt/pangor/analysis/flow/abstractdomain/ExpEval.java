package ca.ubc.ece.salt.pangor.analysis.flow.abstractdomain;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.math.NumberUtils;
import org.mozilla.javascript.Token;
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
import ca.ubc.ece.salt.pangor.analysis.flow.trace.Trace;
import ca.ubc.ece.salt.pangor.cfg.CFG;

public class ExpEval {

	public Environment env;
	public Store store;
	public Scratchpad scratch;
	public Trace trace;
	public Address selfAddr;
	public Map<AstNode, CFG> cfgs;

	public ExpEval(Environment env, Store store, Scratchpad scratch,
				   Trace trace, Address selfAddr, Map<AstNode, CFG> cfgs) {
		this.env = env;
		this.store = store;
		this.scratch = scratch;
		this.trace = trace;
		this.selfAddr = selfAddr;
		this.cfgs = cfgs;
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
		return BValue.top();

	}

	/**
	 * Creates a new function from a function definition.
	 * @param f The function definition.
	 * @return A BValue that points to the new function object.
	 */
	public BValue evalFunctionNode(FunctionNode f){
		Closure closure = new FunctionClosure(cfgs.get(f), env, cfgs);
		Address addr = trace.makeAddr(f.getID(), "");
		store = Helpers.createFunctionObj(closure, store, trace, addr, f.getID());
		return Address.inject(addr);
	}

	/**
	 * Creates a new object from an object literal.
	 * @param ol The object literal.
	 * @return A BValue that points to the new object literal.
	 */
	public BValue evalObjectLiteral(ObjectLiteral ol) {
		Map<String, Address> ext = new HashMap<String, Address>();
		InternalObjectProperties in = new InternalObjectProperties();

		for(ObjectProperty property : ol.getElements()) {
			AstNode prop = property.getLeft();
			String propName = null;
			if(prop instanceof Name) propName = prop.toSource();
			else if(prop instanceof StringLiteral) propName = ((StringLiteral)prop).getValue();
			else if(prop instanceof NumberLiteral) propName = ((NumberLiteral)prop).getValue();
			BValue propVal = this.eval(property.getRight());
			Address propAddr = trace.makeAddr(property.getID(), "");
			store = store.alloc(propAddr, propVal);
			if(propName != null) ext.put(propName, propAddr);
		}

		Obj obj = new Obj(ext, in, ext.keySet());
		Address objAddr = trace.makeAddr(ol.getID(), "");
		store = store.alloc(objAddr, obj);

		return Address.inject(objAddr);
	}

	/**
	 * @param ie The infix expression.
	 * @return the abstract interpretation of the name
	 */
	public BValue evalInfixExpression(InfixExpression ie) {
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
	public BValue evalName(Name name) {
		BValue val = Helpers.resolveValue(env, store, name);
		if(val == null) return BValue.top();
		return val;
	}

	/**
	 * @param numl
	 * @return the abstract interpretation of the number literal
	 */
	public BValue evalNumberLiteral(NumberLiteral numl) {
		return Num.inject(Num.top());
	}

	/**
	 * @param strl The keyword literal.
	 * @return the abstract interpretation of the string literal
	 */
	public BValue evalStringLiteral(StringLiteral strl) {

		Str str = null;
		String val = strl.getValue();
		if(NumberUtils.isNumber(val)) {
			str = new Str(Str.LatticeElement.SNUM, val);
		}
		else {
			str = new Str(Str.LatticeElement.SNOTNUMNORSPL, val);
		}

		return Str.inject(str);

	}

	/**
	 * @param kwl The keyword literal.
	 * @return the abstract interpretation of the keyword literal.
	 */
	public BValue evalKeywordLiteral(KeywordLiteral kwl) {
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
	 * Evaluate a function call expression to a BValue.
	 * @param fc The function call.
	 * @return The return value of the function call.
	 */
	public State evalFunctionCall(FunctionCall fc) {

			/* Create the argument object. */
			Map<String, Address> ext = new HashMap<String, Address>();
			int i = 0;
			for(AstNode arg : fc.getArguments()) {
				BValue argVal = eval(arg);
				store = Helpers.addProp(arg.getID(), String.valueOf(i), argVal,
								ext, store, trace);
				i++;
			}

			InternalObjectProperties internal = new InternalObjectProperties(
					Address.inject(StoreFactory.Arguments_Addr), JSClass.CFunction);
			Obj argObj = new Obj(ext, internal, ext.keySet());

			/* Add the argument object to the store. */
			Address argAddr = trace.makeAddr(fc.getID(), "");
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
				scratch = scratch.strongUpdate(Scratch.RETVAL, BValue.top());
				return new State(store, env, scratch, trace, cfgs);
			}
			else {
				/* Call the function and get a join of the new states. */
				return Helpers.applyClosure(funVal, objAddr, argAddr, store,
													  scratch, trace);
			}

	}

}
