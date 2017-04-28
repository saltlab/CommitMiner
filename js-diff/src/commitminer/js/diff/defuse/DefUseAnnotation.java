package commitminer.js.diff.defuse;

import commitminer.analysis.flow.abstractdomain.Address;

public class DefUseAnnotation {

	public Address address;
	public Integer absolutePosition;
	public Integer length;

	public DefUseAnnotation(Address address, int absolutePosition, int length) {
		this.address = address;
		this.absolutePosition = absolutePosition;
		this.length = length;
	}

}
