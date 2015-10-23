package ca.ubc.ece.salt.pangor.java.test.ast;


import java.util.List;

import junit.framework.TestCase;

import org.junit.Ignore;

import ca.ubc.ece.salt.pangor.analysis.Analysis;
//import ca.ubc.ece.salt.pangor.analysis.classify.ClassifierDataSet;
import ca.ubc.ece.salt.pangor.cfd.ControlFlowDifferencing;
import ca.ubc.ece.salt.pangor.classify.ClassifierDataSet;
import ca.ubc.ece.salt.pangor.classify.alert.ClassifierAlert;
import ca.ubc.ece.salt.pangor.java.cfg.JavaCFGFactory;

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
	protected void runTest(String[] args, List<ClassifierAlert> expectedAlerts, boolean printAlerts, Analysis<ClassifierAlert, ClassifierDataSet> analysis, ClassifierDataSet dataSet) throws Exception {

		/* Control flow difference the files. */
		ControlFlowDifferencing cfd = new ControlFlowDifferencing(new JavaCFGFactory(), args);

		/* Run the analysis. */
        cfd.analyze(analysis);

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

}