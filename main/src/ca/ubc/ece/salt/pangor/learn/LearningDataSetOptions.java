package ca.ubc.ece.salt.pangor.learn;

import org.kohsuke.args4j.Option;

public class LearningDataSetOptions {

	@Option(name="-h", aliases={"--help"}, usage="Display the help file.")
	private boolean help = false;

	@Option(name="-ds", aliases={"--dataset"}, usage="The data set file to read.")
	private String dataSetPath = null;

	@Option(name="-f", aliases={"--filtered"}, usage="The file to write the filtered data set to.")
	private String filteredPath = null;

	@Option(name="-m", aliases={"--metrics"}, usage="Print the metrics from the data set.")
	private boolean printMetrics = false;

	@Option(name="-c", aliases={"--clusters"}, usage="Print the clusters from the data set.")
	private boolean printClusters = false;

	@Option(name = "-a", aliases = { "--arff-path" }, usage = "Folder to write the ARFF files.")
	private String arffFolder = null;

	public String getArffFolder() {
		return this.arffFolder;
	}

	public boolean getHelp() {
		return this.help;
	}

	public String getDataSetPath() {
		return this.dataSetPath;
	}

	public String getFilteredPath() {
		return this.filteredPath;
	}

	public boolean getPrintMetrics() {
		return this.printMetrics;
	}

	public boolean getPrintClusters() {
		return this.printClusters;
	}

}