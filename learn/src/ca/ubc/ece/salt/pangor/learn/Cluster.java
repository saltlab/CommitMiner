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

	/** Tracks the number of instances in the cluster. **/
	public int instances;

	/** Tracks the total number of statements that were modified in the cluster. **/
	public int modifiedStatements;

	/** Tracks how frequently which keywords are modified in the cluster. */
	public Map<String, Integer> keywords;

	public Cluster(int cluster, int modifiedStatements, List<String> keywords) {
		this.cluster = cluster;
		this.modifiedStatements = modifiedStatements;
		this.instances = 1;
		this.keywords = new HashMap<String, Integer>();

		for(String modified : keywords) this.keywords.put(modified, 1);
	}

	/**
	 * @return The average number of modified statements.
	 */
	public int getAverageModifiedStatements() {
		return Math.round(this.modifiedStatements / this.instances);
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
			if(entry.getValue()/this.instances >= 0.9) {
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