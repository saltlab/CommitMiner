package ca.ubc.ece.salt.pangor.analysis;

import java.util.List;
import java.util.Map;

import org.deri.iris.Configuration;
import org.deri.iris.EvaluationException;
import org.deri.iris.KnowledgeBase;
import org.deri.iris.api.IKnowledgeBase;
import org.deri.iris.api.basics.IPredicate;
import org.deri.iris.api.basics.IQuery;
import org.deri.iris.api.basics.IRule;
import org.deri.iris.storage.IRelation;

/**
 * The {@code DataSet} manages the data that is generated during an analysis.
 */
public abstract class DataSet {

	/** The datalog rules that will be part of each IRIS KnowledgeBase. **/
	private List<IRule> rules;

	/**
	 * The datalog queries that we will use to generate the feature vectors
	 * for this data set.
	 */
	private List<IQuery> queries;

	public DataSet(List<IRule> rules, List<IQuery> queries) {
		this.rules = rules;
		this.queries = queries;
	}

	/**
	 * Creates a new knowledge base from the provided facts and the rules and
	 * queries contained in this data set. Queries the facts to produce new
	 * vectors (rows) in the data set.
	 * @param facts The facts generated during the commit analysis.
	 * @throws EvaluationException when there is an error in the Datalog facts,
	 * 							   rules or queries.
	 */
	public void addCommitAnalysisResults(Commit commit, Map<IPredicate, IRelation> facts) throws EvaluationException {

		/* Since we are not doing any further analysis for the commit, we can
		 * create the knowledge base, run the queries and create the alerts. */
		IKnowledgeBase knowledgeBase = new KnowledgeBase(facts, this.rules, new Configuration());

		for(IQuery query : this.queries) {
			IRelation result = knowledgeBase.execute(query);
			registerAlert(query, result);
		}

	}

	/**
	 * A Datalog query has found a match. Register the result as an alert.
	 * @param query The query that matched.
	 * @param result The set of results from the query.
	 */
	protected abstract void registerAlert(IQuery query, IRelation result);

}