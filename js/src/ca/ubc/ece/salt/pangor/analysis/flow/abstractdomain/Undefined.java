package ca.ubc.ece.salt.pangor.analysis.flow.abstractdomain;


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
public class Undefined {

	private LatticeElement le;

	public Undefined() {
		this.le = LatticeElement.TOP;
	}

	private Undefined(LatticeElement le) {
		this.le = le;
	}

	/**
	 * Joins this undefined with another undefined.
	 * @param state The undefined to join with.
	 * @return A new undefined that is the join of the two addresses.
	 */
	public Undefined join(Undefined state) {
		if(this.le == state.le) return new Undefined(this.le);
		return new Undefined(LatticeElement.BOTTOM);
	}

	/**
	 * @param undefined The undefined lattice element to inject.
	 * @return The base value tuple with injected undefined.
	 */
	public BValue inject(Undefined undefined) {
		return new BValue(
				Str.bottom(),
				Number.bottom(),
				Bool.bottom(),
				Null.bottom(),
				undefined,
				Addresses.bottom());
	}

	/**
	 * @return the top lattice element
	 */
	public static Undefined top() {
		return new Undefined(LatticeElement.TOP);
	}

	/**
	 * @return the bottom lattice element
	 */
	public static Undefined bottom() {
		return new Undefined(LatticeElement.BOTTOM);
	}

	/** The lattice elements for the abstract domain. **/
	public enum LatticeElement {
		TOP,
		BOTTOM
	}

}