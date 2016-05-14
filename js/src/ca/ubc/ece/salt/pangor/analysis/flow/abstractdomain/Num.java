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
public class Num {

	private LatticeElement le;

	public Num() {
		this.le = LatticeElement.TOP;
	}

	private Num(LatticeElement le) {
		this.le = le;
	}

	/**
	 * Joins this number with another number.
	 * @param state The number to join with.
	 * @return A new number that is the join of the two numbers.
	 */
	public Num join(Num state) {
		if(this.le == state.le) return new Num(this.le);
		return new Num(LatticeElement.BOTTOM);
	}

	/**
	 * @param number The number lattice element to inject.
	 * @return The base value tuple with injected number.
	 */
	public static BValue inject(Num number) {
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
	public static Num top() {
		return new Num(LatticeElement.TOP);
	}

	/**
	 * @return the bottom lattice element
	 */
	public static Num bottom() {
		return new Num(LatticeElement.BOTTOM);
	}

	/** The lattice elements for the abstract domain. **/
	public enum LatticeElement {
		TOP,
		BOTTOM
	}

	@Override
	public String toString() {
		return "Num:" + this.le.toString();
	}

}