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
public class Null {

	private LatticeElement le;

	public Null() {
		this.le = LatticeElement.TOP;
	}

	private Null(LatticeElement le) {
		this.le = le;
	}

	/**
	 * Joins this null with another null.
	 * @param state The null to join with.
	 * @return A new null that is the join of the two nulls.
	 */
	public Null join(Null state) {
		if(this.le == state.le) return new Null(this.le);
		return new Null(LatticeElement.BOTTOM);
	}

	/**
	 * @param nll The null lattice element to inject.
	 * @return The base value tuple with injected null.
	 */
	public BValue inject(Null nll) {
		return new BValue(
				Str.bottom(),
				Number.bottom(),
				Bool.bottom(),
				nll,
				Undefined.bottom(),
				Addresses.bottom());
	}

	/**
	 * @return the top lattice element
	 */
	public static Null top() {
		return new Null(LatticeElement.TOP);
	}

	/**
	 * @return the bottom lattice element
	 */
	public static Null bottom() {
		return new Null(LatticeElement.BOTTOM);
	}

	/** The lattice elements for the abstract domain. **/
	public enum LatticeElement {
		TOP,
		BOTTOM
	}

}