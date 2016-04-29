package ca.ubc.ece.salt.pangor.analysis.flow.abstractdomain;

import ca.ubc.ece.salt.pangor.analysis.flow.IAbstractDomain;
import ca.ubc.ece.salt.pangor.cfg.CFGEdge;
import ca.ubc.ece.salt.pangor.cfg.CFGNode;

/**
 * Stores the state for the undefined type abstract domain.
 * Lattice element is simple:
 * 			TOP
 * 			 |
 * 			BOT
 * Where TOP means the type could be undefined and BOT means the type is
 * definitely not undefined.
 *
 * TODO: Add change information to the lattice element.
 */
public class UndefinedAD implements IAbstractDomain{

	private LatticeElement le;

	public UndefinedAD() {
		this.le = LatticeElement.TOP;
	}

	private UndefinedAD(LatticeElement le) {
		this.le = le;
	}

	@Override
	public UndefinedAD transfer(CFGEdge edge) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public UndefinedAD transfer(CFGNode node) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public UndefinedAD join(IAbstractDomain istate) {
		if(!(istate instanceof UndefinedAD)) throw new IllegalArgumentException("Attempted to join " + istate.getClass().getName() + " with " + UndefinedAD.class.getName());
		UndefinedAD state = (UndefinedAD) istate;
		if(this.le == state.le) return new UndefinedAD(this.le);
		return new UndefinedAD(LatticeElement.BOTTOM);
	}

	/** The lattice elements for the abstract domain. **/
	public enum LatticeElement {
		TOP,
		BOTTOM
	}

	/**
	 * @return the top lattice element
	 */
	public static UndefinedAD top() {
		return new UndefinedAD(LatticeElement.TOP);
	}

	/**
	 * @return the bottom lattice element
	 */
	public static UndefinedAD bottom() {
		return new UndefinedAD(LatticeElement.BOTTOM);
	}

}