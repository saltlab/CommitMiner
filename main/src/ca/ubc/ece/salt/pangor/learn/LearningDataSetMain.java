package ca.ubc.ece.salt.pangor.learn;

import java.util.Arrays;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;

import weka.core.WekaException;
import ca.ubc.ece.salt.gumtree.ast.ClassifiedASTNode.ChangeType;
import ca.ubc.ece.salt.pangor.api.KeywordDefinition.KeywordType;
import ca.ubc.ece.salt.pangor.api.KeywordUse;
import ca.ubc.ece.salt.pangor.api.KeywordUse.KeywordContext;
import ca.ubc.ece.salt.pangor.learn.analysis.KeywordFilter;
import ca.ubc.ece.salt.pangor.learn.analysis.KeywordFilter.FilterType;
import ca.ubc.ece.salt.pangor.learn.analysis.LearningDataSet;
import ca.ubc.ece.salt.pangor.learn.analysis.LearningMetrics;
import ca.ubc.ece.salt.pangor.learn.analysis.LearningMetrics.KeywordFrequency;

/**
 * Creates clusters similar repairs.
 *
 * 1. Reads the results of the data mining task ({@code LearningAnalysisMain})
 * 2. Builds a CSV file of WEKA-supported feature vectors
 * 3. Clusters the feature vectors
 */
public class LearningDataSetMain {

	protected static final Logger logger = LogManager.getLogger(LearningDataSetMain.class);

	/**
	 * Creates the learning data sets for extracting repair patterns.
	 * @param args
	 * @throws Exception
	 */
	public static void main(String[] args) throws Exception {

		KeywordFilter nofilter = new KeywordFilter(FilterType.INCLUDE,
				KeywordType.UNKNOWN, KeywordContext.UNKNOWN, ChangeType.UNKNOWN,
				"", "");

		LearningDataSetOptions options = new LearningDataSetOptions();
		CmdLineParser parser = new CmdLineParser(options);

		try {
			parser.parseArgument(args);
		} catch (CmdLineException e) {
			LearningDataSetMain.printUsage(e.getMessage(), parser);
			return;
		}

		/* Print the help page. */
		if(options.getHelp()) {
			LearningDataSetMain.printHelp(parser);
			return;
		}

		/* Re-construct the data set. */
		LearningDataSet dataSet = LearningDataSet.createLearningDataSet(
				options.getDataSetPath(), options.getOraclePath(),
				Arrays.asList(nofilter), null,
				options.getMaxChangeComplexity());

		/* Print the metrics from the data set. */
		if(options.getPrintMetrics()) {
			LearningMetrics metrics = dataSet.getMetrics();
			for(KeywordFrequency frequency : metrics.changedKeywordFrequency) {
				System.out.println(frequency.keyword + " : " + frequency.frequency);
			}
		}

		/* Get the clusters for the data set. */
		if(options.getPrintClusters()) {

			/* The clusters stored according to their keyword. */
			Set<ClusterMetrics> keywordClusters = new TreeSet<ClusterMetrics>(new Comparator<ClusterMetrics>() {
				@Override
				public int compare(ClusterMetrics c1, ClusterMetrics c2) {
					if(c1.totalInstances == c2.totalInstances) return c1.toString().compareTo(c2.toString());
					else if(c1.totalInstances < c2.totalInstances) return 1;
					else return -1;
				}
			});

			/* The clusters ranked by their size. */
			Set<Cluster> rankedClusters = new TreeSet<Cluster>(new Comparator<Cluster>() {
				@Override
				public int compare(Cluster c1, Cluster c2) {
					if(c1.instances == c2.instances) return c1.toString().compareTo(c2.toString());
					else if(c1.instances.size() < c2.instances.size()) return 1;
					else return -1;
				}
			});

			/* Add a filter that includes all rows. */
			List<KeywordFilter> rowFilters = new LinkedList<KeywordFilter>();

			KeywordFilter insertedFilter = new KeywordFilter(FilterType.INCLUDE,
				KeywordType.UNKNOWN, KeywordContext.UNKNOWN,
				ChangeType.INSERTED, "", "");
			rowFilters.add(insertedFilter);

			KeywordFilter removedFilter = new KeywordFilter(FilterType.INCLUDE,
				KeywordType.UNKNOWN, KeywordContext.UNKNOWN,
				ChangeType.REMOVED, "", "");
			rowFilters.add(removedFilter);

			/* Re-construct the data set. */
			LearningDataSet clusteringDataSet =
					LearningDataSet.createLearningDataSet(
							options.getDataSetPath(),
							options.getOraclePath(),
							rowFilters,
							new LinkedList<KeywordUse>(), // columnFilters
							options.getMaxChangeComplexity());

			/* Pre-process the file. */
			clusteringDataSet.preProcess();

			/* Get the clusters. */
			try {

				ClusterMetrics clusterMetrics = new ClusterMetrics();
				clusteringDataSet.getWekaClusters(clusterMetrics);

				/* Add the clusters to the sorted list. */
				rankedClusters.addAll(clusterMetrics.clusters.values());

				/* Save arff file */
				if (options.getArffFolder() != null)
					clusteringDataSet.writeArffFile(options.getArffFolder(), "ALL_KEYWORDS.arff");

				/* We only have one ClusterMetrics now. */
				keywordClusters.add(clusterMetrics);

				/* Write the evaluation results from the clustering. */
				clusteringDataSet.evaluate(clusterMetrics);


			} catch (WekaException ex) {
				logger.error("Weka error on building clusters.", ex);
			}

			int i = 0;
			for(Cluster cluster : rankedClusters) {
				System.out.println(i + "\t" + cluster);
				i++;
			}

			System.out.println(ClusterMetrics.getLatexTable(keywordClusters));

		}

	}

	/**
	 * Prints the help file for main.
	 * @param parser The args4j parser.
	 */
	private static void printHelp(CmdLineParser parser) {
        System.out.print("Usage: DataSetMain ");
        parser.setUsageWidth(Integer.MAX_VALUE);
        parser.printSingleLineUsage(System.out);
        System.out.println("\n");
        parser.printUsage(System.out);
        System.out.println("");
        return;
	}

	/**
	 * Prints the usage of main.
	 * @param error The error message that triggered the usage message.
	 * @param parser The args4j parser.
	 */
	private static void printUsage(String error, CmdLineParser parser) {
        System.out.println(error);
        System.out.print("Usage: DataSetMain ");
        parser.setUsageWidth(Integer.MAX_VALUE);
        parser.printSingleLineUsage(System.out);
        System.out.println("");
        return;
	}

}