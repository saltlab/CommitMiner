package ca.ubc.ece.salt.pangor.js.classify.type;

import java.util.Arrays;
import java.util.Set;
import java.util.TreeSet;

import ca.ubc.ece.salt.pangor.cfg.CFGEdge;
import ca.ubc.ece.salt.pangor.cfg.CFGNode;
import ca.ubc.ece.salt.pangor.js.analysis.utilities.SpecialTypeAnalysisUtilities.SpecialType;

public class TypeLatticeElement {

	private static final Set<SpecialType> NO_VALUE
		= new TreeSet<SpecialType>(Arrays.asList(SpecialType.UNDEFINED,
												 SpecialType.NULL,
												 SpecialType.NO_VALUE));

	private static final Set<SpecialType> EMPTY
		= new TreeSet<SpecialType>(Arrays.asList(SpecialType.BLANK,
												 SpecialType.ZERO,
												 SpecialType.EMPTY_ARRAY,
												 SpecialType.EMPTY));

	private static final Set<SpecialType> FALSEY
		= new TreeSet<SpecialType>(Arrays.asList(SpecialType.UNDEFINED,
												 SpecialType.NULL,
												 SpecialType.BLANK,
												 SpecialType.ZERO,
												 SpecialType.EMPTY_ARRAY,
												 SpecialType.NAN,
												 SpecialType.NO_VALUE,
												 SpecialType.EMPTY,
												 SpecialType.FALSEY));

	private static final Set<SpecialType> TRUTHY
		= new TreeSet<SpecialType>(Arrays.asList(SpecialType.FUNCTION,
												 SpecialType.OBJECT,
												 SpecialType.STRING,
												 SpecialType.NUMBER,
												 SpecialType.TRUTHY));

	/** The knowledge we have about the type equivalence. **/
	private SpecialType element;

	/** The knowledge we have about the change in type equivalence. **/
	private Change change;

	public TypeLatticeElement() {
		this.element = SpecialType.BOTTOM;
		this.change = Change.BOTTOM;
	}

	public TypeLatticeElement(SpecialType element, Change change) {
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

		SpecialType type = lubElement(left.element, right.element);
		Change change = lubChange(left.change, right.change);

		return new TypeLatticeElement(type, change);

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

	/**
	 * Returns the lowest upper bound for the two types.
	 * @return true if {@code type} is an element of {@code set}.
	 */
	private static SpecialType lubElement(SpecialType type1, SpecialType type2) {

		/* Cheap way of finding the lowest set that contains both elements. */
		if(type1 == type2) return type1;
		if(type1 == SpecialType.BOTTOM) return type2;
		if(type2 == SpecialType.BOTTOM) return type1;
		if(NO_VALUE.contains(type1) && NO_VALUE.contains(type2)) return SpecialType.NO_VALUE;
		if(EMPTY.contains(type1) && EMPTY.contains(type2)) return SpecialType.EMPTY;
		if(FALSEY.contains(type1) && FALSEY.contains(type2)) return SpecialType.FALSEY;
		if(TRUTHY.contains(type1) && TRUTHY.contains(type2)) return SpecialType.TRUTHY;
		return SpecialType.UNKNOWN;

	}

	private static Change lubChange(Change change1, Change change2) {

		/* Cheap way of finding the lowest set that contains both elements. */
		if(change1 == change2) return change1;
		if(change1 == Change.BOTTOM) return change2;
		if(change2 == Change.BOTTOM) return change1;
		return Change.IR_U;

	}

	public enum Change {
		IR,				// Inserted or Removed (depending on src or dst file analysis).
		U,				// Unchanged.
		IR_U,			// Prior paths include changed and unchanged information.
		BOTTOM
	}

}