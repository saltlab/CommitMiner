package multidiff;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;

import commitminer.analysis.Commit;
import commitminer.analysis.CommitAnalysis;
import commitminer.analysis.SourceCodeFileChange;
import commitminer.analysis.Commit.Type;
import commitminer.analysis.annotation.AnnotationFactBase;
import commitminer.analysis.factories.ICommitAnalysisFactory;
import commitminer.analysis.options.Options;
import commitminer.batch.GitProjectAnalysis;
import commitminer.js.diff.DiffCommitAnalysisFactory;
import commitminer.js.diff.factories.CommitAnalysisFactoryAnnotationMetrics;
import commitminer.js.metrics.AnnotationMetricsPostprocessor;

public class MultiDiffBatch {

	/** The directory where repositories are checked out. **/
	public static final String CHECKOUT_DIR =  new String("repositories");
	
	public static void main(String[] args) {

		/* Get the options from the command line args. */
		MultiDiffOptions options = new MultiDiffOptions();
		CmdLineParser parser = new CmdLineParser(options);

		try {
			parser.parseArgument(args);
		} catch (CmdLineException e) {
			MultiDiffBatch.printUsage(e.getMessage(), parser);
			return;
		}

		/* Print the help page. */
		if(options.getHelp()) {
			MultiDiffBatch.printHelp(parser);
			return;
		}

		/* The analysis we will be using. */
		ICommitAnalysisFactory analysisFactory = new CommitAnalysisFactoryAnnotationMetrics();
		
		GitProjectAnalysis gitProjectAnalysis;
		try {

			/* Checkout or pull the project. */
            gitProjectAnalysis = GitProjectAnalysis.fromURI(options.getURI(),
            		CHECKOUT_DIR, ".*", analysisFactory);
            
            /* Run the analysis on the project history. */
			gitProjectAnalysis.analyze();

		} catch (Exception e) {
			e.printStackTrace(System.err);
			return;
		}	

	}
	
	/** The analysis options. **/
	private MultiDiffOptions options;
	
	public MultiDiffBatch(MultiDiffOptions options) {
		this.options = options;
	}

	/**
	 * Generate the diff in a partial html file.
	 */
	protected void diff(SourceCodeFileChange sourceFileChange) throws Exception {

		/* Set the options for this run. */
		Options.createInstance(Options.DiffMethod.GUMTREE, Options.ChangeImpact.DEPENDENCIES);

		/* Build the dummy commit. */
		Commit commit = getCommit();
		commit.addSourceCodeFileChange(sourceFileChange);

		/* Builds the data set with our custom queries. */
		AnnotationFactBase factBase = AnnotationFactBase.getInstance(sourceFileChange);

		/* Set up the analysis. */
		ICommitAnalysisFactory commitFactory = new DiffCommitAnalysisFactory();
		CommitAnalysis commitAnalysis = commitFactory.newInstance();

		/* Run the analysis. */
		commitAnalysis.analyze(commit);

        /* Print the data set. */
		factBase.printDataSet();

		/* Write metrics to a file. */
		AnnotationMetricsPostprocessor postProc = new AnnotationMetricsPostprocessor(options.getOutputFile());
		postProc.writeHeader();
		postProc.process(commit, sourceFileChange, factBase);

	}

	/**
	 * Prints the help file for main.
	 * @param parser The args4j parser.
	 */
	private static void printHelp(CmdLineParser parser) {
        System.out.print("Usage: MultiDiffBatch ");
        parser.printSingleLineUsage(System.out);
        System.out.println("\n");
        parser.printUsage(System.out);
        System.out.println("");
	}

	/**
	 * Prints the usage of main.
	 * @param error The error message that triggered the usage message.
	 * @param parser The args4j parser.
	 */
	private static void printUsage(String error, CmdLineParser parser) {
        System.out.println(error);
        System.out.print("Usage: MultiDiff ");
        parser.printSingleLineUsage(System.out);
        System.out.println("");
	}


	/**
	 * @return a dummy commit. 
	 */
	public static Commit getCommit() {
		return new Commit("test", "http://github.com/saltlab/Pangor", "c0", "c1", Type.BUG_FIX);
	}

	/**
	 * @return A dummy source code file change for testing.
	 * @throws IOException
	 */
	public static SourceCodeFileChange getSourceCodeFileChange(String srcFile, String dstFile) throws IOException {
		String buggyCode = readFile(srcFile);
		String repairedCode = readFile(dstFile);
		return new SourceCodeFileChange(srcFile, dstFile, buggyCode, repairedCode);
	}

	/**
	 * Reads the contents of a source code file into a string.
	 * @param path The path to the source code file.
	 * @return A string containing the source code.
	 * @throws IOException
	 */
	private static String readFile(String path) throws IOException {
		return FileUtils.readFileToString(new File(path));
	}

}