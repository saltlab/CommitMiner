package ca.ubc.ece.salt.pangor.analysis.flow.abstractdomain;


/**
 * Stores the state for the number type abstract domain.
 * Lattice element is simple:
 * 				  TOP
 * 			   /	  \
 * 			CONST	  REAL
 * 		  /	 |  \   /  |  \
 * 		 NaN NI	 PI 0  1  2 ...
 * 		  \	 \  \	/ /	 /
 * 				BOT
 * Where TOP means the type could be a number and BOT means the type is definitely
 * not a number.
 *
 * TODO: Add change information to the lattice element.
 */
public class Num {

	private LatticeElement le;
	private Double val;

	public Num(LatticeElement le) {
		if(le == LatticeElement.NVAL) throw new Error("A value must be provided with the NVAL lattice element.");
		this.val = null;
	}

	private Num(LatticeElement le, Double val) {
		this.le = le;
		this.val = val;
	}

	/**
	 * Joins this number with another number.
	 * @param state The number to join with.
	 * @return A new number that is the join of the two numbers.
	 */
	public Num join(Num state) {
		if(this.le == state.le && this.val == state.val) return new Num(this.le, this.val);
		if(this.le == LatticeElement.BOTTOM) return new Num(state.le, state.val);
		if(state.le == LatticeElement.BOTTOM) return new Num(this.le, this.val);
		if((this.le == LatticeElement.NVAL || this.le == LatticeElement.NREAL)
				&& (state.le == LatticeElement.NVAL || state.le == LatticeElement.NREAL))
			return new Num(LatticeElement.NREAL);
		if((this.le == LatticeElement.NAN || this.le == LatticeElement.NNI || this.le == LatticeElement.NPI || this.le == LatticeElement.NCONST)
				&& (state.le == LatticeElement.NAN || state.le == LatticeElement.NNI || this.le == LatticeElement.NPI || this.le == LatticeElement.NCONST))
			return new Num(LatticeElement.NCONST);
		return new Num(LatticeElement.TOP);
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
		NAN,
		NNI,
		NPI,
		NVAL,
		NCONST,
		NREAL,
		BOTTOM
	}

	@Override
	public String toString() {
		return "Num:" + this.le.toString();
	}

}