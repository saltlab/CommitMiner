package ca.ubc.ece.salt.pangor.learn;

import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeSet;

/**
 * Stores the metrics for the clusters of one keyword.
 */
public class ClusterMetrics {

	/** Stores the clusters. **/
	public Map<Integer, Cluster> clusters;

	/** The total number of instances. **/
	public int totalInstances;

	/** The total number of instances that were clustered. **/
	public int totalClusteredInstances;

	/** The value of epsilon that was used by DBScan. **/
	public double epsilon;

	/** The confusion matrix for an evaluation. [TP,FP,TN,FN] **/
	public int[] confusionMatrix;

	/** The results matrix. **/

	public ClusterMetrics() {
		this.clusters = new HashMap<Integer, Cluster>();
		this.totalClusteredInstances = 0;
		this.totalInstances = 0;
		this.epsilon = 0;
	}

	/**
	 * @param totalInstances The total number of instances in the data set.
	 */
	public void setTotalInstances(int totalInstances) {
		this.totalInstances = totalInstances;
	}

	/**
	 * @param epsilon The value of epsilon (a distance measure) that was used
	 * 				  by DBScan.
	 */
	public void setEpsilon(double epsilon) {
		this.epsilon = epsilon;
	}

	/**
	 * Add the cluster and re-compute the metrics.
	 * @param clusterNumber The cluster number for the keyword.
	 * @param instanceID The ID for the instance.
	 * @param expected The expected class for the instance.
	 * @param modifiedStatements The number of modified statements in the instance.
	 * @param keywords The list of modified keywords in the instance.
	 */
	public void addInstance(int clusterNumber, int instanceID, String expected,
							int modifiedStatements, List<String> keywords) {

		/* Get the cluster from the map. */
		Cluster cluster = this.clusters.get(clusterNumber);

		if(cluster == null) {
			/* First instance for cluster. */
			cluster = new Cluster(clusterNumber);
			this.clusters.put(clusterNumber, cluster);
		}

		/* Add the instance to the cluster. */
		cluster.addInstance(instanceID, modifiedStatements, expected, keywords);

		/* Increment the total number of instances clustered. */
		this.totalClusteredInstances++;

	}

	/**
	 * @return The total number of instances in all clusters.
	 */
	public int getTotalInstances() {

		return this.totalClusteredInstances;
	}

	/**
	 * @return The average number of instances in each cluster.
	 */
	public int getAverageInsances() {
		return this.totalClusteredInstances / this.clusters.size();
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
	 * Compute the evaluation metrics if an oracle was provided.
	 * @param oracle Key = feature vector ID, Value = class
	 */
	public EvaluationResult evaluate(Map<Integer, String> oracle) {

		/* The metrics for the confusion matrix. */
		double tp = 0, fp = 0, tn = 0, fn = 0;
		double classified = 0;
		double p = 0, r = 0, f = 0, fm = 0, inspect = 0, captured = 0;

		/* The % of a class that makes up a cluster. */
		Map<String, Double> clusterCompositions = new HashMap<String, Double>();

		/* The % of a cluster that makes up a class. */
		Map<String, Double> classCompositions = new HashMap<String, Double>();

		/* Create a map of expected classes. */
		Map<String, List<Integer>> expected = new HashMap<String, List<Integer>>();
		for(Entry<Integer, String> entity : oracle.entrySet()) {

			if(!entity.getValue().equals("?")) {
				List<Integer> instancesInClass = expected.get(entity.getValue());
				if(instancesInClass == null) {
					instancesInClass = new LinkedList<Integer>();
					expected.put(entity.getValue(), instancesInClass);
				}
				instancesInClass.add(entity.getKey());
				classified++;	// All classified values are considered false negatives by default.
			}

		}

		/* Compute the composition of each cluster. */
		Set<String> actual = new HashSet<String>();
		for(Cluster cluster : this.clusters.values()) {

			int tpForCluster = 0;

			System.out.println("Composition of cluster " + cluster.getClusterID());

			List<Entry<String, Integer>> composition = cluster.getClusterComposition();
			for(Entry<String, Integer> entry : composition) {

				String classID = entry.getKey();

				if(!classID.equals("?")) {

					/* A cluster ~= a class when:
					 * 	1. The cluster contains >= 50% of the instances in a class (we don't need the list then).
					 * 	2. The class is the largest group in the dataset (this may be flexible). */

					/* Get the base metrics. */
					double intersection = entry.getValue();
					double classSize = expected.get(entry.getKey()).size();
					double clusterSize = cluster.instances.size();

					/* Compute the percent of the class this cluster represents. */
					double percentOfClass = intersection / classSize;

					/* Compute the percent of the cluster this class represents. */
					double percentOfCluster = intersection / clusterSize;

					/* If both are > 50%, all these are TPs */
					if(percentOfClass >= 0 && percentOfCluster >= 0 && intersection >= 2) {
						actual.add(String.valueOf(expected.get(entry.getKey())));
						tp += intersection;
						tpForCluster += intersection;

						/* Check the cluster composition. */
						Double clusterComposition = clusterCompositions.get(classID);
						clusterComposition = clusterComposition == null ? 0 : clusterComposition;
						if(percentOfCluster > clusterComposition) clusterCompositions.put(classID, percentOfCluster);

						/* Check the class composition. */
						Double classComposition = classCompositions.get(classID);
						classComposition = classComposition == null ? 0 : classComposition;
						if(percentOfClass > classComposition) classCompositions.put(classID, percentOfClass);
					}


					System.out.println(entry.getKey() + ": " + entry.getValue()
							+ "(" + Math.round(100 * percentOfCluster) + "% of cluster)"
							+ "(" + Math.round(100 * percentOfClass) + "% of class)");
				}
				else {
					System.out.println("?: " + entry.getValue());
				}

			}

			fp += cluster.instances.size() - tpForCluster;

		}

		/* Compute the number of true and false negatives.
		 * FN = # of instances with a class - #TPs
		 * TN = # of instances without a class - #FPs */

		fn = classified - tp;
		tn = this.totalInstances - tp - fp - fn;

		p = tp/(tp+fp);
		r = tp/(tp+fn);

		f = 2*((p*r)/(p+r));
		fm = Math.sqrt((tp/(tp+fp)) * (tp/(tp+fn)));

		inspect = (tp + fp) / (tp + fp + tn + fn);

		captured = ((double)actual.size()) / ((double)expected.size());

		/* Store the results. */
		ConfusionMatrix confusionMatrix
			= new ConfusionMatrix((int)tp,(int)fp, (int)tn, (int)fn);

		EvaluationResult evaluationResult
			= new EvaluationResult(confusionMatrix, this.epsilon, p, r, f, fm,
					inspect, captured, clusterCompositions, classCompositions);

		return evaluationResult;

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
			table += "\t\t" + "ALL_KEYWORDS" + " & " + metric.clusters.size() + " & " + metric.totalClusteredInstances + " & " + Math.round(metric.getAverageInsances()) + " & " + metric.getMedianInstances() + " & " + metric.getAverageComplexity() + "\\\\\n";
		}

		table += "\t\t\\hline\n";
		table += "\t\\end{tabular}\n";
		table += "}\n";
		table += "\\end{table*}\n";
		return table.replace("_", "\\_");
	}

	@Override
	public String toString() {
		return "C = " + clusters + ", I = " + totalClusteredInstances + ", AVGI = " + this.getAverageInsances() + ", MDNI = " + this.getMedianInstances();
	}

	public class ConfusionMatrix {
		public int tp, fp, tn, fn;
		public ConfusionMatrix(int tp, int fp, int tn, int fn) {
			this.tp = tp;
			this.fp = fp;
			this.tn = tn;
			this.fn = fn;
		}
	}

}