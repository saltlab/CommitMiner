package ca.ubc.ece.salt.pangor.analysis.flow.abstractdomain;

import ca.ubc.ece.salt.pangor.analysis.flow.IAbstractDomain;
import ca.ubc.ece.salt.pangor.cfg.CFGEdge;
import ca.ubc.ece.salt.pangor.cfg.CFGNode;

/**
 * Stores the state for the number type abstract domain.
 * Lattice element is simple:
 * 			TOP
 * 			 |
 * 			BOT
 * Where TOP means the type could be a number and BOT means the type is definitely
 * not a number.
 *
 * TODO: Add change information to the lattice element.
 */
public class NumberAD implements IAbstractDomain{

	private LatticeElement le;

	public NumberAD() {
		this.le = LatticeElement.TOP;
	}

	private NumberAD(LatticeElement le) {
		this.le = le;
	}

	@Override
	public NumberAD transfer(CFGEdge edge) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public NumberAD transfer(CFGNode node) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public NumberAD join(IAbstractDomain istate) {
		if(!(istate instanceof NumberAD)) throw new IllegalArgumentException("Attempted to join " + istate.getClass().getName() + " with " + NumberAD.class.getName());
		NumberAD state = (NumberAD) istate;
		if(this.le == state.le) return new NumberAD(this.le);
		return new NumberAD(LatticeElement.BOTTOM);
	}

	/** The lattice elements for the abstract domain. **/
	public enum LatticeElement {
		TOP,
		BOTTOM
	}

	/**
	 * @return the top lattice element
	 */
	public static NumberAD top() {
		return new NumberAD(LatticeElement.TOP);
	}

	/**
	 * @return the bottom lattice element
	 */
	public static NumberAD bottom() {
		return new NumberAD(LatticeElement.BOTTOM);
	}

}