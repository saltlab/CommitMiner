package ca.ubc.ece.salt.pangor.analysis.simple;

import java.util.LinkedList;
import java.util.List;

import ca.ubc.ece.salt.pangor.analysis.DataSet;

/**
 * A simple data set used for quick analysis prototypes and testing.
 */
public class SimpleDataSet implements DataSet<SimpleAlert> {

	/** The alerts in this data set. **/
	private List<SimpleAlert> alerts;

	public SimpleDataSet() {
		this.alerts = new LinkedList<SimpleAlert>();
	}

	@Override
	public void registerAlert(SimpleAlert alert) throws Exception {
		this.alerts.add(alert);
	}

	/**
	 * @return The alerts this data set contains.
	 */
	public List<SimpleAlert> getAlerts() {
		return this.alerts;
	}

	/**
	 * @return true if {@code alert} is in this dataset.
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
