package ca.ubc.ece.salt.pangor.analysis.simple;

import java.util.LinkedList;
import java.util.List;

import org.deri.iris.api.IKnowledgeBase;
import org.deri.iris.api.basics.IQuery;
import org.deri.iris.api.basics.IRule;
import org.deri.iris.api.basics.ITuple;
import org.deri.iris.storage.IRelation;

import ca.ubc.ece.salt.pangor.analysis.Commit;
import ca.ubc.ece.salt.pangor.analysis.DataSet;

/**
 * A simple data set used for quick analysis prototypes and testing.
 */
public class SimpleDataSet extends DataSet {

	/** The alerts in this data set. **/
	private List<SimpleFeatureVector> alerts;

	public SimpleDataSet(List<IRule> rules, List<IQuery> queries) {
		super(rules, queries);
		this.alerts = new LinkedList<SimpleFeatureVector>();
	}

	@Override
	public void registerAlerts(Commit commit, IKnowledgeBase knowledgeBase) throws Exception {

		for(IQuery query : this.queries) {

			IRelation results = knowledgeBase.execute(query);

			/* Iterate through the tuples that are members of the relation and
			 * add them as alerts. */
			for(int i = 0; i < results.size(); i++) {

				ITuple tuple = results.get(i);

				/* Create a SimpleAlert and store it in the data set. */
				this.alerts.add(new SimpleFeatureVector(commit, "[" + query.toString() + "](" + tuple.toString() + ")"));

			}

		}

	}

	/**
	 * @return The alerts this data set contains.
	 */
	public List<SimpleFeatureVector> getAlerts() {
		return this.alerts;
	}

	/**
	 * @return true if {@code alert} is in this data set.
	 */
	public boolean contains(SimpleFeatureVector alert) {
		return alerts.contains(alert);
	}

	/**
	 * @return A string containing the serialized alerts.
	 */
	public String printAlerts() {
		String file = "";
		int i = 1;
		for(SimpleFeatureVector alert : this.alerts) {
			file += i + ", " + alert.toString() + "\n";
			i++;
		}
		return file;
	}

}
