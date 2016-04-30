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
public class BooleanAD {

	private LatticeElement le;

	public BooleanAD() {
		this.le = LatticeElement.TOP;
	}

	private BooleanAD(LatticeElement le) {
		this.le = le;
	}

	/**
	 * Joins this boolean with another boolean.
	 * @param state The boolean to join with.
	 * @return A new boolean that is the join of two booleans.
	 */
	public BooleanAD join(BooleanAD state) {
		if(this.le == state.le) return new BooleanAD(this.le);
		return new BooleanAD(LatticeElement.BOTTOM);
	}

	/**
	 * @param bool The boolean lattice element to inject.
	 * @return The base value tuple with injected boolean.
	 */
	public BValue inject(BooleanAD bool) {
		return new BValue(
				StringAD.bottom(),
				NumberAD.bottom(),
				bool,
				NullAD.bottom(),
				UndefinedAD.bottom(),
				Addresses.bottom());
	}

	/**
	 * @return the top lattice element
	 */
	public static BooleanAD top() {
		return new BooleanAD(LatticeElement.TOP);
	}

	/**
	 * @return the bottom lattice element
	 */
	public static BooleanAD bottom() {
		return new BooleanAD(LatticeElement.BOTTOM);
	}

	/** The lattice elements for the abstract domain. **/
	public enum LatticeElement {
		TOP,
		BOTTOM
	}


}