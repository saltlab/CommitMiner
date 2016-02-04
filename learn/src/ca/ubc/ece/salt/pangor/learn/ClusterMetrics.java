package ca.ubc.ece.salt.pangor.learn;

import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

/**
 * Stores the metrics for the clusters of one keyword.
 */
public class ClusterMetrics {

	/** Stores the clusters. **/
	public Map<Integer, Cluster> clusters;

	/** The total number of instances that were clustered. **/
	public int totalInstances;

	public ClusterMetrics() {
		this.clusters = new HashMap<Integer, Cluster>();
		this.totalInstances = 0;
	}

	/**
	 * Add the cluster and re-compute the metrics.
	 * @param clusterNumber The cluster number for the keyword.
	 * @param instanceID The ID for the instance.
	 * @param modifiedStatements The number of modified statements in the instance.
	 * @param keywords The list of modified keywords in the instance.
	 */
	public void addInstance(int clusterNumber, int instanceID,
							int modifiedStatements, List<String> keywords) {

		/* Get the cluster from the map. */
		Cluster cluster = this.clusters.get(clusterNumber);

		if(cluster == null) {
			/* First instance for cluster. */
			cluster = new Cluster(clusterNumber);
			this.clusters.put(clusterNumber, cluster);
		}

		/* Add the instance to the cluster. */
		cluster.addInstance(instanceID, modifiedStatements, keywords);

		/* Increment the total number of instances clustered. */
		this.totalInstances++;

	}

	/**
	 * @return The total number of instances in all clusters.
	 */
	public int getTotalInstances() {

		return this.totalInstances;
	}

	/**
	 * @return The average number of instances in each cluster.
	 */
	public int getAverageInsances() {
		return this.totalInstances / this.clusters.size();
	}

	/**
	 * @return The median number of instances in each cluster.
	 */
	public int getMedianInstances() {
		int[] instances = new int[this.clusters.size()];
		int i = 0;
		for(Cluster cluster : this.clusters.values()) {
			instances[i] = cluster.instances.size();
			i++;
		}
		return instances[instances.length / 2];
	}

	/**
	 * @return The number of clusters that were created.
	 */
	public int getClusterCount() {
		return this.clusters.size();
	}

	/**
	 * @return The average complexity of each cluster.
	 */
	public int getAverageComplexity() {
		int complexity = 0;
		for(Cluster cluster : this.clusters.values()) {
			complexity += cluster.getAverageModifiedStatements();
		}
		return Math.round(complexity / this.clusters.size());
	}

	/**
	 * @return The clusters for the keyword ranked by number of instances.
	 */
	public TreeSet<Cluster> getRankedClusters() {
		/* The clusters ranked by their size. */
		TreeSet<Cluster> rankedClusters = new TreeSet<Cluster>(new Comparator<Cluster>() {
			@Override
			public int compare(Cluster c1, Cluster c2) {
				if(c1.instances == c2.instances) return c1.toString().compareTo(c2.toString());
				else if(c1.instances.size() < c2.instances.size()) return 1;
				else return -1;
			}
		});

		/* Sort the clusters. */
		rankedClusters.addAll(this.clusters.values());

		return rankedClusters;
	}


	/**
	 * Prints a LaTex table from an ordered set of {@code ClusterMetrics}.
	 * @param metrics
	 * @return
	 */
	public static String getLatexTable(Set<ClusterMetrics> metrics) {
		String table = "\\begin{table*}\n";
		table += "\t\\centering\n";
		table += "\t\\caption{Clustering and Inspection Results}\n";
		table += "\t\\label{tbl:clusteringResults}\n";
		table += "{\\scriptsize\n";
		table += "\t\\begin{tabular}{ | l | r | r | r | r | r | }\n";
		table += "\t\t\\hline\n";
		table += "\t\t\\textbf{Keyword} & \\textbf{Clusters} & \\textbf{Tot Intances (I)}  & \\textbf{Avg I} & \\textbf{Mdn I} & \\textbf{Avg Complex.} \\\\ \\hline\n";

		for(ClusterMetrics metric : metrics) {
			table += "\t\t" + "ALL_KEYWORDS" + " & " + metric.clusters.size() + " & " + metric.totalInstances + " & " + Math.round(metric.getAverageInsances()) + " & " + metric.getMedianInstances() + " & " + metric.getAverageComplexity() + "\\\\\n";
		}

		table += "\t\t\\hline\n";
		table += "\t\\end{tabular}\n";
		table += "}\n";
		table += "\\end{table*}\n";
		return table.replace("_", "\\_");
	}

	@Override
	public String toString() {
		return "C = " + clusters + ", I = " + totalInstances + ", AVGI = " + this.getAverageInsances() + ", MDNI = " + this.getMedianInstances();
	}

}