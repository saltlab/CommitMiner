package ca.ubc.ece.salt.pangor.analysis.flow.abstractdomain;

import ca.ubc.ece.salt.pangor.cfg.CFGEdge;
import ca.ubc.ece.salt.pangor.cfg.CFGNode;

/**
 * The abstract domain for base values. Because the value can be multiple
 * types, the abstract domain is a tuple of lattice elements: one for each
 * base type (string, number, boolean, null, undefined and address).
 */
public class BaseValue implements IAbstractDomain {

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

	public BaseValue() {
		this.stringAD = new StringAD();
		this.numberAD = new NumberAD();
		this.booleanAD = new BooleanAD();
		this.nullAD = new NullAD();
		this.undefinedAD = new UndefinedAD();
		this.addressAD = new AddressAD();
	}

	private BaseValue(StringAD stringAD, NumberAD numberAD,
					  BooleanAD booleanAD, NullAD nullAD,
					  UndefinedAD undefinedAD, AddressAD addressAD) {
		this.stringAD = stringAD;
		this.numberAD = numberAD;
		this.booleanAD = booleanAD;
		this.nullAD = nullAD;
		this.undefinedAD = undefinedAD;
		this.addressAD = addressAD;
	}

	@Override
	public IAbstractDomain transfer(CFGEdge edge) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public IAbstractDomain transfer(CFGNode node) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public BaseValue join(IAbstractDomain istate) {
		if(!(istate instanceof BaseValue)) throw new IllegalArgumentException("Attempted to join " + istate.getClass().getName() + " with " + BaseValue.class.getName());
		BaseValue state = (BaseValue) istate;

		return new BaseValue(
				this.stringAD.join(state.stringAD),
				this.numberAD.join(state.numberAD),
				this.booleanAD.join(state.booleanAD),
				this.nullAD.join(state.nullAD),
				this.undefinedAD.join(state.undefinedAD),
				this.addressAD.join(state.addressAD));
	}

}