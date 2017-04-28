package commitminer.analysis.flow.abstractdomain;



/**
 * Stores the state for the number type abstract domain.
 * Lattice element is:
 * 			  			TOP
 * 			   /	 	   \       \
 * 			SNotSpl 	 SNotNum     \
 * 		  /		    \   /		\	   \
 * 		SNum	SNotNumNorSpl	SSpl     \
 * 		 |			 |			  |		   \
 * 		"0"..      "foo"..    "valueOf"... ""
 * 		  \ \		 |   |		 /     /   /
 * 					BOT
 * Where TOP means the type could be any string and BOT means the type is definitely
 * not a string.
 */
public class Str {

	public LatticeElement le;
	public String val;
	public Change change;

	public Str(LatticeElement le, String val, Change change) {
		this.le = le;
		this.val = val;
		this.change = change;
	}

	public Str(LatticeElement le, Change change) {
		if(this.le == LatticeElement.SNUMVAL
				|| this.le == LatticeElement.SNOTNUMNORSPLVAL
				|| this.le == LatticeElement.SSPLVAL)
			throw new Error("A value must be provided with a VAL lattice element.");
		this.le = le;
		this.val = null;
		this.change = change;
	}

	/**
	 * Joins this string with another string.
	 * @param state The string to join with.
	 * @return A new string that is the join of the two strings.
	 */
	public Str join(Str state) {

		Change jc = this.change.join(state.change);

		LatticeElement l = this.le;
		LatticeElement r = state.le;

		if(l == r && this.val == state.val) return new Str(l, this.val, jc);
		if(l == LatticeElement.BOTTOM) return new Str(r, state.val, jc);
		if(r == LatticeElement.BOTTOM) return new Str(l, state.val, jc);

		if(isNum(l) && isNum(r)) return new Str(LatticeElement.SNUM, jc);
		if(isStr(l) && isStr(r)) return new Str(LatticeElement.SNOTNUMNORSPL, jc);
		if(isSpl(l) && isSpl(r)) return new Str(LatticeElement.SSPL, jc);

		if(notSpl(l) && notSpl(r)) return new Str(LatticeElement.SNOTSPL, jc);
		if(notNum(l) && notNum(r)) return new Str(LatticeElement.SNOTNUM, jc);

		if(notBlank(l) && notBlank(r)) return new Str(LatticeElement.SNOTBLANK, jc);

		return new Str(LatticeElement.TOP, jc);

	}

	private static boolean isNum(LatticeElement le) {
		switch (le) {
		case SNUMVAL:
		case SNUM: return true;
		default: return false;
		}
	}

	private static boolean isStr(LatticeElement le) {
		switch (le) {
		case SNOTNUMNORSPLVAL:
		case SNOTNUMNORSPL: return true;
		default: return false;
		}
	}

	private static boolean isSpl(LatticeElement le) {
		switch (le) {
		case SSPLVAL:
		case SSPL: return true;
		default: return false;
		}
	}

	private static boolean notSpl(LatticeElement le) {
		switch(le) {
		case SSPLVAL:
		case SSPL:
		case SNOTNUM:
		case TOP: return false;
		default: return true;
		}
	}

	private static boolean notNum(LatticeElement le) {
		switch(le) {
		case SNUMVAL:
		case SNUM:
		case SNOTSPL:
		case TOP: return false;
		default: return true;
		}
	}

	private static boolean notBlank(LatticeElement le) {
		switch(le) {
		case SBLANK:
		case TOP: return false;
		default: return true;
		}
	}

	/**
	 * @return true if the string is definitely not blank.
	 */
	public static boolean notBlank(Str str) {
		if(isNum(str.le)
				|| isSpl(str.le)
				|| str.le == LatticeElement.BOTTOM
				|| (str.le == LatticeElement.SNOTNUMNORSPLVAL && !str.val.equals("")))
			return true;
		return false;
	}

	/**
	 * @param string The string lattice element to inject.
	 * @return The base value tuple with injected string.
	 */
	public static BValue inject(Str string, Change valChange) {
		return new BValue(
				string,
				Num.bottom(string.change),
				Bool.bottom(string.change),
				Null.bottom(string.change),
				Undefined.bottom(string.change),
				Addresses.bottom(string.change),
				valChange);
	}

	/**
	 * @return the top lattice element
	 */
	public static Str top(Change change) {
		return new Str(LatticeElement.TOP, change);
	}

	/**
	 * @return the bottom lattice element
	 */
	public static Str bottom(Change change) {
		return new Str(LatticeElement.BOTTOM, change);
	}

	/** The type of a lattice element for the abstract domain. **/
	public enum LatticeElement {
		TOP,
		SBLANK,
		SNOTBLANK,
		SNOTSPL,
		SNOTNUM,
		SNUM,
		SNOTNUMNORSPL,
		SSPL,
		SNUMVAL,
		SNOTNUMNORSPLVAL,
		SSPLVAL,
		BOTTOM
	}

	@Override
	public String toString() {
		if(this.val != null) return "Str:" + this.val;
		return "Str:" + this.le.toString();
	}

}