package ca.ubc.ece.salt.pangor.analysis.flow.abstractdomain;


/**
 * Stores the state for the number type abstract domain.
 * Lattice element is simple:
 * 			TOP
 * 			 |
 * 			BOT
 * Where TOP means the type could be a number and BOT means the type is definitely
 * not a number.
 *
 * TODO: Add change information to the lattice element.
 */
public class Number {

	private LatticeElement le;

	public Number() {
		this.le = LatticeElement.TOP;
	}

	private Number(LatticeElement le) {
		this.le = le;
	}

	/**
	 * @param number The number lattice element to inject.
	 * @return The base value tuple with injected number.
	 */
	public BValue inject(Number number) {
		return new BValue(
				Str.bottom(),
				number,
				Bool.bottom(),
				Null.bottom(),
				Undefined.bottom(),
				Addresses.bottom());
	}

	/**
	 * @return the top lattice element
	 */
	public static Number top() {
		return new Number(LatticeElement.TOP);
	}

	/**
	 * @return the bottom lattice element
	 */
	public static Number bottom() {
		return new Number(LatticeElement.BOTTOM);
	}

	/**
	 * Joins this number with another number.
	 * @param state The number to join with.
	 * @return A new number that is the join of the two numbers.
	 */
	public Number join(Number state) {
		if(this.le == state.le) return new Number(this.le);
		return new Number(LatticeElement.BOTTOM);
	}

	/** The lattice elements for the abstract domain. **/
	public enum LatticeElement {
		TOP,
		BOTTOM
	}

}