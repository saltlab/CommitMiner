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
	private String val;

	public Num(LatticeElement le) {
		if(le == LatticeElement.NVAL) throw new Error("A value must be provided with the NVAL lattice element.");
		this.val = null;
	}

	public Num(LatticeElement le, String val) {
		this.le = le;
		this.val = val;
	}

	/**
	 * Joins this number with another number.
	 * @param state The number to join with.
	 * @return A new number that is the join of the two numbers.
	 */
	public Num join(Num state) {

		LatticeElement l = this.le;
		LatticeElement r = state.le;

		if(l == r && this.val == state.val) return new Num(l, this.val);
		if(l == LatticeElement.BOTTOM) return new Num(r, state.val);
		if(r == LatticeElement.BOTTOM) return new Num(l, this.val);

		if(isReal(l) && isReal(r)) return new Num(LatticeElement.NREAL);
		if(isConst(l) && isConst(l)) return new Num(LatticeElement.NCONST);

		return new Num(LatticeElement.TOP);

	}

	public static boolean isReal(LatticeElement le) {
		switch(le) {
		case NVAL:
		case NREAL: return true;
		default: return false;
		}
	}

	public static boolean isConst(LatticeElement le) {
		switch(le) {
		case NAN:
		case NNI:
		case NPI:
		case NCONST: return true;
		default: return false;
		}
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
	 * @return true if the number is definitely not zero.
	 */
	public static boolean notZero(Num num) {
		if(isConst(num.le)
				|| num.le == LatticeElement.BOTTOM
				|| (num.le == LatticeElement.NVAL && Double.parseDouble(num.val) != 0.0))
			return true;
		return false;
	}

	/**
	 * @return true if the number is definitely not NaN.
	 */
	public static boolean notNaN(Num num) {
		if(isReal(num.le)
				|| num.le == LatticeElement.NNI
				|| num.le == LatticeElement.NPI
				|| num.le == LatticeElement.BOTTOM)
			return true;
		return false;
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