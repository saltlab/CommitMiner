package ca.ubc.ece.salt.pangor.java.test.ast;


import java.io.File;
import java.io.IOException;
import java.util.List;

import junit.framework.TestCase;

import org.apache.commons.io.FileUtils;
import org.junit.Ignore;

import ca.ubc.ece.salt.pangor.analysis.Commit;
import ca.ubc.ece.salt.pangor.analysis.CommitAnalysis;
import ca.ubc.ece.salt.pangor.analysis.SourceCodeFileChange;
import ca.ubc.ece.salt.pangor.analysis.classifier.ClassifierAlert;
import ca.ubc.ece.salt.pangor.analysis.classifier.ClassifierDataSet;
import ca.ubc.ece.salt.pangor.java.analysis.ClassAnalysis;

@Ignore
public class TestAnalysis extends TestCase {

	/**
	 * Tests flow analysis repair classifiers.
	 * @param args The command line arguments (i.e., old and new file names).
	 * @param expectedAlerts The list of alerts that should be produced.
	 * @param printAlerts If true, print the alerts to standard output.
	 * @param dataSet The data set that stores the alerts. Needed to assert the
	 * 				  tests pass/fail.
	 * @throws Exception
	 */
	protected void runTest(String[] args, List<ClassifierAlert> expectedAlerts,
			boolean printAlerts, CommitAnalysis<ClassifierAlert, ClassifierDataSet,
												ClassAnalysis<ClassifierAlert>,
												ClassAnalysis<ClassifierAlert>> commitAnalysis,
			ClassifierDataSet dataSet) throws Exception {

		/* Set up a dummy commit. */
		Commit commit = new Commit(1, 1, "test", "http://github.com/saltlab/Pangor", "c0", "c1");

		/* Set up a source code file change. */

		String buggyFile = "/Users/qhanam/Documents/workspace_pangor/pangor/core/test/input/java-source/User.java";
		String repairedFile = "/Users/qhanam/Documents/workspace_pangor/pangor/core/test/input/java-destination/User.java";
		String buggyCode = readFile(buggyFile);
		String repairedCode = readFile(repairedFile);

		SourceCodeFileChange sourceFileChange = new SourceCodeFileChange(buggyFile, repairedFile, buggyCode, repairedCode);

		/* Add the source code file change to the commit. */
		commit.addSourceCodeFileChange(sourceFileChange);

		/* Run the analysis. */
		commitAnalysis.analyze(commit);

		/* Check the results. */

        List<ClassifierAlert> actualAlerts = dataSet.getAlerts();

        /* Output if needed. */
        if(printAlerts) {
        	for(ClassifierAlert alert : actualAlerts) {
        		System.out.println(alert.getLongDescription());
        	}
        }

		/* Check the output. */
        this.check(actualAlerts, expectedAlerts);

	}

	protected void check(List<ClassifierAlert> actualAlerts, List<ClassifierAlert> expectedAlerts) {
		/* Check that all the expected alerts are produced by SDJSB. */
		for(ClassifierAlert expected : expectedAlerts) {
			assertTrue("SDJSB did not produce the alert \"" + expected.getLongDescription() + "\"", actualAlerts.contains(expected));
		}

		/* Check that only the expected alerts are produced by SDJSB. */
		for(ClassifierAlert actual : actualAlerts) {
			assertTrue("SDJSB produced the unexpected alert \"" + actual.getLongDescription() + "\"", expectedAlerts.contains(actual));
		}
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