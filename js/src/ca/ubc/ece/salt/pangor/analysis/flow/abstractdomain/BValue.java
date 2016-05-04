package ca.ubc.ece.salt.pangor.analysis.flow.abstractdomain;


/**
 * The abstract domain for base values. Because the value can be multiple
 * types, the abstract domain is a tuple of lattice elements: one for each
 * base type (string, number, boolean, null, undefined and address).
 */
public class BValue {

	/** The abstract domain for strings. **/
	Str stringAD;

	/** The abstract domain for numbers. **/
	Num numberAD;

	/** The abstract domain for booleans. **/
	Bool booleanAD;

	/** The abstract domain for null. **/
	Null nullAD;

	/** The abstract domain for undefined. **/
	Undefined undefinedAD;

	/** The abstract domain for memory addresses. **/
	Addresses addressAD;

	public BValue(Str stringAD, Num numberAD,
					  Bool booleanAD, Null nullAD,
					  Undefined undefinedAD, Addresses addressAD) {
		this.stringAD = stringAD;
		this.numberAD = numberAD;
		this.booleanAD = booleanAD;
		this.nullAD = nullAD;
		this.undefinedAD = undefinedAD;
		this.addressAD = addressAD;
	}

	public BValue join(BValue state) {

		return new BValue(
				this.stringAD.join(state.stringAD),
				this.numberAD.join(state.numberAD),
				this.booleanAD.join(state.booleanAD),
				this.nullAD.join(state.nullAD),
				this.undefinedAD.join(state.undefinedAD),
				this.addressAD.join(state.addressAD));

	}

	/**
	 * @return the top lattice element
	 */
	public static BValue top() {
		return new BValue(
				Str.top(),
				Num.top(),
				Bool.top(),
				Null.top(),
				Undefined.top(),
				Addresses.top());
	}

	/**
	 * @return the bottom lattice element
	 */
	public static BValue bottom() {
		return new BValue(
				Str.bottom(),
				Num.bottom(),
				Bool.bottom(),
				Null.bottom(),
				Undefined.bottom(),
				Addresses.bottom());
	}

}