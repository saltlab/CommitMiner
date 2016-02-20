package ca.ubc.ece.salt.pangor.learn;

import org.kohsuke.args4j.Option;
import org.kohsuke.args4j.spi.StringArrayOptionHandler;

public class LearningDataSetEvaluationOptions {

	@Option(name="-h", aliases={"--help"}, usage="Display the help file.")
	private boolean help = false;

	@Option(name="-ds", aliases={"--dataset"}, usage="The data set file to read.", handler = StringArrayOptionHandler.class, required=true)
	private String[] dataSetPaths = null;

	@Option(name="-cc", aliases={"--complexity"}, usage="The maximum number of modified statements for a feature vector.")
	private int maxChangeComplexity = 6;

	@Option(name = "-o", aliases = {"--oracle"}, usage="The oracle to use for cluster evaluation.", required=true)
	private String oracle = null;

	public boolean getHelp() {
		return this.help;
	}

	public String[] getDataSetPaths() {
		return this.dataSetPaths;
	}

	public String getOraclePath() {
		return this.oracle;
	}

	public Integer getMaxChangeComplexity() {
		return this.maxChangeComplexity;
	}

}