package ca.ubc.ece.salt.pangor.analysis.flow.abstractdomain;



/**
 * Stores the state for the boolean type abstract domain.
 * Lattice element is simple:
 * 			TOP
 * 		   /   \
 * 		 true false
 * 		   \   /
 * 			BOT
 * Where TOP means the type could be a boolean and BOT means the type is definitely
 * not a boolean.
 */
public class Bool {

	public LatticeElement le;
	public Change change;

	public Bool(LatticeElement le, Change change) {
		this.le = le;
		this.change = change;
	}

	/**
	 * Joins this boolean with another boolean.
	 * @param state The boolean to join with.
	 * @return A new boolean that is the join of two booleans.
	 */
	public Bool join(Bool state) {

		Change jc = this.change.join(state.change);

		LatticeElement l = this.le;
		LatticeElement r = this.le;

		if(l == r) return new Bool(l, jc);
		if(l == LatticeElement.BOTTOM) return new Bool(r, jc);
		if(r == LatticeElement.BOTTOM) return new Bool(l, jc);

		return new Bool(LatticeElement.TOP, jc);

	}

	/**
	 * @return true if the value cannot be false
	 */
	public static boolean notFalse(Bool bool) {
		switch(bool.le) {
		case BOTTOM:
		case TRUE: return true;
		default: return false;
		}
	}

	/**
	 * @param bool The boolean lattice element to inject.
	 * @return The base value tuple with injected boolean.
	 */
	public static BValue inject(Bool bool, Change valChange) {
		return new BValue(
				Str.bottom(bool.change),
				Num.bottom(bool.change),
				bool,
				Null.bottom(bool.change),
				Undefined.bottom(bool.change),
				Addresses.bottom(bool.change),
				valChange);
	}

	/**
	 * @return the top lattice element
	 */
	public static Bool top(Change change) {
		return new Bool(LatticeElement.TOP, change);
	}

	/**
	 * @return the bottom lattice element
	 */
	public static Bool bottom(Change change) {
		return new Bool(LatticeElement.BOTTOM, change);
	}

	/** The lattice elements for the abstract domain. **/
	public enum LatticeElement {
		TOP,
		TRUE,
		FALSE,
		BOTTOM
	}

	@Override
	public String toString() {
		return "Bool:" + this.le.toString();
	}


}