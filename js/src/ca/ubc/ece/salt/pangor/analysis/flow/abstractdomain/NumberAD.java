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
public class NumberAD {

	private LatticeElement le;

	public NumberAD() {
		this.le = LatticeElement.TOP;
	}

	private NumberAD(LatticeElement le) {
		this.le = le;
	}

	/**
	 * @param number The number lattice element to inject.
	 * @return The base value tuple with injected number.
	 */
	public BValue inject(NumberAD number) {
		return new BValue(
				StringAD.bottom(),
				number,
				BooleanAD.bottom(),
				NullAD.bottom(),
				UndefinedAD.bottom(),
				Addresses.bottom());
	}

	/**
	 * @return the top lattice element
	 */
	public static NumberAD top() {
		return new NumberAD(LatticeElement.TOP);
	}

	/**
	 * @return the bottom lattice element
	 */
	public static NumberAD bottom() {
		return new NumberAD(LatticeElement.BOTTOM);
	}

	/**
	 * Joins this number with another number.
	 * @param state The number to join with.
	 * @return A new number that is the join of the two numbers.
	 */
	public NumberAD join(NumberAD state) {
		if(this.le == state.le) return new NumberAD(this.le);
		return new NumberAD(LatticeElement.BOTTOM);
	}

	/** The lattice elements for the abstract domain. **/
	public enum LatticeElement {
		TOP,
		BOTTOM
	}

}