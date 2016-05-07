package ca.ubc.ece.salt.pangor.analysis.flow.trace;

import java.util.Stack;

import org.mozilla.javascript.ast.AstNode;

import ca.ubc.ece.salt.pangor.analysis.flow.abstractdomain.Address;
import ca.ubc.ece.salt.pangor.analysis.flow.abstractdomain.BValue;
import ca.ubc.ece.salt.pangor.analysis.flow.abstractdomain.Environment;
import ca.ubc.ece.salt.pangor.analysis.flow.abstractdomain.Store;

/**
 * Flow-sensitive stack-based k-CFA with h-sensitive heap
 */
public class StackCFA extends Trace {

	/** Call-site sensitivity. **/
	private int k;

	/** Heap sensitiviy. **/
	private int h;

	/** Program-point. **/
	private int pp;

	/** Trace (partial call stack). **/
	private Stack<Integer> tr;

	public StackCFA(int k, int h, int pp, Stack<Integer> tr) {
		if(h > k) throw new Error("heap sensitivity must be <= stack sensitivity");
		this.k = k;
		this.h = h;
		this.pp = pp;
		this.tr = tr;
	}

	@Override
	public Trace update(AstNode statement) {
		// Flow-sensitive
		return new StackCFA(k, h, statement.getID(), tr);
	}

	@Override
	public Trace update(Environment env, Store store, BValue self, BValue args,
			AstNode statement) {
		Stack<Integer> trp = new Stack<Integer>();

		if(tr.size() <= k) trp.addAll(tr);
		else trp.addAll(tr.subList(0, k-1));

		return new StackCFA(k, h, statement.getID(), trp);
	}

	@Override
	public Address toAddr() {
		Stack<Integer> trp = new Stack<Integer>();

		if(tr.size() <= h) trp.addAll(tr);
		else trp.addAll(tr.subList(0, k-1));

		return new Address(intsToBigInteger(trp, pp));
	}

	@Override
	public Address makeAddr(int varID) {
		Stack<Integer> trp = new Stack<Integer>();

		if(tr.size() <= k) trp.addAll(tr);
		else trp.addAll(tr.subList(0, k-1));

		return new Address(intsToBigInteger(trp, varID));
	}

}
