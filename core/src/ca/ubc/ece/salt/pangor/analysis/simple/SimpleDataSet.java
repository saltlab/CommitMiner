package ca.ubc.ece.salt.pangor.analysis.simple;

import java.util.LinkedList;
import java.util.List;

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
	private List<SimpleAlert> alerts;

	public SimpleDataSet(List<IRule> rules, List<IQuery> queries) {
		super(rules, queries);
		this.alerts = new LinkedList<SimpleAlert>();
	}

	@Override
	public void registerAlert(Commit commit, IQuery query, IRelation results) {

		/* Iterate through the tuples that are members of the relation and
		 * add them as alerts. */
		for(int i = 0; i < results.size(); i++) {

			ITuple tuple = results.get(i);

			/* Create a SimpleAlert and store it in the data set. */
			this.alerts.add(new SimpleAlert(commit, "[" + query.toString() + "](" + tuple.toString() + ")"));

		}

	}

	/**
	 * @return The alerts this data set contains.
	 */
	public List<SimpleAlert> getAlerts() {
		return this.alerts;
	}

	/**
	 * @return true if {@code alert} is in this data set.
	 */
	public boolean contains(SimpleAlert alert) {
		return alerts.contains(alert);
	}

	/**
	 * @return A string containing the serialized alerts.
	 */
	public String printAlerts() {
		String file = "";
		int i = 1;
		for(SimpleAlert alert : this.alerts) {
			file += i + ", " + alert.toString() + "\n";
			i++;
		}
		return file;
	}

}
