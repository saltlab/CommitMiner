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
public class UndefinedAD {

	private LatticeElement le;

	public UndefinedAD() {
		this.le = LatticeElement.TOP;
	}

	private UndefinedAD(LatticeElement le) {
		this.le = le;
	}

	/**
	 * Joins this undefined with another undefined.
	 * @param state The undefined to join with.
	 * @return A new undefined that is the join of the two addresses.
	 */
	public UndefinedAD join(UndefinedAD state) {
		if(this.le == state.le) return new UndefinedAD(this.le);
		return new UndefinedAD(LatticeElement.BOTTOM);
	}

	/**
	 * @param undefined The undefined lattice element to inject.
	 * @return The base value tuple with injected undefined.
	 */
	public BValue inject(UndefinedAD undefined) {
		return new BValue(
				StringAD.bottom(),
				NumberAD.bottom(),
				BooleanAD.bottom(),
				NullAD.bottom(),
				undefined,
				Addresses.bottom());
	}

	/**
	 * @return the top lattice element
	 */
	public static UndefinedAD top() {
		return new UndefinedAD(LatticeElement.TOP);
	}

	/**
	 * @return the bottom lattice element
	 */
	public static UndefinedAD bottom() {
		return new UndefinedAD(LatticeElement.BOTTOM);
	}

	/** The lattice elements for the abstract domain. **/
	public enum LatticeElement {
		TOP,
		BOTTOM
	}

}