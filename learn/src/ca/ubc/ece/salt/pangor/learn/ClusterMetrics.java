package ca.ubc.ece.salt.pangor.learn;

import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import ca.ubc.ece.salt.pangor.api.KeywordUse;

/**
 * Stores the metrics for the clusters of one keyword.
 */
public class ClusterMetrics {

	/** The keyword which produced these clusters. **/
	public KeywordUse keyword;

	/** Stores the clusters. **/
	public Map<Cluster, Cluster> clusters;

	/** The total number of instances that were clustered. **/
	public int totalInstances;

	public ClusterMetrics(KeywordUse keyword) {
		this.keyword = keyword;
		this.clusters = new HashMap<Cluster, Cluster>();
		this.totalInstances = 0;
	}

	/**
	 * Add the cluster and re-compute the metrics.
	 * @param keyword The keyword that this cluster was produced for.
	 * @param clusterNumber The cluster number for the keyword.
	 * @param modifiedStatements The number of modified statements in the instance.
	 * @param keywords The list of modified keywords in the instance.
	 */
	public void addInstance(int clusterNumber, int modifiedStatements,
							List<String> keywords) {

		/* Get the cluster from the map. */
		Cluster instance = new Cluster(this.keyword, clusterNumber, modifiedStatements, keywords);
		Cluster cluster = this.clusters.get(instance);

		if(cluster == null) {
			/* First instance for cluster. */
			cluster = instance;
		}
		else {
			/* Update the cluster metrics. */
			cluster.instances++;
			cluster.modifiedStatements += modifiedStatements;
			for(String modified : keywords) {
				int f = cluster.keywords.containsKey(modified) ? cluster.keywords.get(modified) + 1 : 1;
				cluster.keywords.put(modified, f);
			}
		}

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
			instances[i] = cluster.instances;
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
	 * @return The clusters for the keyword ranked by number of instances.
	 */
	public TreeSet<Cluster> getRankedClusters() {
		/* The clusters ranked by their size. */
		TreeSet<Cluster> rankedClusters = new TreeSet<Cluster>(new Comparator<Cluster>() {
			@Override
			public int compare(Cluster c1, Cluster c2) {
				if(c1.instances == c2.instances) return c1.toString().compareTo(c2.toString());
				else if(c1.instances < c2.instances) return 1;
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
		table += "\t\\begin{tabular}{ | l | r | r | r | r | r | r | }\n";
		table += "\t\t\\hline\n";
		table += "\t\t\\textbf{Keyword} & \\textbf{TotC} & \\textbf{Clusters} & \\textbf{AvgI} & \\textbf{MdnI. Size} & \\textbf{BG} & \\textbf{RG} \\\\ \\hline\n";

		for(ClusterMetrics metric : metrics) {
			table += "\t\t" + metric.keyword + " & " + metric.totalInstances + " & " + metric.clusters.size() + " & " + Math.round(metric.getAverageInsances()) + " & " + metric.getMedianInstances() + " & & \\\\\n";
		}

		table += "\t\t\\hline\n";
		table += "\t\\end{tabular}\n";
		table += "}\n";
		table += "\\end{table*}\n";
		return table.replace("_", "\\_");
	}

	@Override
	public String toString() {
		return keyword.toString() + ": C = " + clusters + ", I = " + totalInstances + ", AVGI = " + this.getAverageInsances() + ", MDNI = " + this.getMedianInstances();
	}

}