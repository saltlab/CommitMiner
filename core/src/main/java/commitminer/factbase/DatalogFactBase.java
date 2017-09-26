package commitminer.factbase;

import java.util.Map;

import org.deri.iris.api.basics.IPredicate;
import org.deri.iris.storage.IRelation;

import commitminer.analysis.SourceCodeFileChange;

/**
 * For fact databases that use datalog to query the facts post-analysis.
 */
public class DatalogFactBase extends FactBase {

	/* The fact database. */
	protected Map<IPredicate, IRelation> facts;

	public DatalogFactBase(Map<IPredicate, IRelation> facts,
			SourceCodeFileChange sourceCodeFileChange) {
		super(sourceCodeFileChange);
		this.facts = facts;
	}

}
