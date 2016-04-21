package ca.ubc.ece.salt.pangor.js.classify.protect;

import ca.ubc.ece.salt.pangor.cfg.CFGEdge;
import ca.ubc.ece.salt.pangor.cfg.CFGNode;

public class ProtectedLatticeElement {

	/** The knowledge we have about the type equivalence. **/
	public Element element;

	/** The knowledge we have about the change in type equivalence. **/
	public Change change;

	public ProtectedLatticeElement() {
		this.element = Element.BOTTOM;
		this.change = Change.BOTTOM;
	}

	public ProtectedLatticeElement(Element element, Change change) {
		this.element = element;
		this.change = change;
	}

	/**
	 * Transfer over an edge.
	 */
	public ProtectedLatticeElement transfer(CFGEdge edge) {
		return null;
	}

	/**
	 * Transfer over a node.
	 */
	public ProtectedLatticeElement transfer(CFGNode node) {
		return null;
	}

	/**
	 * Joins two {@code TypeLatticeElement} with LUB
	 * @return The joined lattice element.
	 */
	public static ProtectedLatticeElement join(ProtectedLatticeElement left,
										  ProtectedLatticeElement right) {

		/* Join the type lattice and the change lattice. */
		return new ProtectedLatticeElement(join(left.element, right.element), join(left.change, right.change));

	}

	/**
	 * Joins to elements from the type lattice.
	 */
	private static Element join(Element left, Element right) {

		if(left == Element.BOTTOM)
			return right;

		if(right == Element.BOTTOM)
			return left;

		if(left.equals(right))
			return left;

		return Element.UNKNOWN;
	}

	/**
	 * Joins to elements from the change lattice.
	 */
	private static Change join(Change left, Change right) {

		if(left == Change.BOTTOM)
			return right;

		if(right == Change.BOTTOM)
			return left;

		if(left.equals(right))
			return left;

		return Change.IR_U;

	}

	/**
	 * @return A deep copy of this {@code TypeLatticeElement}.
	 */
	public ProtectedLatticeElement copy() {
		return new ProtectedLatticeElement(this.element, this.change);
	}

	@Override
	public boolean equals(Object o) {
		if(o instanceof ProtectedLatticeElement) {
			ProtectedLatticeElement tle = (ProtectedLatticeElement) o;
			if(this.element == tle.element
					&& this.change == tle.change) return true;
		}
		return false;
	}

	/** The (unordered) elements in the lattice. **/
	public enum Element {
		UNKNOWN,		// We have no knowledge about this type for the variable.
		EQ,				// The variable points to an object of this type.
		NE,				// The variable does not point to an object of this type.
		BOTTOM
	}

	public enum Change {
		IR,				// Inserted or Removed (depending on src or dst file analysis).
		U,				// Unchanged.
		IR_U,			// Prior paths include changed and unchanged information.
		BOTTOM
	}

}