package ca.ubc.ece.salt.pangor.analysis;


/**
 * Describes an anti-pattern found by an analysis.
 */
public interface AntiPattern {

	@Override
	public abstract boolean equals(Object o);

	@Override
	public abstract int hashCode();

}
