package ca.ubc.ece.salt.pangor.analysis.flow.abstractdomain;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.mozilla.javascript.Token;
import org.mozilla.javascript.ast.Assignment;
import org.mozilla.javascript.ast.AstNode;
import org.mozilla.javascript.ast.EmptyStatement;
import org.mozilla.javascript.ast.ExpressionStatement;
import org.mozilla.javascript.ast.FunctionCall;
import org.mozilla.javascript.ast.InfixExpression;
import org.mozilla.javascript.ast.Name;
import org.mozilla.javascript.ast.ParenthesizedExpression;
import org.mozilla.javascript.ast.VariableDeclaration;
import org.mozilla.javascript.ast.VariableInitializer;

import ca.ubc.ece.salt.pangor.analysis.flow.IState;
import ca.ubc.ece.salt.pangor.analysis.flow.trace.Trace;
import ca.ubc.ece.salt.pangor.cfg.CFG;
import ca.ubc.ece.salt.pangor.cfg.CFGEdge;
import ca.ubc.ece.salt.pangor.cfg.CFGNode;

/**
 * Stores the state of the function analysis at a point in the CFG.
 */
public class State implements IState {

	/* The abstract domains that make up the program state. The abstract
	 * domains have access to each other. */

	public Environment env;
	public Store store;
	public Scratchpad scratch;
	public Trace trace;

	private Map<AstNode, CFG> cfgs;

	/**
	 * Create a new state after a transfer or join.
	 * @param store The abstract store of the new state.
	 * @param environment The abstract environment of the new state.
	 */
	public State(Store store, Environment environment, Scratchpad scratchpad, Trace trace, Map<AstNode, CFG> cfgs) {
		this.store = store;
		this.env = environment;
		this.scratch = scratchpad;
		this.trace = trace;
		this.cfgs = cfgs;
	}

	public State transfer(CFGEdge edge, Address selfAddr) {

		/* Update the trace to the current condition. */
		this.trace = this.trace.update(edge.getId());

		/* The condition to transfer over. */
		AstNode condition = (AstNode)edge.getCondition();

		/* Interpret the statement. */
		interpretCondition(condition, selfAddr, false);

		return this;

	}

	/**
	 * Performs an abstract interpretation on the condition.
	 */
	private void interpretCondition(AstNode condition, Address selfAddr, boolean not) {
		if(condition instanceof ParenthesizedExpression) {
			interpretCondition(((ParenthesizedExpression)condition).getExpression(), selfAddr, not);
		}
		else if(condition instanceof Name) {
			Set<Address> addrs = resolveOrCreate(condition);
			Change change = Change.conv(condition);
			if(not) interpretAddrsFalsey(addrs, change);
			else interpretAddrsTruthy(addrs, change);
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
				interpretAnd(ie, selfAddr, not);
				break;
			case Token.OR:
				interpretOr(ie, selfAddr, not);
				break;
			}
		}
	}

	private void interpretAddrsFalsey(Set<Address> addrs, Change change) {

		/* Update the value(s) to be falsey. */
		for(Address addr : addrs) {
			BValue val = Undefined.inject(Undefined.top(change))
					.join(Null.inject(Null.top(change)))
					.join(Str.inject(new Str(Str.LatticeElement.SBLANK, change)))
					.join(Num.inject(new Num(Num.LatticeElement.NAN_ZERO, change)))
					.join(Bool.inject(new Bool(Bool.LatticeElement.FALSE, change)));
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
		BValue rhsVal = BValue.bottom(Change.bottom());
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
		BValue rhsVal = BValue.bottom(Change.bottom());
		for(Address rhsAddr : rhsAddrs) {
			rhsVal = rhsVal.join(this.store.apply(rhsAddr));
		}

		/* Update the value(s) on the LHS. */
		Change change = Change.conv(ie);
		Set<Address>lhsAddrs = resolveOrCreate(ie.getLeft());
		if(BValue.isUndefined(rhsVal) || BValue.isNull(rhsVal))
			for(Address lhsAddr : lhsAddrs) {
				rhsVal = Undefined.inject(Undefined.top(change))
						.join(Null.inject(Null.top(change)));
				store.strongUpdate(lhsAddr, rhsVal);
			}
		if(BValue.isBlank(rhsVal) || BValue.isZero(rhsVal))
			for(Address lhsAddr : lhsAddrs) {
				rhsVal = Str.inject(new Str(Str.LatticeElement.SBLANK, change))
						.join(Num.inject(new Num(Num.LatticeElement.ZERO, change)));
				store.strongUpdate(lhsAddr, rhsVal);
			}
		if(BValue.isNaN(rhsVal))
			for(Address lhsAddr : lhsAddrs)
				store.strongUpdate(lhsAddr, Num.inject(new Num(Num.LatticeElement.NAN, change)));
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
		BValue rhsVal = BValue.bottom(Change.bottom());
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
		BValue rhsVal = BValue.bottom(Change.bottom());
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

	private void interpretOr(InfixExpression ie, Address selfAddr, boolean not) {

		/* Interpret both sides of the condition if they must both be
		 * true. */
		if(not) {
			interpretCondition(ie.getLeft(), selfAddr, false);
			interpretCondition(ie.getRight(), selfAddr, false);
		}

	}

	private void interpretAnd(InfixExpression ie, Address selfAddr, boolean not) {

		/* Interpret both sides of the condition if they must both be
		 * true. */
		if(!not) {
			interpretCondition(ie.getLeft(), selfAddr, false);
			interpretCondition(ie.getRight(), selfAddr, false);
		}

	}

	public State transfer(CFGNode node, Address selfAddr) {

		/* Update the trace to the current statement. */
		this.trace = this.trace.update(node.getId());

		/* The statement to transfer over. */
		AstNode statement = (AstNode)node.getStatement();

		/* Interpret the statement. */
		interpretStatement(statement, selfAddr);

		return this;

	}

	/**
	 * Performs an abstract interpretation on the node.
	 */
	private void interpretStatement(AstNode node, Address selfAddr) {

		if(node instanceof EmptyStatement) { /* Skip. */ }
		else if(node instanceof ExpressionStatement) {
			interpretStatement(((ExpressionStatement)node).getExpression(), selfAddr);
		}
		else if(node instanceof VariableDeclaration) {
			interpretVariableDeclaration((VariableDeclaration)node, selfAddr);
		}
		else if(node instanceof FunctionCall) {
			ExpEval expEval = new ExpEval(env, store, scratch, trace, selfAddr, cfgs);
			State endState = expEval.evalFunctionCall((FunctionCall) node);
			this.store = endState.store;
		}
		else if(node instanceof Assignment) {
			interpretAssignment(selfAddr, (Assignment)node);
		}

	}

	/**
	 * Updates the store based on abstract interpretation of assignments.
	 * @param vd The variable declaration. Variables have already been
	 * lifted into the environment.
	 */
	public void interpretVariableDeclaration(VariableDeclaration vd, Address selfAddr) {
		for(VariableInitializer vi : vd.getVariables()) {
			if(vi.getInitializer() != null) {
				concreteAssignInterpreter(selfAddr, vi.getTarget(), vi.getInitializer());
			}
		}
	}

	/**
	 * Updates the store based on abstract interpretation of assignments.
	 * @param a The assignment.
	 */
	private void interpretAssignment(Address selfAddr, Assignment a) {
		concreteAssignInterpreter(selfAddr, a.getLeft(), a.getRight());
	}

	/**
	 * Helper function since variable initializers and assignments do the same thing.
	 */
	private void concreteAssignInterpreter(Address selfAddr, AstNode lhs, AstNode rhs) {

		/* Resolve the left hand side to a set of addresses. */
//		Set<Address> addrs = Helpers.resolve(environment, store, lhs);
		Set<Address> addrs = resolveOrCreate(lhs);

		/* Resolve the right hand side to a value. */
		ExpEval expEval = new ExpEval(env, store, scratch, trace, selfAddr, cfgs);
		BValue val = expEval.eval(rhs);
		store = expEval.store;

		/* Update the values in the store. */
		// TODO: Is this correct? We should probably only do a strong update if
		//		 there is only one address. Otherwise we don't know which one
		//		 to update.
		for(Address addr : addrs) {
			store = store.strongUpdate(addr, val);
		}

	}

	private Set<Address> resolveOrCreate(AstNode node) {
		Set<Address> result = new HashSet<Address>();
		Change change = Change.conv(node);

		/* Base Case: A simple name in the environment. */
		if(node instanceof Name) {
			Address addr = env.apply(node.toSource());
			if(addr == null) {
				/* Assume the variable exists in the environment (ie. not a TypeError)
				 * and add it to the environment/store as BValue.TOP since we know
				 * nothing about it. */
				addr = trace.makeAddr(node.getID(), "");
				env = env.strongUpdate(new Identifier(node.toSource(), Change.top()), addr);
				store = store.alloc(addr, Addresses.dummy(change));
			}
			result.add(addr);
		}

		/* Recursive Case: A property access. */
		else if(node instanceof InfixExpression) {

			InfixExpression ie = (InfixExpression) node;

			/* We do not handle the cases where:
			 * 	1. The rhs is an expression
			 *  2. The operator is not a property access */
			if(!(ie.getRight() instanceof Name)
					|| ie.getOperator() != Token.GETPROP) return result;

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
					Map<Identifier, Address> ext = new HashMap<Identifier, Address>();
					Obj dummy = new Obj(ext, new InternalObjectProperties());
					Address addr = trace.makeAddr(node.getID(), "");
					store = store.alloc(addr, dummy);
					val = val.join(Address.inject(addr, change));
					store = store.strongUpdate(valAddr, val);
				}

				for(Address objAddr : val.addressAD.addresses) {

					/* Get the Obj from the store. */
					Obj obj = store.getObj(objAddr);

					/* Look up the property. */
					Address propAddr = obj.externalProperties.get(ie.getRight().toSource());

					if(propAddr != null) result.add(propAddr);
					else {
						/* This property was not found, which means it is either
						 * undefined or was initialized somewhere outside the
						 * analysis. Create it and give it the value BValue.TOP. */
						propAddr = trace.makeAddr(node.getID(), ie.getRight().toSource());
						store = store.alloc(propAddr, Addresses.dummy(change));
						obj.externalProperties.put(new Identifier(ie.getRight().toSource(), Change.u()), propAddr);
						result.add(propAddr);
					}
				}
			}
		}

		return result;

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
				this.trace, cfgs);

		return joined;

	}

}
