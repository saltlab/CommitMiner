package ca.ubc.ece.salt.pangor.analysis.flow.abstractdomain;


/**
 * The abstract domain for base values. Because the value can be multiple
 * types, the abstract domain is a tuple of lattice elements: one for each
 * base type (string, number, boolean, null, undefined and address).
 */
public class BValue {

	/** The abstract domain for strings. **/
	StringAD stringAD;

	/** The abstract domain for numbers. **/
	NumberAD numberAD;

	/** The abstract domain for booleans. **/
	BooleanAD booleanAD;

	/** The abstract domain for null. **/
	NullAD nullAD;

	/** The abstract domain for undefined. **/
	UndefinedAD undefinedAD;

	/** The abstract domain for memory addresses. **/
	Addresses addressAD;

	public BValue(StringAD stringAD, NumberAD numberAD,
					  BooleanAD booleanAD, NullAD nullAD,
					  UndefinedAD undefinedAD, Addresses addressAD) {
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
				StringAD.top(),
				NumberAD.top(),
				BooleanAD.top(),
				NullAD.top(),
				UndefinedAD.top(),
				Addresses.top());
	}

	/**
	 * @return the bottom lattice element
	 */
	public static BValue bottom() {
		return new BValue(
				StringAD.bottom(),
				NumberAD.bottom(),
				BooleanAD.bottom(),
				NullAD.bottom(),
				UndefinedAD.bottom(),
				Addresses.bottom());
	}

}