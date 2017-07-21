package commitminer.analysis.flow.abstractdomain;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.math.NumberUtils;
import org.mozilla.javascript.Token;
import org.mozilla.javascript.ast.ArrayLiteral;
import org.mozilla.javascript.ast.Assignment;
import org.mozilla.javascript.ast.AstNode;
import org.mozilla.javascript.ast.ElementGet;
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

public class ExpEval {
	
	private static int ctr = 1;

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
		else if(node instanceof ElementGet) {
			return evalElementGet((ElementGet)node);
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
		else if(node instanceof ArrayLiteral) {
			return evalArrayLiteral((ArrayLiteral)node);
		}
		else if(node instanceof FunctionNode) {
			return evalFunctionNode((FunctionNode)node);
		}
		else if(node instanceof FunctionCall) {
			State endState = this.evalFunctionCall((FunctionCall) node);
			this.state.store = endState.store;
//			this.state.store = Helpers.gc(this.state.env, this.state.store); TODO: Helpers.gc needs to account for all environments in the call stack
 
			return endState.scratch.applyReturn();
		}

		/* We could not evaluate the expression. Return top. */
		return BValue.top(Change.convU(node), Change.convU(node), Change.convU(node));

	}

	/**
	 * @param eg The element get expression.
	 * @return the value of the element.
	 */
	private BValue evalElementGet(ElementGet eg) {
		/* This is an identifier.. so we attempt to dereference it. */
		BValue val = resolveValue(eg);
		if(val == null) return BValue.top(Change.u(), Change.u(), Change.u());
		return val; 
	}

	/**
	 * Creates a new function from a function definition.
	 * @param f The function definition.
	 * @return A BValue that points to the new function object.
	 */
	public BValue evalFunctionNode(FunctionNode f){
		Closure closure = new FunctionClosure(state.cfgs.get(f), state.env, state.cfgs);
		Address addr = state.trace.makeAddr(f.getID(), "");
		addr = state.trace.modAddr(addr, JSClass.CFunction);
		state.store = Helpers.createFunctionObj(closure, state.store, state.trace, addr, f);
		return Address.inject(addr, Change.convU(f), Change.convU(f), Change.convU(f), DefinerIDs.inject(f.getID()));
	}

	/**
	 * Creates a new object from an object literal.
	 * @param ol The object literal.
	 * @return A BValue that points to the new object literal.
	 */
	public BValue evalObjectLiteral(ObjectLiteral ol) {
		Map<String, Property> ext = new HashMap<String, Property>();
		InternalObjectProperties in = new InternalObjectProperties();

		for(ObjectProperty property : ol.getElements()) {
			AstNode prop = property.getLeft();
			String propName = "~unknown~";
			if(prop instanceof Name) propName = prop.toSource();
			else if(prop instanceof StringLiteral) propName = ((StringLiteral)prop).getValue();
			else if(prop instanceof NumberLiteral) propName = ((NumberLiteral)prop).getValue();
			BValue propVal = this.eval(property.getRight());
			Address propAddr = state.trace.makeAddr(property.getID(), propName);
			state.store = state.store.alloc(propAddr, propVal);
			if(propName != null) ext.put(propName, new Property(property.getID(), propName, Change.u(), propAddr));
		}

		Obj obj = new Obj(ext, in);
		Address objAddr = state.trace.makeAddr(ol.getID(), "");
		state.store = state.store.alloc(objAddr, obj);

		return Address.inject(objAddr, Change.convU(ol), Change.convU(ol), Change.convU(ol), DefinerIDs.inject(ol.getID()));
	}
	
	/**
	 * Creates a new array from an array literal.
	 * @param al The array literal.
	 * @return A BValue that points to the new array literal.
	 */
	public BValue evalArrayLiteral(ArrayLiteral al) {

		Map<String, Property> ext = new HashMap<String, Property>();
		InternalObjectProperties in = new InternalObjectProperties();

		Integer i = 0;
		for(AstNode element : al.getElements()) {
			BValue propVal = this.eval(element);
			Address propAddr = state.trace.makeAddr(element.getID(), "");
			state.store = state.store.alloc(propAddr, propVal);
			ext.put(i.toString(), new Property(element.getID(), i.toString(), Change.u(), propAddr));
			i++;
		}

		Obj obj = new Obj(ext, in);
		Address objAddr = state.trace.makeAddr(al.getID(), "");
		state.store = state.store.alloc(objAddr, obj);

		return Address.inject(objAddr, Change.convU(al), Change.convU(al), Change.convU(al), DefinerIDs.inject(al.getID()));
		
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
			val = BValue.bottom(Change.c(), Change.c(), Change.u());
		}
		else if(ue.getChangeType() == ChangeType.INSERTED
				|| ue.getChangeType() == ChangeType.REMOVED
				|| ue.getChangeType() == ChangeType.UPDATED) {
			val = BValue.bottom(Change.c(), Change.c(), Change.u());
		}
		else {
			val = BValue.bottom(Change.u(), Change.u(), Change.u());
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
			if(val == null) return BValue.top(Change.u(), Change.u(), Change.u());
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
			val = BValue.top(Change.c(), Change.c(), Change.u());
		}
		else if(ie.getChangeType() == ChangeType.INSERTED
				|| ie.getChangeType() == ChangeType.REMOVED
				|| ie.getChangeType() == ChangeType.UPDATED) {
			val = BValue.top(Change.c(), Change.c(), Change.u());
		}
		else {
			val = BValue.top(Change.u(), Change.c(), Change.u());
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
			plus = BValue.bottom(Change.c(), Change.c(), Change.u());
		}
		else if(ie.getChangeType() == ChangeType.INSERTED
				|| ie.getChangeType() == ChangeType.REMOVED
				|| ie.getChangeType() == ChangeType.UPDATED) {
			plus = BValue.top(Change.c(), Change.c(), Change.u());
		}
		else {
			plus = BValue.bottom(Change.u(), Change.c(), Change.u());
		}

		/* Assign a definer ID to track this new value. */
		ie.setDummy();
		plus.definerIDs = plus.definerIDs.join(DefinerIDs.inject(ie.getID()));

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
			val = BValue.bottom(Change.c(), Change.c(), Change.u());
		}
		else if(ie.getChangeType() == ChangeType.INSERTED
				|| ie.getChangeType() == ChangeType.REMOVED
				|| ie.getChangeType() == ChangeType.UPDATED) {
			val = BValue.top(Change.c(), Change.c(), Change.u());
		}
		else {
			val = BValue.bottom(Change.u(), Change.u(), Change.u());
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
		if(val == null) return BValue.top(Change.u(), Change.u(), Change.u());
		return val;
	}

	/**
	 * @param numl
	 * @return the abstract interpretation of the number literal
	 */
	public BValue evalNumberLiteral(NumberLiteral numl) {
		return Num.inject(new Num(Num.LatticeElement.VAL, numl.getValue(), Change.convU(numl)), Change.convU(numl), Change.convU(numl), DefinerIDs.inject(numl.getID()));
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

		return Str.inject(str, change, change, DefinerIDs.inject(strl.getID()));

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
			return Null.inject(Null.top(change), change, change, DefinerIDs.inject(kwl.getID()));
		case Token.TRUE:
			return Bool.inject(new Bool(Bool.LatticeElement.TRUE, change), change, change, DefinerIDs.inject(kwl.getID()));
		case Token.FALSE:
			return Bool.inject(new Bool(Bool.LatticeElement.FALSE, change), change, change, DefinerIDs.inject(kwl.getID()));
		case Token.DEBUGGER:
		default:
			return BValue.bottom(change, change, change);
		}
	}

	/**
	 * @return The list of functions pointed to by the value.
	 */
	private List<Address> extractFunctions(BValue val, List<Address> functionAddrs, Set<Address> visited) {

		for(Address objAddr : val.addressAD.addresses) {
			Obj obj = state.store.getObj(objAddr);

			if(obj.internalProperties.klass == JSClass.CFunction) {
				InternalFunctionProperties ifp = (InternalFunctionProperties) obj.internalProperties;
				if(ifp.closure instanceof FunctionClosure) {
					functionAddrs.add(objAddr);
				}
			}

			/* Recursively look for object properties that are functions. */
			for(Property property : obj.externalProperties.values()) {
				
				/* Avoid circular references. */
				if(visited.contains(property.address)) continue;
				visited.add(property.address);

				extractFunctions(state.store.apply(property.address), functionAddrs, visited);
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
		
		if(fc.getTarget().toSource().equals("foo")) {
			System.out.println("foo: " + ctr);
			ctr++;
		}
		
		/* The state after the function call. */
		State newState = null;

		/* Keep track of callback functions. */
		// TODO: We shouldn't need to do this... they are reachable in Helpers.analyzeEnvReachable
		List<Address> callbacks = new LinkedList<Address>();

		/* Create the argument values. */
		BValue[] args = new BValue[fc.getArguments().size()];
		int i = 0;
		for(AstNode arg : fc.getArguments()) {

			/* Get the value of the object. It could be a function, object literal, etc. */
			BValue argVal = eval(arg);

			if(arg instanceof ObjectLiteral) {
				/* If this is an object literal, make a fake var in the
				 * environment and point it to the object literal. */
				Address address = state.trace.makeAddr(arg.getID(), "");
				state.env.strongUpdateNoCopy(arg.getID().toString(), new Variable(arg.getID(), arg.getID().toString(), new Addresses(address, Change.u())));
				state.store = state.store.alloc(address, argVal);
			}
			
			args[i] = argVal;
			callbacks.addAll(extractFunctions(argVal, new LinkedList<Address>(), new HashSet<Address>()));
			i++;

		}
		
		Scratchpad scratch = new Scratchpad(null, args);

		/* Attempt to resolve the function and its parent object. */
		BValue funVal = resolveValue(fc.getTarget());
		BValue objVal = resolveSelf(fc.getTarget());

		/* If the function is not a member variable, it is local and we
		 * use the object of the currently executing function as self. */
		Address objAddr = state.trace.toAddr("this");
		if(objVal == null) objAddr = state.selfAddr;
		else state.store = state.store.alloc(objAddr, objVal);


		if(funVal != null) {

			/* Update the control-call domain for the function call. */
			Control control = state.control;
			control = control.update(fc);

			/* Call the function and get a join of the new states. */
			newState = Helpers.applyClosure(funVal, objAddr, state.store,
												  scratch, state.trace, control,
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
					? BValue.top(Change.top(), Change.top(), Change.u())
					: BValue.top(Change.u(), Change.u(), Change.u());
			state.scratch = state.scratch.strongUpdate(value, null);
			newState = new State(state.store, state.env, state.scratch,
								 state.trace, state.control, state.selfAddr,
								 state.cfgs, state.callStack);
			
			/* Create the return value. */
			BValue retVal =  BValue.top(Change.convU(fc), Change.convU(fc), Change.u());
			
			newState.scratch = newState.scratch.strongUpdate(retVal, null);
		}
		else {

			BValue retVal =  newState.scratch.applyReturn();
			if(retVal == null) {
				/* Functions with no return statement return undefined. */
				retVal = Undefined.inject(Undefined.top(Change.convU(fc)), Change.u(), Change.u(), DefinerIDs.bottom());
				newState.scratch = newState.scratch.strongUpdate(retVal, null);
			}

			/* This could be a new value if the call is new. */
			if(callChange) {
				newState.scratch.applyReturn().change = Change.top();
			}

		}

		/* Analyze any callbacks
		 * qhanam: Removed condition: "that were not analyzed within the callee". */
		for(Address addr : callbacks) {
			Obj funct = newState.store.getObj(addr);

			InternalFunctionProperties ifp = (InternalFunctionProperties)funct.internalProperties;

			/* Was the callback analyzed within the callee?
			 * qhanam: This check is not effective. It misses the case where a
			 * 		   function needs to be re-checked through a function call.
			 * 		   It is probably safe to omit the check in most cases. */
//			if(closure.cfg.getEntryNode().getBeforeState() == null) {

				/* Create the argument object. */
				scratch = new Scratchpad(null, new BValue[0]);

				/* Create the control domain. */
				Control control = new Control();

				/* Is this function being called recursively? If so abort. */
				if(state.callStack.contains(addr)) continue;

				/* Push this function onto the call stack. */
				state.callStack.push(addr);

				/* Analyze the function. */
				ifp.closure.run(state.selfAddr, state.store,
								scratch, state.trace, control,
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
		
		/* Resolve the identifier. */
		Set<Address> addrs = state.resolveOrCreate(node);
		if(addrs == null) return null;
		for(Address addr : addrs) {
			if(value == null) value = state.store.apply(addr);
			else value = value.join(state.store.apply(addr));
		}
		
		/* Track data dependencies for modified values. */
		if(Change.convU(node).le == Change.LatticeElement.CHANGED) {
			value.dependent = Change.convU(node);
			value.definerIDs = value.definerIDs.join(DefinerIDs.inject(node.getID()));
		}
		
		return value;
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
			DefinerIDs definerIDs =  DefinerIDs.bottom();
			for(Address addr : addrs) {
				BValue val = this.state.store.apply(addr);
				for(Address objAddr : val.addressAD.addresses) {
					Obj obj = this.state.store.getObj(objAddr);
					if(obj != null) {
						selfAddrs.addresses.add(objAddr);
						definerIDs = definerIDs.join(val.definerIDs);
					}
				}
			}
			if(selfAddrs.addresses.isEmpty()) return BValue.bottom(Change.u(), Change.u(), Change.u());

			return Addresses.inject(selfAddrs, Change.u(), Change.u(), definerIDs);
		}
		else {
			/* Ignore everything else (e.g., method calls) for now. */
			return null;
		}
	}

}