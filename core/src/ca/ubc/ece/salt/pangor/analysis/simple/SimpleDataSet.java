package ca.ubc.ece.salt.pangor.analysis.simple;

import java.util.LinkedList;
import java.util.List;

import ca.ubc.ece.salt.pangor.analysis.DataSet;

/**
 * A simple data set used for quick analysis prototypes and testing.
 */
public class SimpleDataSet<A> implements DataSet<A> {

	/** The alerts in this data set. **/
	private List<A> alerts;

	public SimpleDataSet() {
		this.alerts = new LinkedList<A>();
	}

	@Override
	public void registerAlert(A alert) throws Exception {
		this.alerts.add(alert);
	}

	/**
	 * @return The alerts this data set contains.
	 */
	public List<A> getAlerts() {
		return this.alerts;
	}

	/**
	 * @return true if {@code alert} is in this dataset.
	 */
	public boolean contains(A alert) {
		return alerts.contains(alert);
	}

	/**
	 * @return A string containing the serialized alerts.
	 */
	public String printAlerts() {
		String file = "";
		int i = 1;
		for(A alert : this.alerts) {
			file += i + ", " + alert.toString() + "\n";
			i++;
		}
		return file;
	}

}
