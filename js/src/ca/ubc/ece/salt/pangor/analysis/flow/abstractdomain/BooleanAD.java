package ca.ubc.ece.salt.pangor.analysis.flow.abstractdomain;

import ca.ubc.ece.salt.pangor.cfg.CFGEdge;
import ca.ubc.ece.salt.pangor.cfg.CFGNode;

/**
 * Stores the state for the boolean type abstract domain.
 * Lattice element is simple:
 * 			TOP
 * 			 |
 * 			BOT
 * Where TOP means the type could be a boolean and BOT means the type is definitely
 * not a boolean.
 *
 * TODO: Add change information to the lattice element.
 */
public class BooleanAD implements IAbstractDomain{

	private LatticeElement le;

	public BooleanAD() {
		this.le = LatticeElement.TOP;
	}

	private BooleanAD(LatticeElement le) {
		this.le = le;
	}

	@Override
	public BooleanAD transfer(CFGEdge edge) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public BooleanAD transfer(CFGNode node) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public BooleanAD join(IAbstractDomain istate) {
		if(!(istate instanceof BooleanAD)) throw new IllegalArgumentException("Attempted to join " + istate.getClass().getName() + " with " + BooleanAD.class.getName());
		BooleanAD state = (BooleanAD) istate;
		if(this.le == state.le) return new BooleanAD(this.le);
		return new BooleanAD(LatticeElement.BOTTOM);
	}

	/** The lattice elements for the abstract domain. **/
	public enum LatticeElement {
		TOP,
		BOTTOM
	}

}