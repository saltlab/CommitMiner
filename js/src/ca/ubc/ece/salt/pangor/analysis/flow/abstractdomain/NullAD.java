package ca.ubc.ece.salt.pangor.analysis.flow.abstractdomain;


/**
 * Stores the state for the null type abstract domain.
 * Lattice element is simple:
 * 			TOP
 * 			 |
 * 			BOT
 * Where TOP means the type could be null and BOT means the type is definitely
 * not null.
 *
 * TODO: Add change information to the lattice element.
 */
public class NullAD {

	private LatticeElement le;

	public NullAD() {
		this.le = LatticeElement.TOP;
	}

	private NullAD(LatticeElement le) {
		this.le = le;
	}

	/**
	 * Joins this null with another null.
	 * @param state The null to join with.
	 * @return A new null that is the join of the two nulls.
	 */
	public NullAD join(NullAD state) {
		if(this.le == state.le) return new NullAD(this.le);
		return new NullAD(LatticeElement.BOTTOM);
	}

	/**
	 * @param nll The null lattice element to inject.
	 * @return The base value tuple with injected null.
	 */
	public BValue inject(NullAD nll) {
		return new BValue(
				StringAD.bottom(),
				NumberAD.bottom(),
				BooleanAD.bottom(),
				nll,
				UndefinedAD.bottom(),
				Addresses.bottom());
	}

	/**
	 * @return the top lattice element
	 */
	public static NullAD top() {
		return new NullAD(LatticeElement.TOP);
	}

	/**
	 * @return the bottom lattice element
	 */
	public static NullAD bottom() {
		return new NullAD(LatticeElement.BOTTOM);
	}

	/** The lattice elements for the abstract domain. **/
	public enum LatticeElement {
		TOP,
		BOTTOM
	}

}