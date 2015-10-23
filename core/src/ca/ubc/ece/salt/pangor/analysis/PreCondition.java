package ca.ubc.ece.salt.pangor.analysis;


/**
 * Described a pre-condition found by the analysis.
 */
public interface PreCondition {

	@Override
	public abstract boolean equals(Object o);

	@Override
	public abstract int hashCode();

}
