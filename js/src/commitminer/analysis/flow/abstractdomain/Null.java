package commitminer.analysis.flow.abstractdomain;



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

	public LatticeElement le;
	public Change change;

	private Null(LatticeElement le, Change change) {
		this.le = le;
		this.change = change;
	}

	/**
	 * Performs a strong update on this lattice element.
	 * @param le The new lattice element.
	 * @return The updated state for this domain.
	 */
	public Null strongUpdate(LatticeElement le, Change change) {
		return new Null(le, change);
	}

	/**
	 * Performs a weak update on this lattice element.
	 * @param le The lattice element to merge with.
	 * @return The updated state for this domain.
	 */
	public Null weakUpdate(LatticeElement le, Change change) {
		change = this.change.join(change);
		if(this.le == le) return new Null(this.le, change);
		return new Null(LatticeElement.TOP, change);
	}

	/**
	 * Joins this null with another null.
	 * @param state The null to join with.
	 * @return A new null that is the join of the two nulls.
	 */
	public Null join(Null state) {
		Change jc = this.change.join(state.change);
		return this.weakUpdate(state.le, jc);
	}

	/**
	 * @param nll The null lattice element to inject.
	 * @return The base value tuple with injected null.
	 */
	public static BValue inject(Null nll, Change valChange) {
		return new BValue(
				Str.bottom(nll.change),
				Num.bottom(nll.change),
				Bool.bottom(nll.change),
				nll,
				Undefined.bottom(nll.change),
				Addresses.bottom(nll.change),
				valChange);
	}

	/**
	 * @return the top lattice element
	 */
	public static Null top(Change change) {
		return new Null(LatticeElement.TOP, change);
	}

	/**
	 * @return the bottom lattice element
	 */
	public static Null bottom(Change change) {
		return new Null(LatticeElement.BOTTOM, change);
	}

	/** The lattice elements for the abstract domain. **/
	public enum LatticeElement {
		TOP,
		BOTTOM
	}

	@Override
	public String toString() {
		return "Null:" + this.le.toString();
	}


}