package multidiff;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

import org.apache.commons.io.FileUtils;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;

import commitminer.analysis.Commit;
import commitminer.analysis.CommitAnalysis;
import commitminer.analysis.SourceCodeFileChange;
import commitminer.analysis.Commit.Type;
import commitminer.analysis.annotation.AnnotationDataSet;
import commitminer.analysis.annotation.AnnotationFactBase;
import commitminer.analysis.factories.ICommitAnalysisFactory;
import commitminer.js.diff.DiffCommitAnalysisFactory;
import commitminer.js.diff.view.HTMLMultiDiffViewer;
import commitminer.js.diff.view.HTMLUnixDiffViewer;

public class MultiDiff {

	public static void main(String[] args) {

		/* The test files. */
		MultiDiffOptions options = new MultiDiffOptions();
		CmdLineParser parser = new CmdLineParser(options);

		try {
			parser.parseArgument(args);
		} catch (CmdLineException e) {
			MultiDiff.printUsage(e.getMessage(), parser);
			return;
		}

		/* Print the help page. */
		if(options.getHelp()) {
			MultiDiff.printHelp(parser);
			return;
		}

		/* Read the source files. */
		SourceCodeFileChange sourceCodeFileChange = null;
		
		try { 
			sourceCodeFileChange = getSourceCodeFileChange(options.getOriginal(), options.getModified());
		} catch(IOException e) {
			System.err.println("An IOException occurred while reading the source code files. Check that the paths are correct.");
			return;
		}

		/* Build the expected feature vectors. */
		try {
			MultiDiff multiDiff = new MultiDiff(options);
			multiDiff.diff(sourceCodeFileChange);
		} catch (Exception e) {
			System.err.println("An Exception occurred while generating the diff... aborting.");
		}

	}
	
	/** The analysis options. **/
	private MultiDiffOptions options;
	
	public MultiDiff(MultiDiffOptions options) {
		this.options = options;
	}

	/**
	 * Generate the diff in a partial html file.
	 */
	protected void diff(SourceCodeFileChange sourceFileChange) throws Exception {

		/* Read the source files. */
		String srcCode = new String(Files.readAllBytes(Paths.get(options.getOriginal())));
		String dstCode = new String(Files.readAllBytes(Paths.get(options.getModified())));

		/* Set up a 'fake' commit since this is not a mining task. */
		Commit commit = getCommit();
		commit.addSourceCodeFileChange(sourceFileChange);

		/* Builds the data set with our custom queries. */
		AnnotationDataSet dataSet = new AnnotationDataSet( AnnotationFactBase.getInstance(sourceFileChange));

		/* Set up the analysis. */
		ICommitAnalysisFactory commitFactory = new DiffCommitAnalysisFactory(dataSet);
		CommitAnalysis commitAnalysis = commitFactory.newInstance();

		/* Run the analysis. */
		commitAnalysis.analyze(commit);

        /* Print the data set. */
		dataSet.printDataSet();

		/* Only annotate the destination file. The source file isn't especially useful. */
		String annotatedDst = HTMLMultiDiffViewer.annotate(dstCode, dataSet.getAnnotationFactBase());

		/* Combine the annotated file with the UnixDiff. */
		String annotatedCombined = HTMLUnixDiffViewer.annotate(srcCode, dstCode, annotatedDst);
		Files.write(Paths.get(options.getOutputFile()), annotatedCombined.getBytes(), StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);

	}

	/**
	 * Prints the help file for main.
	 * @param parser The args4j parser.
	 */
	private static void printHelp(CmdLineParser parser) {
        System.out.print("Usage: MultiDiff ");
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