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

	public LatticeElement le;
	public Change change;

	private Undefined(LatticeElement le, Change change) {
		this.le = le;
		this.change = change;
	}

	/**
	 * Joins this undefined with another undefined.
	 * @param state The undefined to join with.
	 * @return A new undefined that is the join of the two addresses.
	 */
	public Undefined join(Undefined state) {
		Change change = this.change.join(state.change);
		if(this.le == state.le) return new Undefined(this.le, change);
		return new Undefined(LatticeElement.TOP, change);
	}

	/**
	 * @param undefined The undefined lattice element to inject.
	 * @return The base value tuple with injected undefined.
	 */
	public static BValue inject(Undefined undefined, Change valChange) {
		return new BValue(
				Str.bottom(undefined.change),
				Num.bottom(undefined.change),
				Bool.bottom(undefined.change),
				Null.bottom(undefined.change),
				undefined,
				Addresses.bottom(undefined.change),
				valChange);
	}

	/**
	 * @return the top lattice element
	 */
	public static Undefined top(Change change) {
		return new Undefined(LatticeElement.TOP, change);
	}

	/**
	 * @return the bottom lattice element
	 */
	public static Undefined bottom(Change change) {
		return new Undefined(LatticeElement.BOTTOM, change);
	}

	/** The lattice elements for the abstract domain. **/
	public enum LatticeElement {
		TOP,
		BOTTOM
	}

	@Override
	public String toString() {
		return "Undef:" + this.le.toString();
	}

}