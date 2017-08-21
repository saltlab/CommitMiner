package multidiff;

import org.kohsuke.args4j.Option;

import commitminer.analysis.options.Options;

public class MultiDiffOptions {

	@Option(name="-f", aliases={"--file"}, usage="The output file to write diff results to.")
	private String outFile = "out.csv";
	
	@Option(name="-o", aliases={"--original"}, usage="The original file.")
	private String original = null;
	
	@Option(name="-m", aliases={"--modified"}, usage="The modified file.")
	private String modified = null;
	
	@Option(name="-u", aliases={"--uri"}, 
			usage="The uri of the public repository (e.g., https://github.com/qhanam/JSRepairClass.git).", 
			required=true)
	private String host = null;
	
	@Option(name = "-tr", aliases = { "--threads" }, usage = "The number of threads to be used.")
	private Integer nThreads = 1;
	
	@Option(name = "-d", aliases={"--diff"}, usage="The diff method (GUMTREE|MEYERS)")
	private Options.DiffMethod diffMethod = Options.DiffMethod.GUMTREE;
	
	@Option(name = "-ci", aliases={"--changeimpact"}, usage="The change impact method (MULTIDIFF|DEPENDENCIES).")
	private Options.ChangeImpact changeImpact = Options.ChangeImpact.MULTIDIFF;

	@Option(name="-h", aliases={"--help"}, usage="Display the help file.")
	private boolean help = false;
	
	public String getOutputFile() { return outFile; }
	public String getOriginal() { return original; }
	public String getModified() { return modified; }
	public String getURI() { return host; }
	public Integer getNThreads() { return nThreads; }
	public Options.DiffMethod getDiffMethod() { return diffMethod; }

	public Options.ChangeImpact getChangeImpact() { return changeImpact; }
	
	public boolean getHelp() {
		return help;
	}

}