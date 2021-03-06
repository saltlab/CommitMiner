package commitminer.analysis.flow.abstractdomain;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.Stack;

import org.mozilla.javascript.Token;
import org.mozilla.javascript.ast.Assignment;
import org.mozilla.javascript.ast.AstNode;
import org.mozilla.javascript.ast.ElementGet;
import org.mozilla.javascript.ast.EmptyStatement;
import org.mozilla.javascript.ast.ExpressionStatement;
import org.mozilla.javascript.ast.FunctionCall;
import org.mozilla.javascript.ast.InfixExpression;
import org.mozilla.javascript.ast.Name;
import org.mozilla.javascript.ast.ParenthesizedExpression;
import org.mozilla.javascript.ast.PropertyGet;
import org.mozilla.javascript.ast.ReturnStatement;
import org.mozilla.javascript.ast.UnaryExpression;
import org.mozilla.javascript.ast.VariableDeclaration;
import org.mozilla.javascript.ast.VariableInitializer;

import commitminer.analysis.flow.IState;
import commitminer.analysis.flow.trace.Trace;
import commitminer.cfg.CFG;
import commitminer.cfg.CFGEdge;
import commitminer.cfg.CFGNode;

/**
 * Stores the state of the function analysis at a point in the CFG.
 */
public class State implements IState {
	
	/* The abstract domains that make up the program state. The abstract
	 * domains have access to each other. */
	
	private static int i = 1;

	public Environment env;
	public Store store;
	public Scratchpad scratch;
	public Trace trace;
	public Control control;

	public Queue<Address> declaredFunctions;

	public Address selfAddr;

	/** Maintain the call stack to prevent recursive calls. **/
	public Stack<Address> callStack;

	/** A reference to the list of CFGs to use for executing methods. **/
	public Map<AstNode, CFG> cfgs;

	/**
	 * Create a new state after a transfer or join.
	 * @param store The abstract store of the new state.
	 * @param environment The abstract environment of the new state.
	 */
	public State(Store store, Environment environment, Scratchpad scratchpad,
				 Trace trace, Control control, Address selfAddr,
				 Map<AstNode, CFG> cfgs, Stack<Address> callStack) {
		this.store = store;
		this.env = environment;
		this.scratch = scratchpad;
		this.trace = trace;
		this.control = control;
		this.selfAddr = selfAddr;
		this.cfgs = cfgs;
		this.callStack = callStack;
	}

	@Override
	public State clone() {
		State clone =  new State(store.clone(), env.clone(), scratch.clone(), trace,
						 control.clone(), selfAddr, cfgs, callStack);
		return clone;
	}

	public State transfer(CFGEdge edge) {

		/* Update the trace to the current condition. */
		this.trace = this.trace.update(edge.getId());

		/* The condition to transfer over. */
		AstNode condition = (AstNode)edge.getCondition();

		/* Interpret the statement. */
		interpretCondition(condition, false);

		/* Interpret the effect of the edge on control flow. */
		this.control = this.control.update(edge, edge.getFrom());

		return this;

	}

	/**
	 * Performs an abstract interpretation on the condition.
	 */
	private void interpretCondition(AstNode condition, boolean not) {
		if(condition instanceof ParenthesizedExpression) {
			interpretCondition(((ParenthesizedExpression)condition).getExpression(), not);
		}
		else if(condition instanceof Name) {
			Set<Address> addrs = resolveOrCreate(condition);
			Change change = Change.conv(condition);
			if(not) interpretAddrsFalsey(addrs, change);
			else interpretAddrsTruthy(addrs, change);
		}
		else if(condition instanceof UnaryExpression &&
				((UnaryExpression) condition).getOperator() == Token.NOT) {
			UnaryExpression ue = (UnaryExpression) condition;
			interpretCondition(ue.getOperand(), !not);
		}
		else if(condition instanceof InfixExpression &&
				((InfixExpression)condition).getOperator() == Token.GETPROP) {
			Set<Address> addrs = resolveOrCreate(condition);
			Change change = Change.conv(condition);
			if(not) interpretAddrsFalsey(addrs, change);
			else interpretAddrsTruthy(addrs, change);
		}
		else if(condition instanceof InfixExpression) {
			InfixExpression ie = (InfixExpression)condition;
			switch(ie.getOperator()) {
			case Token.EQ:
				if(not) interpretNE(ie);
				else interpretEQ(ie);
			case Token.NE:
				if(not) interpretEQ(ie);
				else interpretNE(ie);
			case Token.SHEQ:
				if(not) interpretSHNE(ie);
				else interpretSHEQ(ie);
				break;
			case Token.SHNE:
				if(not) interpretSHEQ(ie);
				else interpretSHNE(ie);
				break;
			case Token.AND:
				interpretAnd(ie, not);
				break;
			case Token.OR:
				interpretOr(ie, not);
				break;
			}
		}
	}

	private void interpretAddrsFalsey(Set<Address> addrs, Change change) {

		/* Update the value(s) to be falsey. */
		for(Address addr : addrs) {

			/* Keep the BValue change LE (the value does not change). */
			BValue oldVal = store.apply(addr);

			BValue val = Undefined.inject(Undefined.top(change), oldVal.change, oldVal.dependent, oldVal.definerIDs)
					.join(Null.inject(Null.top(change), oldVal.change, oldVal.dependent, oldVal.definerIDs))
					.join(Str.inject(new Str(Str.LatticeElement.SBLANK, change), oldVal.change, oldVal.dependent, DefinerIDs.bottom()))
					.join(Num.inject(new Num(Num.LatticeElement.NAN_ZERO, change), oldVal.change, oldVal.dependent, DefinerIDs.bottom()))
					.join(Bool.inject(new Bool(Bool.LatticeElement.FALSE, change), oldVal.change, oldVal.dependent, DefinerIDs.bottom()));
			store.strongUpdate(addr, val);

		}

	}

	private void interpretAddrsTruthy(Set<Address> addrs, Change change) {

		/* Update the value(s) to be truthy. */
		for(Address addr : addrs) {
			BValue val = store.apply(addr);
			val.undefinedAD = Undefined.bottom(change);
			val.nullAD = Null.bottom(change);
			val.stringAD = new Str(Str.LatticeElement.SNOTBLANK, change);
			val.numberAD = new Num(Num.LatticeElement.NOT_ZERO_NOR_NAN, change);
		}

	}

	private void interpretNE(InfixExpression ie) {

		Set<Address> rhsAddrs = resolveOrCreate(ie.getRight());

		/* Get the value of the RHS. */
		BValue rhsVal = BValue.bottom(Change.bottom(), Change.bottom(), Change.bottom());
		for(Address rhsAddr : rhsAddrs) {
			rhsVal = rhsVal.join(this.store.apply(rhsAddr));
		}

		/* Update the value(s) on the LHS. */
		Change change = Change.conv(ie);
		Set<Address> lhsAddrs = resolveOrCreate(ie.getLeft());
		if(BValue.isUndefined(rhsVal) || BValue.isNull(rhsVal))
			for(Address lhsAddr : lhsAddrs) {
				BValue lhsVal = store.apply(lhsAddr);
				lhsVal.undefinedAD = Undefined.bottom(change);
				lhsVal.nullAD = Null.bottom(change);
			}
		if(BValue.isBlank(rhsVal) || BValue.isZero(rhsVal))
			for(Address lhsAddr : lhsAddrs) {
				BValue lhsVal = store.apply(lhsAddr);
				/* Make sure we don't decrease the precision of the LE. */
				if(!Str.notBlank(lhsVal.stringAD))
					lhsVal.stringAD = new Str(Str.LatticeElement.SNOTBLANK, change);
				if(!Num.notZero(lhsVal.numberAD))
					lhsVal.numberAD = new Num(Num.LatticeElement.NOT_ZERO, change);
			}
		if(BValue.isNaN(rhsVal))
			for(Address lhsAddr : lhsAddrs) {
				BValue lhsVal = store.apply(lhsAddr);
				if(!Num.notNaN(lhsVal.numberAD))
					lhsVal.numberAD = new Num(Num.LatticeElement.NOT_NAN, change);
			}
		if(BValue.isFalse(rhsVal))
			interpretAddrsTruthy(lhsAddrs, change);
		if(BValue.isAddress(rhsVal))
			for(Address lhsAddr : lhsAddrs) {
				BValue lhsVal = store.apply(lhsAddr);
				lhsVal.addressAD.addresses.removeAll(rhsVal.addressAD.addresses);
				lhsVal.addressAD.change = change;
			}

	}

	private void interpretEQ(InfixExpression ie) {

		/* Get the value of the RHS. */
		Set<Address> rhsAddrs = resolveOrCreate(ie.getRight());
		BValue rhsVal = BValue.bottom(Change.bottom(), Change.bottom(), Change.bottom());
		for(Address rhsAddr : rhsAddrs) {
			rhsVal = rhsVal.join(this.store.apply(rhsAddr));
		}

		/* Update the value(s) on the LHS. */
		Change change = Change.conv(ie);
		Set<Address>lhsAddrs = resolveOrCreate(ie.getLeft());
		if(BValue.isUndefined(rhsVal) || BValue.isNull(rhsVal))
			for(Address lhsAddr : lhsAddrs) {
				BValue oldVal = store.apply(lhsAddr);
				rhsVal = Undefined.inject(Undefined.top(change), oldVal.change, oldVal.dependent, oldVal.definerIDs)
						.join(Null.inject(Null.top(change), oldVal.change, oldVal.dependent, oldVal.definerIDs));
				store.strongUpdate(lhsAddr, rhsVal);
			}
		if(BValue.isBlank(rhsVal) || BValue.isZero(rhsVal))
			for(Address lhsAddr : lhsAddrs) {
				BValue oldVal = store.apply(lhsAddr);
				rhsVal = Str.inject(new Str(Str.LatticeElement.SBLANK, change), oldVal.change, oldVal.dependent, oldVal.definerIDs)
						.join(Num.inject(new Num(Num.LatticeElement.ZERO, change), oldVal.change, oldVal.dependent, oldVal.definerIDs));
				store.strongUpdate(lhsAddr, rhsVal);
			}
		if(BValue.isNaN(rhsVal))
			for(Address lhsAddr : lhsAddrs) {
				BValue oldVal = store.apply(lhsAddr);
				store.strongUpdate(lhsAddr, Num.inject(new Num(Num.LatticeElement.NAN, change), oldVal.change, oldVal.dependent, oldVal.definerIDs));
			}
		if(BValue.isFalse(rhsVal))
			interpretAddrsFalsey(lhsAddrs, change);
		if(BValue.isAddress(rhsVal))
			for(Address lhsAddr : lhsAddrs) {
				BValue lhsVal = store.apply(lhsAddr);
				lhsVal.addressAD.addresses.retainAll(rhsVal.addressAD.addresses);
				lhsVal.addressAD.change = change;
			}

	}

	private void interpretSHEQ(InfixExpression ie) {

		/* Get the value of the RHS. */
		Set<Address> rhsAddrs = resolveOrCreate(ie.getRight());
		BValue rhsVal = BValue.bottom(Change.bottom(), Change.bottom(), Change.bottom());
		for(Address rhsAddr : rhsAddrs) {
			rhsVal = rhsVal.join(this.store.apply(rhsAddr));
		}

		/* Update the value(s) on the LHS. */
		Change change = Change.conv(ie);
		rhsVal.setChange(change);
		Set<Address> lhsAddrs = resolveOrCreate(ie.getLeft());
		for(Address lhsAddr : lhsAddrs) {
			store.strongUpdate(lhsAddr, rhsVal);
		}

	}

	private void interpretSHNE(InfixExpression ie) {

		Set<Address> rhsAddrs = resolveOrCreate(ie.getRight());

		/* Get the value of the RHS. */
		BValue rhsVal = BValue.bottom(Change.bottom(), Change.bottom(), Change.bottom());
		for(Address rhsAddr : rhsAddrs) {
			rhsVal = rhsVal.join(this.store.apply(rhsAddr));
		}

		/* Update the value(s) on the LHS. */
		Change change = Change.conv(ie);
		Set<Address> lhsAddrs = resolveOrCreate(ie.getLeft());
		if(BValue.isUndefined(rhsVal))
			for(Address lhsAddr : lhsAddrs)
				store.apply(lhsAddr).undefinedAD = Undefined.bottom(change);
		if(BValue.isNull(rhsVal))
			for(Address lhsAddr : lhsAddrs)
				store.apply(lhsAddr).nullAD = Null.bottom(change);
		if(BValue.isBlank(rhsVal))
			for(Address lhsAddr : lhsAddrs)
				store.apply(lhsAddr).stringAD = new Str(Str.LatticeElement.SNOTBLANK, change);
		if(BValue.isNaN(rhsVal))
			for(Address lhsAddr : lhsAddrs)
				store.apply(lhsAddr).numberAD = new Num(Num.LatticeElement.NOT_NAN, change);
		if(BValue.isZero(rhsVal))
			for(Address lhsAddr : lhsAddrs)
				store.apply(lhsAddr).numberAD = new Num(Num.LatticeElement.NOT_ZERO, change);
		if(BValue.isFalse(rhsVal))
			for(Address lhsAddr : lhsAddrs)
				store.apply(lhsAddr).booleanAD = new Bool(Bool.LatticeElement.TRUE, change);
		if(BValue.isAddress(rhsVal))
			for(Address lhsAddr : lhsAddrs) {
				BValue lhsVal = store.apply(lhsAddr);
				lhsVal.addressAD.addresses.removeAll(rhsVal.addressAD.addresses);
				lhsVal.addressAD.change = change;
			}

	}

	private void interpretOr(InfixExpression ie, boolean not) {

		/* Interpret both sides of the condition if they must both be
		 * true. */
		if(not) {
			interpretCondition(ie.getLeft(), false);
			interpretCondition(ie.getRight(), false);
		}

	}

	private void interpretAnd(InfixExpression ie, boolean not) {

		/* Interpret both sides of the condition if they must both be
		 * true. */
		if(!not) {
			interpretCondition(ie.getLeft(), false);
			interpretCondition(ie.getRight(), false);
		}

	}

	public State transfer(CFGNode node) {

		/* Update the trace to the current statement. */
		this.trace = this.trace.update(node.getId());

		/* The statement to transfer over. */
		AstNode statement = (AstNode)node.getStatement();

		/* Interpret the statement. */
		interpretStatement(statement);

		return this;

	}

	/**
	 * Performs an abstract interpretation on the node.
	 */
	private void interpretStatement(AstNode node) {

		if(node instanceof EmptyStatement) { /* Skip. */ }
		else if(node instanceof ExpressionStatement) {
			interpretStatement(((ExpressionStatement)node).getExpression());
		}
		else if(node instanceof VariableDeclaration) {
			interpretVariableDeclaration((VariableDeclaration)node);
		}
		else if(node instanceof FunctionCall) {
			if(((FunctionCall)node).getTarget().toSource().equals("this.runHooks")) {
				System.out.println(i + " - " + "this.runHooks");
				this.i++;
			}
			ExpEval expEval = new ExpEval(this);
			State endState = expEval.evalFunctionCall((FunctionCall) node);
			this.store = endState.store;
//			this.store = Helpers.gc(this.env, this.store); TODO: Helpers.gc needs to account for all environments in the call stack
		}
		else if(node instanceof Assignment) {
			interpretAssignment((Assignment)node);
		}
		else if(node instanceof ReturnStatement) {
			interpretReturn((ReturnStatement)node);
		}

	}

	/**
	 * Evaluates the return expression and stores the value in the scratchpad
	 * for use by the caller.
	 */
	public void interpretReturn(ReturnStatement rs) {

		BValue retVal = null;

		/* Evaluate the return value from the return expression. */
		if(rs.getReturnValue() == null) {
			retVal = Undefined.inject(Undefined.top(Change.u()), Change.convU(rs), Change.convU(rs), DefinerIDs.inject(rs.getID()));
		}
		else {
			ExpEval expEval = new ExpEval(this);
			retVal = expEval.eval(rs.getReturnValue());
		}

		/* Join the values if a return value already exists on the path. */
		BValue oldVal = this.scratch.applyReturn();
		if(oldVal != null)
			retVal = retVal.join(oldVal);
		
		/* Conservatively add a dummy DefinerID to the BValue if there are currently
		 * no DefinerIDs */
		if(retVal.definerIDs.isEmpty()) {
			if(rs.getReturnValue() == null) {
				retVal.definerIDs = retVal.definerIDs.strongUpdate(rs.getID());
				rs.setDummy();
			}
			else {
				retVal.definerIDs = retVal.definerIDs.strongUpdate(rs.getReturnValue().getID());
				rs.getReturnValue().setDummy();
			}
		}

		/* Make a fake var in the environment and point it to the value so that
		 * if it contains a function, it will be analyzed during the
		 * 'accessible function' phase of the analysis. */
		Address address = this.trace.makeAddr(rs.getID(), "");
		this.env = this.env.strongUpdate("~retval~", new Variable(rs.getID(), "~retval~", new Addresses(address, Change.u())));
		this.store = this.store.alloc(address, retVal);

		/* Update the return value on the scratchpad. */
		this.scratch = this.scratch.strongUpdate(retVal, null);

	}

	/**
	 * Updates the store based on abstract interpretation of assignments.
	 * @param vd The variable declaration. Variables have already been
	 * lifted into the environment.
	 */
	public void interpretVariableDeclaration(VariableDeclaration vd) {
		for(VariableInitializer vi : vd.getVariables()) {
			if(vi.getInitializer() != null) {
				concreteAssignInterpreter(vi.getTarget(), vi.getInitializer());
			}
		}
	}

	/**
	 * Updates the store based on abstract interpretation of assignments.
	 * @param a The assignment.
	 */
	public void interpretAssignment(Assignment a) {
		concreteAssignInterpreter(a.getLeft(), a.getRight());
	}

	/**
	 * Helper function since variable initializers and assignments do the same thing.
	 */
	private void concreteAssignInterpreter(AstNode lhs, AstNode rhs) {

		/* Resolve the left hand side to a set of addresses. */
		Set<Address> addrs = resolveOrCreate(lhs);

		/* Resolve the right hand side to a value. */
		ExpEval expEval = new ExpEval(this);
		BValue val = expEval.eval(rhs);

		/* Conservatively add a dummy DefinerID to the BValue if there are currently
		 * no DefinerIDs */
		if(val.definerIDs.isEmpty()) {
			val.definerIDs = val.definerIDs.strongUpdate(rhs.getID());
			rhs.setDummy();
		}

		/* Update the values in the store. */
		// TODO: Is this correct? We should probably only do a strong update if
		//		 there is only one address. Otherwise we don't know which one
		//		 to update.
		for(Address addr : addrs) {
			store = store.strongUpdate(addr, val);
		}

	}

	/**
	 * Helper function since variable initializers and assignments do the same thing.
	 */
	public void interpretAssignment(AstNode lhs, BValue val) {

		/* Resolve the left hand side to a set of addresses. */
		Set<Address> addrs = resolveOrCreate(lhs);

		/* Update the values in the store. */
		// TODO: Is this correct? We should probably only do a strong update if
		//		 there is only one address. Otherwise we don't know which one
		//		 to update.
		for(Address addr : addrs) {
			store = store.strongUpdate(addr, val);
		}

	}

	/**
	 * Base case: A simple name in the environment.
	 * @param node A Name node
	 * @return The set of addresses this identifier can resolve to.
	 */
	private Set<Address> resolveOrCreateBaseCase(Name node) {

		Set<Address> result = new HashSet<Address>();

		Addresses addrs = env.apply(node.toSource());
		if(addrs == null) {
			/* Assume the variable exists in the environment (ie. not a TypeError)
			 * and add it to the environment/store as BValue.TOP since we know
			 * nothing about it. */
			Address addr = trace.makeAddr(node.getID(), "");
			env = env.strongUpdate(node.toSource(), new Variable(node.getID(), node.toSource(), Change.bottom(), new Addresses(addr, Change.u())));
			store = store.alloc(addr, Addresses.dummy(Change.bottom(), Change.bottom(), Change.bottom(), DefinerIDs.inject(node.getID())));
			addrs = new Addresses(addr, Change.u());
		}

		result.addAll(addrs.addresses);
		return result;

	}

	/**
	 * Recursive case: A property access.
	 * @param ie A qualified name node.
	 * @return The set of addresses this identifier can resolve to.
	 */
	private Set<Address> resolveOrCreateProperty(PropertyGet ie) {

		Set<Address> result = new HashSet<Address>();

		/* We do not handle the cases where the rhs is an expression. */
		if(!(ie.getRight() instanceof Name)) return result;

		/* We have a qualified name. Recursively find all the addresses
		 * that lhs can resolve to. */
		Set<Address> lhs = resolveOrCreate(ie.getLeft());

		/* Just in case we couldn't resolve or create the sub-expression. */
		if(lhs == null) return result;

		/* Lookup the current property at each of these addresses. Ignore
		 * type errors and auto-boxing for now. */
		for(Address valAddr : lhs) {

			/* Get the value at the address. */
			BValue val = store.apply(valAddr);

			/* We may need to create a dummy object if 'val' doesn't point
			 * to any objects. */
			if(val.addressAD.addresses.size() == 0) {
				Map<String, Property> ext = new HashMap<String, Property>();
				Obj dummy = new Obj(ext, new InternalObjectProperties());
				Address addr = trace.makeAddr(ie.getLeft().getID(), "");
				store = store.alloc(addr, dummy);
				val = val.join(Address.inject(addr, Change.bottom(), Change.bottom(), Change.bottom(), DefinerIDs.bottom()));
				store = store.strongUpdate(valAddr, val);
			}

			for(Address objAddr : val.addressAD.addresses) {

				/* Get the Obj from the store. */
				Obj obj = store.getObj(objAddr);

				/* Look up the property. */
				Address propAddr = obj.apply(ie.getRight().toSource());

				if(propAddr != null) {
					result.add(propAddr);
					// Sanity check that the property address is in the store.
					BValue propVal = store.apply(propAddr);
					if(propVal == null)
						throw new Error("Property value does not exist in store.");
				}
				else {
					/* This property was not found, which means it is either
					 * undefined or was initialized somewhere outside the
					 * analysis. Create it and give it the value BValue.TOP. */

					/* Create a new address (BValue) for the property and
					 * put it in the store. */
					propAddr = trace.makeAddr(ie.getRight().getID(), ie.getRight().toSource());
					BValue propVal = Addresses.dummy(Change.bottom(), Change.bottom(), Change.bottom(), DefinerIDs.inject(ie.getRight().getID()));
					store = store.alloc(propAddr, propVal);

					/* Add the property to the external properties of the object. */
					Map<String, Property> ext = new HashMap<String, Property>(obj.externalProperties);
					ext.put(ie.getRight().toSource(), new Property(ie.getRight().getID(), ie.getRight().toSource(), Change.u(), propAddr));

					/* We need to create a new object so that the previous
					 * states are not affected by this update. */
					Obj newObj = new Obj(ext, obj.internalProperties);
					store = store.strongUpdate(objAddr, newObj);

					result.add(propAddr);
				}
			}
		}

		return result;

	}
	
	/**
	 * Recursive case: An element access.
	 * @param eg An element access node.
	 * @return The set of addresses this element can resolve to.
	 */
	private Set<Address> resolveOrCreateElementCase(ElementGet eg) {

		Set<Address> result = new HashSet<Address>();
		
		/* We have a qualified name. Recursively find all the addresses
		 * that lhs can resolve to. */
		Set<Address> lhs = resolveOrCreate(eg.getTarget());

		/* Just in case we couldn't resolve or create the sub-expression. */
		if(lhs == null) return result;

		/* Lookup the current property at each of these addresses. Ignore
		 * type errors and auto-boxing for now. */
		for(Address valAddr : lhs) {
			
			/* Get the value at the address. */
			BValue val = store.apply(valAddr);

			/* We may need to create a dummy object if 'val' doesn't point
			 * to any objects. */
			if(val.addressAD.addresses.size() == 0) {
				Map<String, Property> ext = new HashMap<String, Property>();
				Obj dummy = new Obj(ext, new InternalObjectProperties());
				Address addr = trace.makeAddr(eg.getID(), "");
				store = store.alloc(addr, dummy);
				val = val.join(Address.inject(addr, Change.bottom(), Change.bottom(), Change.bottom(), DefinerIDs.bottom()));
				store = store.strongUpdate(valAddr, val);
			}
			
			for(Address objAddr : val.addressAD.addresses) {

				/* Get the Obj from the store. */
				Obj obj = store.getObj(objAddr);

				/* Look up the property. */
				ExpEval expEval = new ExpEval(this);
				BValue elementValue = expEval.eval(eg.getElement());
				
				String elementString = null;
				if(elementValue.numberAD.le == Num.LatticeElement.VAL) {
					elementString = elementValue.numberAD.val;
				}
				else if(elementValue.stringAD.le == Str.LatticeElement.SNOTNUMNORSPLVAL
						|| elementValue.stringAD.le == Str.LatticeElement.SNUMVAL
						|| elementValue.stringAD.le == Str.LatticeElement.SSPLVAL) {
					elementString = elementValue.stringAD.val;
				}
				else {
					elementString = "~unknown~";
				}
				
				Address propAddr = obj.apply(elementString);

				if(propAddr != null) {
					result.add(propAddr);
					// Sanity check that the property address is in the store.
					BValue propVal = store.apply(propAddr);
					if(propVal == null)
						throw new Error("Property value does not exist in store.");
				}
				else {
					/* This property was not found, which means it is either
					 * undefined or was initialized somewhere outside the
					 * analysis. Create it and give it the value BValue.TOP. */

					/* Create a new address (BValue) for the property and
					 * put it in the store. */
					propAddr = trace.makeAddr(eg.getID(), elementString);
					BValue propVal = Addresses.dummy(Change.bottom(), Change.bottom(), Change.bottom(), DefinerIDs.inject(eg.getID()));
					store = store.alloc(propAddr, propVal);

					/* Add the property to the external properties of the object. */
					Map<String, Property> ext = new HashMap<String, Property>(obj.externalProperties);
					ext.put(elementString, new Property(eg.getTarget().getID(), elementString, Change.u(), propAddr));

					/* We need to create a new object so that the previous
					 * states are not affected by this update. */
					Obj newObj = new Obj(ext, obj.internalProperties);
					store = store.strongUpdate(objAddr, newObj);

					result.add(propAddr);
				}
			}
		
		}
		
		return result;
		
	}
	
	/**
	 * Resolve an identifier which we don't currently handle, such as an array
	 * access.
	 * @return the address of a new (stopgap) value.
	 */
	public Set<Address> resolveOrCreateExpression(AstNode node) {
		
		/* Resolve the expression to a value. */
		ExpEval expEval = new ExpEval(this);
		BValue val = expEval.eval(node);
		
		/* Place the value on the store */
		Set<Address> addrs = new HashSet<Address>();
		Address addr = trace.makeAddr(node.getID(), "");
		store = store.alloc(addr, val);
		addrs.add(addr);

		return addrs;
		
	}

	/**
	 * Resolve an identifier to an address in the store.
	 * @param node The identifier to resolve.
	 * @return The set of addresses this identifier can resolve to.
	 */
	public Set<Address> resolveOrCreate(AstNode node) {

		/* Base Case: A simple name in the environment. */
		if(node instanceof Name) {
			return resolveOrCreateBaseCase((Name)node);
		}

		/* Recursive Case: A property access. */
		else if(node instanceof PropertyGet) {
			return resolveOrCreateProperty((PropertyGet)node);
		}
		
		/* Recursive Case: An element access. */
		else if(node instanceof ElementGet) {
			return resolveOrCreateElementCase((ElementGet)node);
		}

		/* This must be an expression, which we need to resolve. */
		else {
			return resolveOrCreateExpression(node);
		}

	}

	/**
	 * We should only join states from the same trace.
	 * @param state The state to join with.
	 * @return A state representing the join of the two states.
	 */
	public State join(State state) {

		if(state == null) return this;

		State joined = new State(
				this.store.join(state.store),
				this.env.join(state.env),
				this.scratch.join(state.scratch),
				this.trace,
				this.control.join(state.control),
				this.selfAddr,
				cfgs, callStack);

		return joined;

	}

}
