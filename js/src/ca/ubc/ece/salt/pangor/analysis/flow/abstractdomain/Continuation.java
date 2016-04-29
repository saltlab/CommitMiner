package ca.ubc.ece.salt.pangor.analysis.flow.abstractdomain;

import ca.ubc.ece.salt.pangor.analysis.flow.IAbstractDomain;
import ca.ubc.ece.salt.pangor.cfg.CFGEdge;
import ca.ubc.ece.salt.pangor.cfg.CFGNode;

/**
 * The abstract domain for keeping track of the address of code to continue
 * executing after a basic block has finished executing.
 *
 * In the current implementation, we traverse CFGs that are complete for
 * functions so we only need to keep track of the continuation for method
 * calls (i.e., where to continue executing after a function returns).
 *
 * This is not the behaviour of JSAI, which is based on "Abstracting Abstract
 * Domains" and might have implications for making some context-sensitivities
 * tractable. For our specialized purpose (our analysis is constrained to one
 * file), this is probably not needed.
 *
 * For function calls, what we can do is create a new variable in the
 * environment of the caller for the return value. The address of the return
 * value in the store and the continuation node will be passed to the callee.
 */
public class Continuation implements IAbstractDomain {

	@Override
	public IAbstractDomain transfer(CFGEdge edge) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public IAbstractDomain transfer(CFGNode node) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public IAbstractDomain join(IAbstractDomain ad) {
		// TODO Auto-generated method stub
		return null;
	}

}
