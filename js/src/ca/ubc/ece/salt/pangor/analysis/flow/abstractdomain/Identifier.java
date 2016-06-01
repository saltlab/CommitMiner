package ca.ubc.ece.salt.pangor.analysis.flow.abstractdomain;


/**
 * A variable identifier combined with a change lattice.
 */
public class Identifier {

	public String name;
	public Change change;

	/**
	 * Use for standard lookup operations when the change type does not matter.
	 * @param name The name of the identifier to inject.
	 */
	public Identifier(String name) {
		this.name = name;
		this.change = Change.bottom();
	}

	/**
	 * Use for standard lookup operations when the change type does not matter.
	 * @param name The name of the identifier to inject.
	 * @param change How the identifier was changed.
	 */
	public Identifier(String name, Change change) {
		this.name = name;
		this.change = change;
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

}