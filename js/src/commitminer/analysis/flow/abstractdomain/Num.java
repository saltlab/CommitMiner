package commitminer.analysis.flow.abstractdomain;



/**
 * Stores the state for the number type abstract domain.
 * Lattice:
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

	public LatticeElement le;
	public String val;
	public Change change;

	public Num(LatticeElement le, Change change) {
		if(le == null) throw new Error("The lattice element cannot be null.");
		if(le == LatticeElement.VAL) throw new Error("A value must be provided with the NVAL lattice element.");
		this.le = le;
		this.val = null;
		this.change = change;
	}

	public Num(LatticeElement le, String val, Change change) {
		if(le == null) throw new Error("The lattice element cannot be null.");
		this.le = le;
		this.val = val;
		this.change = change;
	}

	/**
	 * Joins this number with another number.
	 * @param state The number to join with.
	 * @return A new number that is the join of the two numbers.
	 */
	public Num join(Num state) {

		Change change = this.change.join(state.change);

		LatticeElement l = this.le;
		LatticeElement r = state.le;

		if(l == r && this.val == state.val) return new Num(l, this.val, change);
		if(l == LatticeElement.BOTTOM) return new Num(r, state.val, change);
		if(r == LatticeElement.BOTTOM) return new Num(l, this.val, change);

		if(isReal(l) && isReal(r)) return new Num(LatticeElement.REAL, change);
		if(notNaN(l) && notNaN(r)) return new Num(LatticeElement.NOT_NAN, change);
		if(notZero(l) && notZero(r)) return new Num(LatticeElement.NOT_ZERO, change);
		if(isFalsey(l) && isFalsey(r)) return new Num(LatticeElement.NAN_ZERO, change);
		if(notZeroNorNaN(l) && notZeroNorNaN(r)) return new Num(LatticeElement.NOT_ZERO_NOR_NAN, change);

		return new Num(LatticeElement.TOP, change);

	}

	public static boolean isReal(LatticeElement le) {
		switch(le) {
		case VAL:
		case REAL: return true;
		default: return false;
		}
	}

	public static boolean isFalsey(LatticeElement le) {
		switch(le) {
		case NAN:
		case ZERO:
		case NAN_ZERO: return true;
		default: return false;
		}
	}

	public static boolean notNaN(LatticeElement le) {
		switch(le) {
		case NAN:
		case NOT_ZERO:
		case NAN_ZERO:
		case TOP: return false;
		default: return true;
		}
	}

	public static boolean notZero(LatticeElement le) {
		switch(le) {
		case ZERO:
		case NOT_NAN:
		case NAN_ZERO:
		case TOP: return false;
		default: return true;
		}
	}

	public static boolean notZeroNorNaN(LatticeElement le) {
		switch(le) {
		case ZERO:
		case NAN:
		case NOT_NAN:
		case NOT_ZERO:
		case NAN_ZERO:
		case TOP: return false;
		default: return true;
		}
	}

	/**
	 * @param number The number lattice element to inject.
	 * @return The base value tuple with injected number.
	 */
	public static BValue inject(Num number, Change valChange) {
		return new BValue(
				Str.bottom(number.change),
				number,
				Bool.bottom(number.change),
				Null.bottom(number.change),
				Undefined.bottom(number.change),
				Addresses.bottom(number.change),
				valChange);
	}

	/**
	 * @return true if the number is definitely not zero.
	 */
	public static boolean notZero(Num num) {
		return notZero(num.le);
	}

	/**
	 * @return true if the number is definitely not NaN.
	 */
	public static boolean notNaN(Num num) {
		return notNaN(num.le);
	}

	/**
	 * @return the top lattice element
	 */
	public static Num top(Change change) {
		return new Num(LatticeElement.TOP, change);
	}

	/**
	 * @return the bottom lattice element
	 */
	public static Num bottom(Change change) {
		return new Num(LatticeElement.BOTTOM, change);
	}

	/** The lattice elements for the abstract domain. **/
	public enum LatticeElement {
		TOP,
		ZERO,
		NAN,
		NAN_ZERO,
		NI,
		PI,
		VAL,
		REAL,
		NOT_NAN,
		NOT_ZERO,
		NOT_ZERO_NOR_NAN,
		BOTTOM
	}

	@Override
	public String toString() {
		return "Num:" + this.le.toString();
	}
	
	@Override
	public boolean equals(Object o) {
		if(!(o instanceof Num)) return false;
		Num num = (Num)o;
		if(this.le != num.le) return false;
		if(!this.val.equals(num.val)) return false;
		if(!this.change.equals(num.change)) return false;
		return true;
	}

}