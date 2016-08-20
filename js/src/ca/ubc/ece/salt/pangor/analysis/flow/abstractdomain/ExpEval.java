package ca.ubc.ece.salt.pangor.analysis.flow.abstractdomain;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

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
import org.mozilla.javascript.ast.UnaryExpression;

import ca.ubc.ece.salt.gumtree.ast.ClassifiedASTNode.ChangeType;
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
		else if(node instanceof UnaryExpression) {
			return evalUnaryExpression((UnaryExpression)node);
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
		else if(node instanceof FunctionCall) {
			State endState = this.evalFunctionCall((FunctionCall) node);
			this.state.store = endState.store;
			BValue retVal =  endState.scratch.apply(Scratch.RETVAL);
			if(retVal == null) {
				/* Functions with no return statement return undefined. */
				retVal = Undefined.inject(Undefined.top(Change.convU(node)), Change.u());
			}
			return retVal;
		}

		/* We could not evaluate the expression. Return top. */
		return BValue.top(Change.convU(node), Change.convU(node));

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
		return Address.inject(addr, Change.convU(f), Change.convU(f));
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
	 * @param ue The unary expression.
	 * @return the abstract interpretation of the expression.
	 */
	public BValue evalUnaryExpression(UnaryExpression ue) {

		BValue operand = this.eval(ue.getOperand());

		/* First create a bottom BValue with the proper change type. We will
		 * put in values later. */
		BValue val;
		if(operand.change.le == Change.LatticeElement.CHANGED
				|| operand.change.le == Change.LatticeElement.TOP) {
			val = BValue.bottom(Change.c(), Change.u());
		}
		else if(ue.getChangeType() == ChangeType.INSERTED
				|| ue.getChangeType() == ChangeType.REMOVED
				|| ue.getChangeType() == ChangeType.UPDATED) {
			val = BValue.bottom(Change.c(), Change.u());
		}
		else {
			val = BValue.bottom(Change.u(), Change.u());
		}

		/* For now, just do a basic conservative estimate of unary operator
		 * evaluations. */

		switch(ue.getType()) {
		case Token.NOT:
			val.booleanAD.le = Bool.LatticeElement.TOP;
			return val;
		case Token.INC:
		case Token.DEC:
			val.numberAD.le = Num.LatticeElement.TOP;
			return val;
		case Token.TYPEOF:
			val.stringAD.le = Str.LatticeElement.TOP;
			return val;
		default:
			val.undefinedAD.le = Undefined.LatticeElement.TOP;
			return val;
		}

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
		case Token.GETPROPNOWARN:
		case Token.GETPROP: {
			/* This is an identifier.. so we attempt to dereference it. */
			BValue val = resolveValue(ie);
			if(val == null) return BValue.top(Change.u(), Change.u());
			return val; }
		case Token.ADD:
			return evalPlus(ie);
		case Token.SUB:
		case Token.MUL:
		case Token.DIV:
		case Token.MOD:
			return evalMathOp(ie);
		case Token.ASSIGN_ADD: {
			BValue val = evalPlus(ie);
			state.interpretAssignment(ie.getLeft(), val);
			return this.eval(ie.getLeft()); }
		case Token.ASSIGN_SUB:
		case Token.ASSIGN_MUL:
		case Token.ASSIGN_DIV:
		case Token.ASSIGN_MOD: {
			BValue val = evalMathOp(ie);
			state.interpretAssignment(ie.getLeft(), val);
			return this.eval(ie.getLeft()); }
		default:
			return this.evalBinOp(ie);
		}

	}

	/**
	 * Evaluates an unknown binary operator on two BValues.
	 */
	public BValue evalBinOp(InfixExpression ie) {

		BValue left = this.eval(ie.getLeft());
		BValue right = this.eval(ie.getRight());

		/* First create a bottom BValue with the proper change type. We will
		 * put in values later. */
		BValue val;
		if(left.change.le == Change.LatticeElement.CHANGED
				|| left.change.le == Change.LatticeElement.TOP
				|| right.change.le == Change.LatticeElement.CHANGED
				|| right.change.le == Change.LatticeElement.TOP) {
			val = BValue.top(Change.c(), Change.u());
		}
		else if(ie.getChangeType() == ChangeType.INSERTED
				|| ie.getChangeType() == ChangeType.REMOVED
				|| ie.getChangeType() == ChangeType.UPDATED) {
			val = BValue.top(Change.c(), Change.u());
		}
		else {
			val = BValue.top(Change.u(), Change.u());
		}

		return val;

	}

	/**
	 * Evaluates the plus operation on two BValues.
	 *
	 * TODO: The value evaluations are not very precise right now, because
	 * for MultiDiff they don't need to be. We are more interested in the
	 * value's change type. When we start to deal with type changes again,
	 * the binary operator evaluations should be updated.
	 */
	public BValue evalPlus(InfixExpression ie) {

		BValue left = this.eval(ie.getLeft());
		BValue right = this.eval(ie.getRight());

		/* First create a bottom BValue with the proper change type. We will
		 * put in values later. */
		BValue plus;
		if(left.change.le == Change.LatticeElement.CHANGED
				|| left.change.le == Change.LatticeElement.TOP
				|| right.change.le == Change.LatticeElement.CHANGED
				|| right.change.le == Change.LatticeElement.TOP) {
			plus = BValue.bottom(Change.c(), Change.u());
		}
		else if(ie.getChangeType() == ChangeType.INSERTED
				|| ie.getChangeType() == ChangeType.REMOVED
				|| ie.getChangeType() == ChangeType.UPDATED) {
			plus = BValue.top(Change.c(), Change.u());
		}
		else {
			plus = BValue.bottom(Change.u(), Change.u());
		}

		/* For now, just do a basic conservative estimate of binary operator
		 * evaluations. */

		/* Strings. */
		if(left.stringAD.le != Str.LatticeElement.BOTTOM
				|| right.stringAD.le != Str.LatticeElement.BOTTOM) {
				plus.stringAD.le = Str.LatticeElement.TOP;
		}
		/* Numbers. */
		if(left.numberAD.le != Num.LatticeElement.BOTTOM
				|| right.numberAD.le != Num.LatticeElement.BOTTOM) {
			plus.numberAD.le = Num.LatticeElement.TOP;
		}
		/* Booleans and Nulls. */
		if((left.booleanAD.le != Bool.LatticeElement.BOTTOM
				|| left.nullAD.le == Null.LatticeElement.TOP)
				&& (right.booleanAD.le != Bool.LatticeElement.BOTTOM
				|| right.nullAD.le == Null.LatticeElement.TOP)) {
			plus.numberAD.le = Num.LatticeElement.TOP;
		}

		return plus;

	}

	/**
	 * Evaluates a math operation on two BValues.
	 *
	 * TODO: The value evaluations are not very precise right now, because
	 * for MultiDiff they don't need to be. We are more interested in the
	 * value's change type. When we start to deal with type changes again,
	 * the binary operator evaluations should be updated.
	 */
	public BValue evalMathOp(InfixExpression ie) {

		BValue left = this.eval(ie.getLeft());
		BValue right = this.eval(ie.getRight());

		/* First create a bottom BValue with the proper change type. We will
		 * put in values later. */
		BValue val;
		if(left.change.le == Change.LatticeElement.CHANGED
				|| left.change.le == Change.LatticeElement.TOP
				|| right.change.le == Change.LatticeElement.CHANGED
				|| right.change.le == Change.LatticeElement.TOP) {
			val = BValue.bottom(Change.c(), Change.u());
		}
		else if(ie.getChangeType() == ChangeType.INSERTED
				|| ie.getChangeType() == ChangeType.REMOVED
				|| ie.getChangeType() == ChangeType.UPDATED) {
			val = BValue.top(Change.c(), Change.u());
		}
		else {
			val = BValue.bottom(Change.u(), Change.u());
		}

		/* For now, just do a basic conservative estimate of binary operator
		 * evaluations. */

		val.numberAD.le = Num.LatticeElement.TOP;

		return val;

	}

	/**
	 * @param name
	 * @return the abstract interpretation of the name
	 */
	public BValue evalName(Name name) {
		BValue val = resolveValue(name);
		if(val == null) return BValue.top(Change.u(), Change.u());
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
		Change change = Change.convU(strl);
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
	 * @return The list of functions pointed to by the value.
	 */
	private List<Address> extractFunctions(BValue val) {

		List<Address> functionAddrs = new LinkedList<Address>();

		for(Address objAddr : val.addressAD.addresses) {
			Obj obj = state.store.getObj(objAddr);
			if(obj.internalProperties.klass == JSClass.CFunction) {
				InternalFunctionProperties ifp = (InternalFunctionProperties) obj.internalProperties;
				if(ifp.closure instanceof FunctionClosure) {
					functionAddrs.add(objAddr);
				}
			}
		}

		return functionAddrs;

	}

	/**
	 * Evaluate a function call expression to a BValue.
	 * @param fc The function call.
	 * @return The return value of the function call.
	 */
	public State evalFunctionCall(FunctionCall fc) {

		/* The state after the function call. */
		State newState = null;

		/* Keep track of callback functions. */
		List<Address> callbacks = new LinkedList<Address>();

		/* Create the argument object. */
		Map<Identifier, Address> ext = new HashMap<Identifier, Address>();
		int i = 0;
		for(AstNode arg : fc.getArguments()) {
			BValue argVal = eval(arg);
			callbacks.addAll(extractFunctions(argVal));
			state.store = Helpers.addProp(fc.getID(), String.valueOf(i), argVal,
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
		BValue funVal = resolveValue(fc.getTarget());
		BValue objVal = resolveSelf(fc.getTarget());

		/* If the function is not a member variable, it is local and we
		 * use the object of the currently executing function as self. */
		Address objAddr = state.trace.toAddr("this");
		if(objVal == null) objAddr = state.selfAddr;
		else state.store = state.store.alloc(objAddr, objVal);


		if(funVal != null) {
			/* If this is a new function call, we interpret the control of
			 * the callee as changed. */
			Control control = state.control;
			if(Change.convU(fc).le == Change.LatticeElement.CHANGED) {
				control = control.clone();
				control.conditions.add(fc); // All statements in the callee will be labeled control changed
			}

			/* Call the function and get a join of the new states. */
			newState = Helpers.applyClosure(funVal, objAddr, argAddr, state.store,
												  new Scratchpad(), state.trace, control,
												  state.callStack);
		}

		/* Get the call change type. */
		boolean callChange =
				Change.convU(fc).le == Change.LatticeElement.CHANGED
					|| Change.convU(fc).le == Change.LatticeElement.TOP
				? true : false;

		if(newState == null) {
			/* Because our analysis is not complete, the identifier may not point
			 * to any function object. In this case, we assume the (local) state
			 * is unchanged, but add BValue.TOP as the return value. */
			BValue value = callChange
					? BValue.top(Change.top(), Change.u())
					: BValue.top(Change.u(), Change.u());
			state.scratch = state.scratch.strongUpdate(Scratch.RETVAL, value);
			newState = new State(state.store, state.env, state.scratch,
								 state.trace, state.control, state.selfAddr,
								 state.cfgs, state.callStack);
		}
		else {

			/* This could be a new value if the call is new. */
			if(callChange)
				newState.scratch.apply(Scratch.RETVAL).change = Change.top();

		}

		/* Analyze any callbacks
		 * qhanam: Removed condition: "that were not analyzed within the callee". */
		for(Address addr : callbacks) {
			Obj funct = newState.store.getObj(addr);

			InternalFunctionProperties ifp = (InternalFunctionProperties)funct.internalProperties;
			FunctionClosure closure = (FunctionClosure)ifp.closure;

			/* Was the callback analyzed within the callee?
			 * qhanam: This check is not effective. It misses the case where a
			 * 		   function needs to be re-checked through a function call.
			 * 		   It is probably safe to omit the check in most cases. */
//			if(closure.cfg.getEntryNode().getBeforeState() == null) {

				/* Create the argument object. */
				argAddr = createTopArgObject((FunctionNode)closure.cfg.getEntryNode().getStatement());

				/* Create the control domain. */
				Control control = state.control;
				AstNode node = (AstNode)closure.cfg.getEntryNode().getStatement();
				if(Change.conv(node).le == Change.LatticeElement.CHANGED) {
					control = state.control.clone();
					control.conditions.add(node); // Mark all as control flow modified
				}

				/* Is this function being called recursively? If so abort. */
				if(state.callStack.contains(addr)) return state;

				/* Push this function onto the call stack. */
				state.callStack.push(addr);

				/* Analyze the function. */
				ifp.closure.run(state.selfAddr, argAddr, state.store,
								state.scratch, state.trace, control,
								state.callStack);

				/* Pop this function off the call stack. */
				state.callStack.pop();

//			}

		}

		return newState;

	}

	/**
	 * Resolves a variable to its addresses in the store.
	 * @return The addresses as a BValue.
	 */
	public BValue resolveValue(AstNode node) {
		BValue value = null;
		Set<Address> addrs = state.resolveOrCreate(node);
		if(addrs == null) return null;
		for(Address addr : addrs) {
			if(value == null) value = state.store.apply(addr);
			else value = value.join(state.store.apply(addr));
		}
		return value;
	}

	/**
	 * Creates an arg object where each argument corresponds to a parameter
	 * and each argument value is BValue.TOP.
	 * @param state
	 * @param f The function
	 * @return
	 */
	private Address createTopArgObject(FunctionNode f) {

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

	/**
	 * Resolves a function's parent object.
	 * @return The parent object (this) and the function object.
	 */
	public BValue resolveSelf(AstNode node) {
		if(node instanceof Name) {
			/* This is a variable name, not a field. */
			return null;
		}
		else if(node instanceof InfixExpression) {
			/* We have a qualified name. Recursively find the addresses. */
			InfixExpression ie = (InfixExpression) node;
			Set<Address> addrs = state.resolveOrCreate(ie.getLeft());
			if(addrs == null) return null;

			/* Resolve all the objects in the address list and create a new
			 * BValue which points to those objects. */
			Addresses selfAddrs = new Addresses(Addresses.LatticeElement.SET, Change.u());
			for(Address addr : addrs) {
				BValue val = this.state.store.apply(addr);
				for(Address objAddr : val.addressAD.addresses) {
					Obj obj = this.state.store.getObj(objAddr);
					if(obj != null) selfAddrs.addresses.add(objAddr);
				}
			}
			if(selfAddrs.addresses.isEmpty()) return BValue.bottom(Change.u(), Change.u());

			return Addresses.inject(selfAddrs, Change.u());
		}
		else {
			/* Ignore everything else (e.g., method calls) for now. */
			return null;
		}
	}

}