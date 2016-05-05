package ca.ubc.ece.salt.pangor.analysis.flow.abstractdomain;


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
public class Bool {

	private LatticeElement le;

	public Bool() {
		this.le = LatticeElement.TOP;
	}

	private Bool(LatticeElement le) {
		this.le = le;
	}

	/**
	 * Joins this boolean with another boolean.
	 * @param state The boolean to join with.
	 * @return A new boolean that is the join of two booleans.
	 */
	public Bool join(Bool state) {
		if(this.le == state.le) return new Bool(this.le);
		return new Bool(LatticeElement.BOTTOM);
	}

	/**
	 * @param bool The boolean lattice element to inject.
	 * @return The base value tuple with injected boolean.
	 */
	public static BValue inject(Bool bool) {
		return new BValue(
				Str.bottom(),
				Num.bottom(),
				bool,
				Null.bottom(),
				Undefined.bottom(),
				Addresses.bottom());
	}

	/**
	 * @return the top lattice element
	 */
	public static Bool top() {
		return new Bool(LatticeElement.TOP);
	}

	/**
	 * @return the bottom lattice element
	 */
	public static Bool bottom() {
		return new Bool(LatticeElement.BOTTOM);
	}

	/** The lattice elements for the abstract domain. **/
	public enum LatticeElement {
		TOP,
		BOTTOM
	}


}