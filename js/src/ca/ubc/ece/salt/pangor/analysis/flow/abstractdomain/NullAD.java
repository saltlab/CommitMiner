package ca.ubc.ece.salt.pangor.analysis.flow.abstractdomain;

import ca.ubc.ece.salt.pangor.analysis.flow.IAbstractDomain;
import ca.ubc.ece.salt.pangor.cfg.CFGEdge;
import ca.ubc.ece.salt.pangor.cfg.CFGNode;

/**
 * Stores the state for the null type abstract domain.
 * Lattice element is simple:
 * 			TOP
 * 			 |
 * 			BOT
 * Where TOP means the type could be null and BOT means the type is definitely
 * not null.
 *
 * TODO: Add change information to the lattice element.
 */
public class NullAD implements IAbstractDomain{

	private LatticeElement le;

	public NullAD() {
		this.le = LatticeElement.TOP;
	}

	private NullAD(LatticeElement le) {
		this.le = le;
	}

	@Override
	public NullAD transfer(CFGEdge edge) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public NullAD transfer(CFGNode node) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public NullAD join(IAbstractDomain istate) {
		if(!(istate instanceof NullAD)) throw new IllegalArgumentException("Attempted to join " + istate.getClass().getName() + " with " + NullAD.class.getName());
		NullAD state = (NullAD) istate;
		if(this.le == state.le) return new NullAD(this.le);
		return new NullAD(LatticeElement.BOTTOM);
	}

	/** The lattice elements for the abstract domain. **/
	public enum LatticeElement {
		TOP,
		BOTTOM
	}

	/**
	 * @return the top lattice element
	 */
	public static NullAD top() {
		return new NullAD(LatticeElement.TOP);
	}

	/**
	 * @return the bottom lattice element
	 */
	public static NullAD bottom() {
		return new NullAD(LatticeElement.BOTTOM);
	}

}