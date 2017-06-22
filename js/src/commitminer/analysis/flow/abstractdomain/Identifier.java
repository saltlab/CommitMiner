package commitminer.analysis.flow.abstractdomain;

import commitminer.analysis.annotation.DependencyIdentifier;


/**
 * A variable identifier combined with a change lattice.
 */
public class Identifier implements DependencyIdentifier {

	public Integer definerID;
	public String name;
	public Change change;
	public Addresses addresses;

	/**
	 * Use for standard lookup operations when the change type does not matter.
	 * @param name The name of the identifier to inject.
	 */
	public Identifier(Integer definerID, String name, Addresses addresses) {
		this.definerID = definerID;
		this.name = name;
		this.change = Change.bottom();
		this.addresses = addresses;
	}

	/**
	 * Use for standard lookup operations when the change type does not matter.
	 * @param name The name of the identifier to inject.
	 * @param change How the identifier was changed.
	 */
	public Identifier(Integer definerID, String name, Change change, Addresses addresses) {
		this.definerID = definerID;
		this.name = name;
		this.change = change;
		this.addresses = addresses;
	}
	
	/**
	 * Joins the given Identifier with this Identifier.
	 */
	public Identifier join(Identifier id) {
		
		if(!definerID.equals(id.definerID) || !name.equals(id.name)) 
			throw new Error("Identifier::join() -- Cannot join different Identifiers.");

		change.join(id.change);
		addresses.join(id.addresses);

		return new Identifier(definerID, name, change.join(id.change), addresses.join(id.addresses));
	}

	@Override
	public int hashCode() {
		return this.name.hashCode();
	}

	@Override
	public boolean equals(Object o) {
		if(this == o) return true;
		if(!(o instanceof Identifier)) return false;
		Identifier right = (Identifier)o;
		return this.name.equals(right.name);
	}

	@Override
	public String getAddress() {
		return definerID.toString();
	}

}