package commitminer.learn;

import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

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
	public Map<Integer, String> instances;

	/** Tracks the total number of statements that were modified in the cluster. **/
	private int modifiedStatements;

	/** Tracks the projects that are represented in the cluster. **/
	public Set<String> projects;

	/** Tracks how frequently which keywords are modified in the cluster. */
	public Map<String, Integer> keywords;

	public Cluster(int cluster) {
		this.cluster = cluster;
		this.modifiedStatements = 0;
		this.projects = new HashSet<String>();
		this.instances = new HashMap<Integer, String>();
		this.keywords = new HashMap<String, Integer>();
	}

	/**
	 * Add the instance to the cluster.
	 * @param instanceID The ID for the instance.
	 * @param modifiedStatements The number of modified statements in the instance.
	 * @param project The project that the commit came from
	 * @param expected The expected class for the instance.
	 * @param keywords The list of modified keywords in the instance.
	 */
	public void addInstance(int instanceID, int modifiedStatements,
							String project, String expected,
							List<String> keywords) {
		this.modifiedStatements += modifiedStatements;
		this.instances.put(instanceID, expected);
		this.projects.add(project);
		for(String modified : keywords) {
			int f = this.keywords.containsKey(modified) ? this.keywords.get(modified) + 1 : 1;
			this.keywords.put(modified, f);
		}
	}

	/**
	 * @return The average number of modified statements.
	 */
	public int getAverageModifiedStatements() {
		double avg = this.modifiedStatements / this.instances.size();
		return (int) Math.round(avg);
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
			if(entry.getValue()/this.instances.size() >= 0.6) {
				modified += entry.getKey() + " ";
			}
		}
		if(modified.length() > 1) modified = modified.substring(0, modified.length() - 1);
		return modified + "}";
	}

	/**
	 * @return The number of keywords that are modified in > 90% of feature vectors.
	 */
	public int getModifiedKeywordSize() {
		int count = 0;
		for(Entry<String, Integer> entry : keywords.entrySet()) {
			if(entry.getValue()/this.instances.size() >= 0.6) {
				count++;
			}
		}
		return count;
	}

	/**
	 * @return The number of projects represented in the cluster.
	 */
	public int getProjectCount() {
		return this.projects.size();
	}

	/**
	 * Used to evaluate the cluster.
	 * @return a map of expected classes to frequency within the cluster.
	 */
	public List<Entry<String, Integer>> getClusterComposition() {

		/* Compute the frequency of each class. */
		Map<String, Integer> composition = new HashMap<String, Integer>();
		for(String expected : this.instances.values()) {
			Integer count = composition.get(expected);
			count = count == null ? 1 : count + 1;
			composition.put(expected, count);
		}

		/* Sort the list according to most frequent class. */
		List<Entry<String, Integer>> sorted = new LinkedList<Entry<String, Integer>>(composition.entrySet());
		Collections.sort(sorted, new Comparator<Entry<String, Integer>>() {
			@Override
			public int compare(Entry<String, Integer> l, Entry<String, Integer> r) {
				return l.getValue().compareTo(r.getValue());
			}
		});

		return sorted;

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
		return this.getModifiedKeywords() + ": ClusterID = " + cluster + ", Instances = " + instances.size() + ", Complexity = " + this.getAverageModifiedStatements() + ", BasicChanges = " + this.getModifiedKeywordSize() + ", ProjectCount = " + this.getProjectCount();
	}

}