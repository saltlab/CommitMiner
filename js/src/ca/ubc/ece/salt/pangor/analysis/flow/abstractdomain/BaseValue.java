package ca.ubc.ece.salt.pangor.analysis.flow.abstractdomain;



/**
 * The abstract domain for base values. Because the value can be multiple
 * types, the abstract domain is a tuple of lattice elements: one for each
 * base type (string, number, boolean, null, undefined and address).
 */
public class BaseValue {

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
	AddressAD addressAD;

	public BaseValue join(BaseValue baseValue) {
		// TODO Auto-generated method stub
		return null;
	}

}
