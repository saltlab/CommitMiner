package ca.ubc.ece.salt.pangor.analysis.flow.abstractdomain;

/**
 * Computes abstract memory addresses (hashes) for abstract values, objects
 * and continuations. The different hashes are be computed depending on the
 * desired context sensitivity.
 *
 * This implements the technique in "Abstracting Abstract Machines".
 */
public abstract class SmartHash {
	// TODO: Once we implement inter-procedural analysis.
	//		 We only need K-call-site sensitivity.
}
