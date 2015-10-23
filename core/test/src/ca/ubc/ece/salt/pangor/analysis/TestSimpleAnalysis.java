package ca.ubc.ece.salt.pangor.analysis;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.junit.Assert;
import org.junit.Test;

import ca.ubc.ece.salt.pangor.analysis.simple.SimpleAlert;
import ca.ubc.ece.salt.pangor.analysis.simple.SimpleCFGFactory;
import ca.ubc.ece.salt.pangor.analysis.simple.SimpleDataSet;
import ca.ubc.ece.salt.pangor.analysis.simple.SimpleSourceCodeFileAnalysis;
import ca.ubc.ece.salt.pangor.batch.SourceCodeFileChange;

/**
 * Tests {@code CommitAnalysis} and {@code SourceCodeFileAnalysis} with
 * {@code SimpleSourceCodeFileAnalysis}.
 */
public class TestSimpleAnalysis {

	@Test
	public void test() throws Exception {

		/* Set up a dummy commit. */
		Commit commit = new Commit(1, 1, "test", "http://github.com/saltlab/Pangor", "c0", "c1");

		/* Add a source code file change. */
		String buggyFile = "/Users/qhanam/Documents/workspace_pangor/pangor/core/test/input/java-source/User.java";
		String repairedFile = "/Users/qhanam/Documents/workspace_pangor/pangor/core/test/input/java-destination/User.java";
		String buggyCode = readFile(buggyFile);
		String repairedCode = readFile(repairedFile);

		SourceCodeFileChange sourceFileChange = new SourceCodeFileChange(buggyFile, repairedFile, buggyCode, repairedCode);

		commit.addSourceCodeFileChange(sourceFileChange);

		/* Set up the data set (stores alerts aka feature vectors). */
		SimpleDataSet<SimpleAlert> dataSet = new SimpleDataSet<SimpleAlert>();

		/* We will use a simple file analysis for this test. */
		SimpleSourceCodeFileAnalysis srcAnalysis = new SimpleSourceCodeFileAnalysis();
		SimpleSourceCodeFileAnalysis dstAnalysis = new SimpleSourceCodeFileAnalysis();

		/* The SimpleCFGFactory doesn't do anything, so it is generic to any file type. */
		SimpleCFGFactory cfgFactory = new SimpleCFGFactory();

		/* Set up the commit analysis. */
		CommitAnalysis<SimpleAlert, SimpleDataSet<SimpleAlert>,
					   SimpleSourceCodeFileAnalysis, SimpleSourceCodeFileAnalysis> commitAnalysis;
		commitAnalysis = new CommitAnalysis<SimpleAlert, SimpleDataSet<SimpleAlert>,
					   SimpleSourceCodeFileAnalysis, SimpleSourceCodeFileAnalysis>(
							   dataSet, commit, srcAnalysis, dstAnalysis, cfgFactory, false);

		commitAnalysis.analyze(commit);

		/* We should have one alert in the data set now. */
		Assert.assertTrue(dataSet.contains(new SimpleAlert(commit, "CompilationUnit")));

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
