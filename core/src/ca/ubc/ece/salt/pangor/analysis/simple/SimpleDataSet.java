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
	 * @return A string containing the serialized alerts.
	 */
	public String printAlerts() {
		String file = "";
		for(A alert : this.alerts) {
			file += alert.toString();
		}
		return file;
	}

}
