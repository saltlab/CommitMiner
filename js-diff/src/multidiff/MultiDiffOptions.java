package multidiff;

import org.kohsuke.args4j.Option;

public class MultiDiffOptions {

	@Option(name="-f", aliases={"--file"}, usage="The output file to write diff results to.")
	private String outFile = "out.csv";
	
	@Option(name="-o", aliases={"--original"}, usage="The original file.", required=true)
	private String original = null;
	
	@Option(name="-m", aliases={"--modified"}, usage="The modified file.", required=true)
	private String modified = null;
	
	@Option(name="-u", aliases={"--uri"}, 
			usage="The uri of the public repository (e.g., https://github.com/qhanam/JSRepairClass.git).", 
			required=true)
	private String host = null;
	
	@Option(name = "-tr", aliases = { "--threads" }, usage = "The number of threads to be used.")
	private Integer nThreads = 1;
	
	@Option(name="-h", aliases={"--help"}, usage="Display the help file.")
	private boolean help = false;

	public String getOutputFile() { return outFile; }
	public String getOriginal() { return original; }
	public String getModified() { return modified; }
	public String getURI() { return host; }
	public Integer getNThreads() { return nThreads; }
	
	public boolean getHelp() {
		return help;
	}

}