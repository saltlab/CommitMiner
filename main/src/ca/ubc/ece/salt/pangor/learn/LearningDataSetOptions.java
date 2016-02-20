package ca.ubc.ece.salt.pangor.learn;

import org.kohsuke.args4j.Option;

public class LearningDataSetOptions {

	@Option(name="-h", aliases={"--help"}, usage="Display the help file.")
	private boolean help = false;

	@Option(name="-ds", aliases={"--dataset"}, usage="The data set file to read.")
	private String dataSetPath = null;

	@Option(name="-m", aliases={"--metrics"}, usage="Print the metrics from the data set.")
	private boolean printMetrics = false;

	@Option(name="-c", aliases={"--clusters"}, usage="Print the clusters from the data set.")
	private boolean printClusters = false;

	@Option(name="-cc", aliases={"--complexity"}, usage="The maximum number of modified statements for a feature vector.")
	private int maxChangeComplexity = 10;

	@Option(name = "-a", aliases = { "--arff-path" }, usage = "Folder to write the ARFF files.")
	private String arffFolder = null;

	@Option(name = "-o", aliases = {"--oracle"}, usage="The oracle to use for cluster evaluation.")
	private String oracle = null;

	@Option(name = "-e", aliases = {"--epsilon"}, usage="The value of epsilon for clustering.")
	private double epsilon = 0.1;

	@Option(name = "-cw", aliases = {"--compexityWeight"}, usage="The weight of the complexity feature for clustering.")
	private double complexityWeight = 0.2;

	public String getArffFolder() {
		return this.arffFolder;
	}

	public boolean getHelp() {
		return this.help;
	}

	public String getDataSetPath() {
		return this.dataSetPath;
	}

	public String getOraclePath() {
		return this.oracle;
	}

	public boolean getPrintMetrics() {
		return this.printMetrics;
	}

	public boolean getPrintClusters() {
		return this.printClusters;
	}

	public Integer getMaxChangeComplexity() {
		return this.maxChangeComplexity;
	}

	public Double getEpsilon() {
		return this.epsilon;
	}

	public Double getComplexityWeight() {
		return this.complexityWeight;
	}

}