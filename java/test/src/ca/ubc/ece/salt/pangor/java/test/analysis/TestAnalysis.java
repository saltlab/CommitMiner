package ca.ubc.ece.salt.pangor.java.test.analysis;


import java.io.File;
import java.io.IOException;
import java.util.List;

import junit.framework.TestCase;

import org.apache.commons.io.FileUtils;
import org.junit.Ignore;

import ca.ubc.ece.salt.pangor.analysis.Commit;
import ca.ubc.ece.salt.pangor.analysis.CommitAnalysis;
import ca.ubc.ece.salt.pangor.analysis.SourceCodeFileChange;
import ca.ubc.ece.salt.pangor.analysis.simple.SimpleDataSet;
import ca.ubc.ece.salt.pangor.analysis.simple.SimpleFeatureVector;

@Ignore
public class TestAnalysis extends TestCase {

	/**
	 * Tests flow analysis repair classifiers.
	 * @param srcFile The path to the source file.
	 * @param dstFile The path to the destination file.
	 * @param expectedAlerts The list of alerts that should be produced.
	 * @param printAlerts If true, print the alerts to standard output.
	 * @param dataSet The data set that stores the alerts. Needed to assert the
	 * 				  tests pass/fail.
	 * @throws Exception
	 */
	protected void runTest(Commit commit, SourceCodeFileChange sourceFileChange,
			List<SimpleFeatureVector> expectedAlerts,
			boolean printAlerts,
			CommitAnalysis commitAnalysis,
			SimpleDataSet dataSet) throws Exception {

		/* Add the source code file change to the commit. */
		commit.addSourceCodeFileChange(sourceFileChange);

		/* Run the analysis. */
		commitAnalysis.analyze(commit);

		/* Check the results. */

        List<SimpleFeatureVector> actualAlerts = dataSet.getAlerts();

        /* Output if needed. */
        if(printAlerts) {
        	for(SimpleFeatureVector alert : actualAlerts) {
        		System.out.println(alert.toString());
        	}
        }

		/* Check the output. */
        this.check(actualAlerts, expectedAlerts);

	}

	protected void check(List<SimpleFeatureVector> actualAlerts, List<SimpleFeatureVector> expectedAlerts) {
		/* Check that all the expected alerts are produced by SDJSB. */
		for(SimpleFeatureVector expected : expectedAlerts) {
			assertTrue("SDJSB did not produce the alert \"" + expected.toString() + "\"", actualAlerts.contains(expected));
		}

		/* Check that only the expected alerts are produced by SDJSB. */
		for(SimpleFeatureVector actual : actualAlerts) {
			assertTrue("SDJSB produced the unexpected alert \"" + actual.toString() + "\"", expectedAlerts.contains(actual));
		}
	}

	/**
	 * @return A dummy commit for testing.
	 */
	public static Commit getCommit() {
		return new Commit("test", "http://github.com/saltlab/Pangor", "c0", "c1", true);
	}

	/**
	 * @return A dummy source code file change for testing.
	 * @throws IOException
	 */
	public static SourceCodeFileChange getSourceCodeFileChange(String srcFile, String dstFile) throws IOException {
		String buggyCode = TestAnalysis.readFile(srcFile);
		String repairedCode = TestAnalysis.readFile(dstFile);
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