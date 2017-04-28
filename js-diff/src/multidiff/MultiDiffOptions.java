package multidiff;

import org.kohsuke.args4j.Option;

public class MultiDiffOptions {

	@Option(name="-f", aliases={"--file"}, usage="The partial html file to write the annotated diff to.")
	private String outFile = null;
	
	@Option(name="-o", aliases={"--original"}, usage="The original file.", required=true)
	private String original = null;
	
	@Option(name="-m", aliases={"--modified"}, usage="The modified file.", required=true)
	private String modified = null;
	
	@Option(name="-du", aliases={"--defuse"}, usage="Turn on function def/use output.")
	private boolean defUse = false;

	@Option(name="-var", aliases={"--variable"}, usage="Turn on variable differences.")
	private boolean varDiff = false;

	@Option(name="-val", aliases={"--value"}, usage="Turn on value differences.")
	private boolean valDiff = false;

	@Option(name="-con", aliases={"--control"}, usage="Turn on control differences.")
	private boolean conDiff = false;

	@Option(name="-h", aliases={"--help"}, usage="Display the help file.")
	private boolean help = false;

	public String getOutputFile() {
		if(outFile == null) return "out.html";
		return outFile;
	}
	
	public String getOriginal() {
		return original;
	}
	
	public String getModified() {
		return modified;
	}
	
	public boolean defUse() { return defUse; }
	public boolean varDiff() { return varDiff; }
	public boolean valDiff() { return valDiff; }
	public boolean conDiff() { return conDiff; }

	public boolean getHelp() {
		return help;
	}

}