package ca.ubc.ece.salt.pangor.analysis.flow.abstractdomain;


/**
 * Stores the state for the change type abstract domain.
 * Lattice:
 * 			TOP
 * 		   /   \
 * 		  C	   U
 * 		   \   /
 * 			BOT
 * Where BOT is 0, C means the lattice element was changed (inserted or
 * removed), U means the lattice element was unchanged, TOP means the
 * lattice element was both changed and unchanged on paths that include
 * the current state.
 */
public class Change {

	private LatticeElement le;

	public Change(LatticeElement le) {
		this.le = le;
	}

	/**
	 * Joins this change with another change.
	 * @param state The change to join with.
	 * @return A new change that is the join of two changes.
	 */
	public Change join(Change state) {

		LatticeElement l = this.le;
		LatticeElement r = this.le;

		if(l == r) return new Change(l);
		if(l == LatticeElement.BOTTOM) return new Change(r);
		if(r == LatticeElement.BOTTOM) return new Change(l);

		return new Change(LatticeElement.TOP);

	}

	/**
	 * @return the top lattice element
	 */
	public static Change top() {
		return new Change(LatticeElement.TOP);
	}

	/**
	 * @return the bottom lattice element
	 */
	public static Change bottom() {
		return new Change(LatticeElement.BOTTOM);
	}

	/** The lattice elements for the abstract domain. **/
	public enum LatticeElement {
		TOP,
		CHANGED,
		UNCHANGED,
		BOTTOM
	}

	@Override
	public String toString() {
		return "Change:" + this.le.toString();
	}

}