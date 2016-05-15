package ca.ubc.ece.salt.pangor.analysis.flow.trace;

import java.math.BigInteger;

import org.mozilla.javascript.ast.AstNode;

import ca.ubc.ece.salt.pangor.analysis.flow.abstractdomain.Address;
import ca.ubc.ece.salt.pangor.analysis.flow.abstractdomain.Environment;
import ca.ubc.ece.salt.pangor.analysis.flow.abstractdomain.Store;

/**
 * Flow-sensitive context-insensitive.
 */
public class FSCI extends Trace {

	public int pp;

	/**
	 * @param pp The program point.
	 */
	public FSCI(int pp) {
		this.pp = pp;
	}

	@Override
	public Trace update(int pp) {
		// Flow sensitive
		return new FSCI(pp);
	}

	@Override
	public Trace update(Environment env, Store store, Address selfAddr, Address args,
			AstNode call) {
		// Context insensitive
		return new FSCI(call.getID());
	}

	@Override
	public Address toAddr(String prop) {
		return new Address(BigInteger.valueOf(pp).shiftLeft(32), prop);
	}

	@Override
	public Address makeAddr(int varID, String prop) {
		return new Address(BigInteger.valueOf(pp).shiftLeft(32).add(BigInteger.valueOf(varID)), prop);
	}

}