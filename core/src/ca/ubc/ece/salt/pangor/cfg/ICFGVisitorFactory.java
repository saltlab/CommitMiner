package ca.ubc.ece.salt.pangor.cfg;

import java.util.Map;

import org.deri.iris.api.basics.IPredicate;
import org.deri.iris.storage.IRelation;

import ca.ubc.ece.salt.pangor.analysis.SourceCodeFileChange;


/**
 * Builds new instances of a domain analysis.
 */
public interface ICFGVisitorFactory {
	ICFGVisitor newInstance(SourceCodeFileChange sourceCodeFileChange, Map<IPredicate, IRelation> facts);
}
