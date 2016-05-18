package ca.ubc.ece.salt.pangor.analysis.flow.abstractdomain;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.mozilla.javascript.Token;
import org.mozilla.javascript.ast.Assignment;
import org.mozilla.javascript.ast.AstNode;
import org.mozilla.javascript.ast.EmptyExpression;
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

		return this;

	}

	/**
	 * Performs an abstract interpretation on the condition.
	 */
	public void interpretCondition(AstNode condition, Address selfAddr) {
		if(condition == null) { /* Skip. */ }
		else if(condition instanceof EmptyExpression) { /* Skip. */ }
		else if(condition instanceof ParenthesizedExpression) {
			interpretConditionParenthesizedExpression((ParenthesizedExpression)condition, selfAddr);
		}
		else if()
	}

	private void interpretCondition(AstNode condition, Address selfAddr, boolean not) {
		if(condition instanceof ParenthesizedExpression) {
			interpretCondition(((ParenthesizedExpression)condition).getExpression(), selfAddr, not);
		}
		else if(condition instanceof Name) {
			// TODO: condition is cast as a boolean... so update it to be truthy or falsey
			Set<Address> addrs = resolveOrCreate(condition);
		}
		else if(condition instanceof InfixExpression &&
				((InfixExpression)condition).getOperator() == Token.GETPROP) {
			// TODO: condition is cast as a boolean... so update it to be truthy or falsey
			Set<Address> addrs = resolveOrCreate(condition);
		}
		else if(condition instanceof InfixExpression) {
			InfixExpression ie = (InfixExpression)condition;
			Set<Address> rhsAddrs;
			Set<Address> lhsAddrs;
			BValue rhsVal;
			switch(ie.getOperator()) {
			case Token.EQ:
				// TODO: Get the type of the RHS and update the value of the LHS
			case Token.NE:
				// TODO: Get the type of the RHS and update the value of the LHS
			case Token.SHEQ:
				// TODO: Get the type of the RHS and update the value of the LHS
				rhsAddrs = resolveOrCreate(ie.getRight());
				rhsVal = BValue.bottom();
				for(Address rhsAddr : rhsAddrs) {
					rhsVal = rhsVal.join(this.store.apply(rhsAddr));
				}
				lhsAddrs = resolveOrCreate(ie.getLeft());
				for(Address lhsAddr : lhsAddrs) {
					store.strongUpdate(lhsAddr, rhsVal);
				}
			case Token.SHNE:
				// TODO: Get the type of the RHS and update the value of the LHS
				rhsAddrs = resolveOrCreate(ie.getRight());
				rhsVal = BValue.bottom();
				for(Address rhsAddr : rhsAddrs) {
					rhsVal = rhsVal.join(this.store.apply(rhsAddr));
				}
				lhsAddrs = resolveOrCreate(ie.getLeft());
				if(BValue.isUndefined(rhsVal))
					for(Address lhsAddr : lhsAddrs)
						store.apply(lhsAddr).undefinedAD = Undefined.bottom();
				if(BValue.isNull(rhsVal))
					for(Address lhsAddr : lhsAddrs)
						store.apply(lhsAddr).nullAD = Null.bottom();
				if(BValue.isBlank(rhsVal))
					for(Address lhsAddr : lhsAddrs)
						store.apply(lhsAddr).stringAD = new Str(Str.LatticeElement.SNOTBLANK);
				if(BValue.isNaN(rhsVal))
					for(Address lhsAddr : lhsAddrs)
						store.apply(lhsAddr).numberAD = new Num(Num.LatticeElement.NOT_NAN);
				if(BValue.isZero(rhsVal))
					for(Address lhsAddr : lhsAddrs)
						store.apply(lhsAddr).numberAD = new Num(Num.LatticeElement.NOT_ZERO);
				if(BValue.isFalse(rhsVal))
					for(Address lhsAddr : lhsAddrs)
						store.apply(lhsAddr).booleanAD = new Bool(Bool.LatticeElement.TRUE);
				if(BValue.isAddress(rhsVal))
					for(Address lhsAddr : lhsAddrs)
						store.apply(lhsAddr).addressAD.addresses.removeAll(rhsVal.addressAD.addresses);
			case Token.AND:
				/* Interpret both sides of the condition if they must both be
				 * true. */
				if(!not) {
					interpretCondition(ie.getLeft(), selfAddr, false);
					interpretCondition(ie.getRight(), selfAddr, false);
				}
			case Token.OR:
				/* Interpret both sides of the condition if they must both be
				 * true. */
				if(not) {
					interpretCondition(ie.getLeft(), selfAddr, false);
					interpretCondition(ie.getRight(), selfAddr, false);
				}
			}
		}
	}

	private void interpretConditionParenthesizedExpression(ParenthesizedExpression pe, Address selfAddr) {
		interpretCondition(pe.getExpression(), selfAddr);
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

		/* Base Case: A simple name in the environment. */
		if(node instanceof Name) {
			Address addr = env.apply(node.toSource());
			if(addr == null) {
				/* Assume the variable exists in the environment (ie. not a TypeError)
				 * and add it to the environment/store as BValue.TOP since we know
				 * nothing about it. */
				addr = trace.makeAddr(node.getID(), "");
				env = env.strongUpdate(node.toSource(), addr);
				store = store.alloc(addr, Addresses.dummy());
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
					Obj dummy = new Obj(new HashMap<String, Address>(), new InternalObjectProperties(), new HashSet<String>());
					Address addr = trace.makeAddr(node.getID(), "");
					store = store.alloc(addr, dummy);
					val = val.join(Address.inject(addr));
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
						store = store.alloc(propAddr, Addresses.dummy());
						obj.externalProperties.put(ie.getRight().toSource(), propAddr);
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
