package ca.ubc.ece.salt.pangor.java.test.ast;

import java.util.LinkedList;
import java.util.List;

import org.junit.Test;

import ca.ubc.ece.salt.pangor.analysis.Commit;
import ca.ubc.ece.salt.pangor.analysis.CommitAnalysis;
import ca.ubc.ece.salt.pangor.analysis.SourceCodeFileChange;
import ca.ubc.ece.salt.pangor.analysis.simple.SimpleAlert;
import ca.ubc.ece.salt.pangor.analysis.simple.SimpleDataSet;
import ca.ubc.ece.salt.pangor.cfg.CFGFactory;
import ca.ubc.ece.salt.pangor.java.analysis.ClassAnalysis;
import ca.ubc.ece.salt.pangor.java.analysis.methodrename.RenameMethodDstAnalysis;
import ca.ubc.ece.salt.pangor.java.analysis.methodrename.RenameMethodSrcAnalysis;
import ca.ubc.ece.salt.pangor.java.cfg.JavaCFGFactory;

public class TestRenameRefactoring extends TestAnalysis{


	private void runTest(Commit commit, SourceCodeFileChange sourceFileChange,
			List<SimpleAlert> expectedAlerts,
			boolean printAlerts) throws Exception {

		/* Build the CFG with the JDT Java CFG factory. */
		CFGFactory cfgFactory = new JavaCFGFactory();

		SimpleDataSet dataSet = new SimpleDataSet();

		/* Registers a pre-condition for each method. */
		ClassAnalysis<SimpleAlert> srcAnalysis
			= new ClassAnalysis<SimpleAlert>(new RenameMethodSrcAnalysis());

		/* Registers a pattern for each method. */
		ClassAnalysis<SimpleAlert> dstAnalysis
			= new ClassAnalysis<SimpleAlert>(new RenameMethodDstAnalysis());

		/* Produces an alert for each method that was not inserted or removed. */
		CommitAnalysis<SimpleAlert,
					   SimpleDataSet,
					   ClassAnalysis<SimpleAlert>,
					   ClassAnalysis<SimpleAlert>> commitAnalysis
			= new CommitAnalysis<SimpleAlert,
								 SimpleDataSet,
								 ClassAnalysis<SimpleAlert>,
								 ClassAnalysis<SimpleAlert>>(dataSet,
										 					 srcAnalysis,
										 					 dstAnalysis,
										 					 cfgFactory,
										 					 printAlerts);

		/* Run the analysis. */
		this.runTest(commit, sourceFileChange, expectedAlerts, printAlerts, commitAnalysis, dataSet);

	}

	@Test
	public void testSimpleRename() throws Exception {

		/* The test files. */
		String srcFile = "/Users/qhanam/Documents/workspace_pangor/pangor/core/test/input/java-source/User.java";
		String dstFile = "/Users/qhanam/Documents/workspace_pangor/pangor/core/test/input/java-destination/User.java";

		/* Set up the dummy data. */
		Commit commit = TestAnalysis.getCommit();
		SourceCodeFileChange sourceFileChange = TestAnalysis.getSourceCodeFileChange(srcFile, dstFile);

		/* Define the expected results. */
		List<SimpleAlert> expectedAlerts = new LinkedList<SimpleAlert>();
		expectedAlerts.add(new SimpleAlert(commit, sourceFileChange, "getName -> getUserName"));

		this.runTest(commit, sourceFileChange, expectedAlerts, true);

	}

}
