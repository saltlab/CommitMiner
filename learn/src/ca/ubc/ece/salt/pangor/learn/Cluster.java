package ca.ubc.ece.salt.pangor.learn;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

/**
 * Stores meta data for a cluster. Used to summarize clustering results.
 */
public class Cluster {

	/** The cluster number. **/
	public int cluster;

	/**
	 * Tracks the instances in the cluster. The key is the instance ID while
	 * the value is the actual class.
	 */
	public Map<Integer, Integer> instances;

	/** Tracks the total number of statements that were modified in the cluster. **/
	public int modifiedStatements;

	/** Tracks how frequently which keywords are modified in the cluster. */
	public Map<String, Integer> keywords;

	public Cluster(int cluster) {
		this.cluster = cluster;
		this.modifiedStatements = 0;
		this.instances = new HashMap<Integer, Integer>();
		this.keywords = new HashMap<String, Integer>();
	}

	/**
	 * Add the instance to the cluster.
	 * @param instanceID The ID for the instance.
	 * @param modifiedStatements The number of modified statements in the instance.
	 * @param keywords The list of modified keywords in the instance.
	 */
	public void addInstance(int instanceID, int modifiedStatements, List<String> keywords) {
		this.modifiedStatements += modifiedStatements;
		this.instances.put(instanceID, -1);
		for(String modified : keywords) {
			int f = this.keywords.containsKey(modified) ? this.keywords.get(modified) + 1 : 1;
			this.keywords.put(modified, f);
		}
	}

	/**
	 * @return The average number of modified statements.
	 */
	public int getAverageModifiedStatements() {
		return Math.round(this.modifiedStatements / this.instances.size());
	}

	/**
	 * @return The unique ID for the cluster.
	 */
	public String getClusterID() {
		return String.valueOf(this.cluster);
	}

	/**
	 * @return The keywords that are modified in > 90% of feature vectors.
	 */
	public String getModifiedKeywords() {
		String modified = "{";
		for(Entry<String, Integer> entry : keywords.entrySet()) {
			if(entry.getValue()/this.instances.size() >= 0.9) {
				modified += entry.getKey() + ",";
			}
		}
		return modified.substring(0, modified.length() - 1) + "}";
	}

	@Override
	public boolean equals(Object o) {
		if(o instanceof Cluster) {
			Cluster c = (Cluster) o;
			if(this.cluster == c.cluster) return true;
		}
		return false;
	}

	@Override
	public int hashCode() {
		return Integer.hashCode(this.cluster);
	}

	@Override
	public String toString() {
		return this.getModifiedKeywords() + ": C = " + cluster + ", I = " + instances + ", X = " + this.getAverageModifiedStatements();
	}

}