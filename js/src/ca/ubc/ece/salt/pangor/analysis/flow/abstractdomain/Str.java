package ca.ubc.ece.salt.pangor.analysis.flow.abstractdomain;

import java.util.HashMap;
import java.util.Map;
import java.util.TreeSet;

/**
 * Stores the state for the number type abstract domain.
 * Lattice element is:
 * 			  		TOP
 * 			   /	 	   \
 * 			SNotSpl 	 SNotNum
 * 		  /		    \   /		\
 * 		SNum	SNotNumNorSpl	SSpl
 * 		 |			 |			  |
 * 		"0"..      "foo"..    "valueOf"...
 * 		  \ \		 |   |		 /     /
 * 					BOT
 * Where TOP means the type could be any string and BOT means the type is definitely
 * not a string.
 *
 * TODO: Add change information to the lattice element.
 */
public class Str {

	/** The lattice (for LUB). **/
	public final LatticeElement TOP = new LatticeElement(LatticeElementType.TOP, null);
	public final LatticeElement SNOTSPL = new LatticeElement(LatticeElementType.SNOTSPL, null, TOP);
	public final LatticeElement SNOTNUM = new LatticeElement(LatticeElementType.SNOTSPL, null, TOP);
	public final LatticeElement SNUM = new LatticeElement(LatticeElementType.SNUM, null, SNOTSPL);
	public final LatticeElement SNOTNUMNORSPL = new LatticeElement(LatticeElementType.SNOTNUMNORSPL, null, SNOTSPL, SNOTNUM);
	public final LatticeElement SSPL = new LatticeElement(LatticeElementType.SSPL, null, SNOTNUM);
	public final LatticeElement BOT = new LatticeElement(LatticeElementType.BOTTOM, null, SNUM, SNOTNUMNORSPL, SSPL);

	private final Map<LatticeElement, TreeSet<LatticeElement>> LESS_EQUAL;
	{
		LESS_EQUAL = new HashMap<LatticeElement, TreeSet<LatticeElement>>();
		lessEqualElements(LESS_EQUAL, BOT);
	}

	private LatticeElement le;

	public Str(LatticeElementType let, String value) {
		this.le = new LatticeElement(let, value);
	}

	public Str(LatticeElement le) {
		this.le = le;
	}

	/**
	 * Joins this string with another string.
	 * @param state The string to join with.
	 * @return A new string that is the join of the two strings.
	 */
	public Str join(Str state) {

		/* We need to handle three special cases because we don't store lattice
		 * elements for every possible string. */
		if(this.le == state.le) return new Str(this.le);
		if(this.le.type == LatticeElementType.BOTTOM) return new Str(state.le);
		if(state.le.type == LatticeElementType.BOTTOM) return new Str(this.le);

		/* Compute LUB. */
		return new Str(lub(this.le, state.le));

	}

	/**
	 * @param string The string lattice element to inject.
	 * @return The base value tuple with injected string.
	 */
	public static BValue inject(Str string) {
		return new BValue(
				string,
				Num.bottom(),
				Bool.bottom(),
				Null.bottom(),
				Undefined.bottom(),
				Addresses.bottom());
	}

	/**
	 * @return the top lattice element
	 */
	public static Str top() {
		return new Str(LatticeElementType.TOP, null);
	}

	/**
	 * @return the bottom lattice element
	 */
	public static Str bottom() {
		return new Str(LatticeElementType.BOTTOM, null);
	}


	/**
	 * @return the least upper bound for the two lattice elements.
	 */
	private LatticeElement lub(LatticeElement left, LatticeElement right) {
		TreeSet<LatticeElement> leftSet = LESS_EQUAL.get(left);
		TreeSet<LatticeElement> rightSet = LESS_EQUAL.get(right);
		leftSet.retainAll(rightSet);
		return leftSet.last();
	}

	/**
	 * Recursively builds <= sets for the lattice.
	 * @return The set of elements that are less than or equal to the given
	 *  	   lattice element.
	 */
	private static TreeSet<LatticeElement> lessEqualElements(
			Map<LatticeElement, TreeSet<LatticeElement>> preComputed,
			LatticeElement element) {
		TreeSet<LatticeElement> elements = new TreeSet<LatticeElement>();
		elements.add(element);
		for(LatticeElement lessThanElement : element.next) {
			elements.addAll(lessEqualElements(preComputed, lessThanElement));
		}
		preComputed.put(element, elements);
		return elements;
	}

	/** A lattice element for the abstract domain. **/
	public class LatticeElement implements Comparable<LatticeElement> {

		/**
		 * The type of value in the lattice element. Needed because we may
		 * know the exact string value if it has so far been constant
		 */
		public LatticeElementType type;

		/**
		 * The exact value of the lattice element (if known), or null if the
		 * lattice element can be more than one exact value.
		 */
		public String value;

		/** The next element in the lattice order (i.e. next < this). **/
		public LatticeElement[] next;

		/**
		 * @param type The type of value in the lattice element.
		 * @param value The exact value of the lattice element (if known) or
		 * 				null if the lattice element can be more than one exact
		 * 				value.
		 * @param next The next element in the lattice order (i.e. next < this).
		 */
		public LatticeElement(LatticeElementType type, String value, LatticeElement... next) {
			this.type = type;
			this.value = value;
			this.next = next;
		}

		@Override
		public int compareTo(LatticeElement o) {
			return this.type.compareTo(o.type);
		}

	}

	/** The type of a lattice element for the abstract domain. **/
	public enum LatticeElementType {
		TOP,
		SNOTSPL,
		SNOTNUM,
		SNUM,
		SNOTNUMNORSPL,
		SSPL,
		BOTTOM
	}

}