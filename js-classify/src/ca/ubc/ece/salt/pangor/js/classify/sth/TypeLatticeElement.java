package ca.ubc.ece.salt.pangor.js.classify.sth;

import ca.ubc.ece.salt.pangor.cfg.CFGEdge;
import ca.ubc.ece.salt.pangor.cfg.CFGNode;

public class TypeLatticeElement {

	/** The knowledge we have about the type equivalence. **/
	private Element element;

	/** The knowledge we have about the change in type equivalence. **/
	private Change change;

	public TypeLatticeElement() {
		this.element = Element.BOTTOM;
		this.change = Change.NA;
	}

	public TypeLatticeElement(Element element, Change change) {
		this.element = element;
		this.change = change;
	}

	/**
	 * Transfer over an edge.
	 */
	public TypeLatticeElement transfer(CFGEdge edge) {
		return null;
	}

	/**
	 * Transfer over a node.
	 */
	public TypeLatticeElement transfer(CFGNode node) {
		return null;
	}

	/**
	 * Joins two {@code TypeLatticeElement} with LUB
	 * @return The joined lattice element.
	 */
	public static TypeLatticeElement join(TypeLatticeElement left,
										  TypeLatticeElement right) {

		if(left.element == Element.BOTTOM)
			return new TypeLatticeElement(right.element, right.change);

		if(right.element == Element.BOTTOM)
			return new TypeLatticeElement(left.element, left.change);

		if(left.element.equals(right.element))
			return new TypeLatticeElement(left.element, left.change);

		if(left.element != right.element)
			return new TypeLatticeElement(Element.UNKNOWN, Change.NA);

		return new TypeLatticeElement(left.element, Change.IR_U);

	}

	/**
	 * @return A deep copy of this {@code TypeLatticeElement}.
	 */
	public TypeLatticeElement copy() {
		return new TypeLatticeElement(this.element, this.change);
	}

	@Override
	public boolean equals(Object o) {
		if(o instanceof TypeLatticeElement) {
			TypeLatticeElement tle = (TypeLatticeElement) o;
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
		NA,				// Type status unknown, so change information irrelevant.
		IR,				// Inserted or Removed (depending on src or dst file analysis).
		U,				// Unchanged.
		IR_U			// Prior paths include changed and unchanged information.
	}

}