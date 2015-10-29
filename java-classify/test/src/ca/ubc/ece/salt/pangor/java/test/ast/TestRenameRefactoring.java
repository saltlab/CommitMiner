package ca.ubc.ece.salt.pangor.java.test.ast;

import java.util.LinkedList;
import java.util.List;

import org.junit.Test;

import ca.ubc.ece.salt.pangor.analysis.Commit;
import ca.ubc.ece.salt.pangor.analysis.CommitAnalysis;
import ca.ubc.ece.salt.pangor.analysis.SourceCodeFileChange;
import ca.ubc.ece.salt.pangor.analysis.classify.ClassifierAlert;
import ca.ubc.ece.salt.pangor.analysis.classify.ClassifierDataSet;
import ca.ubc.ece.salt.pangor.cfg.CFGFactory;
import ca.ubc.ece.salt.pangor.java.analysis.ClassAnalysis;
import ca.ubc.ece.salt.pangor.java.analysis.methodrename.RenameMethodDstAnalysis;
import ca.ubc.ece.salt.pangor.java.analysis.methodrename.RenameMethodSrcAnalysis;
import ca.ubc.ece.salt.pangor.java.cfg.JavaCFGFactory;
import ca.ubc.ece.salt.pangor.java.classify.alert.RenameMethodAlert;
import ca.ubc.ece.salt.pangor.java.classify.alert.UpdatedCallsiteAlert;

public class TestRenameRefactoring extends TestAnalysis{


	private void runTest(Commit commit, SourceCodeFileChange sourceFileChange,
			List<ClassifierAlert> expectedAlerts,
			boolean printAlerts) throws Exception {

		/* Build the CFG with the JDT Java CFG factory. */
		CFGFactory cfgFactory = new JavaCFGFactory();

		ClassifierDataSet dataSet = new ClassifierDataSet(null, null);

		/* The source analysis for rename refactoring. */
		ClassAnalysis<ClassifierAlert> srcAnalysis
			= new ClassAnalysis<ClassifierAlert>(new RenameMethodSrcAnalysis());

		/* The destination analysis for rename refactoring. */
		ClassAnalysis<ClassifierAlert> dstAnalysis
			= new ClassAnalysis<ClassifierAlert>(new RenameMethodDstAnalysis());

		/* Produces an alert for each method that was not inserted or removed. */
		CommitAnalysis<ClassifierAlert,
					   ClassifierDataSet,
					   ClassAnalysis<ClassifierAlert>,
					   ClassAnalysis<ClassifierAlert>> commitAnalysis
			= new CommitAnalysis<ClassifierAlert,
								 ClassifierDataSet,
								 ClassAnalysis<ClassifierAlert>,
								 ClassAnalysis<ClassifierAlert>>(dataSet,
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
		List<ClassifierAlert> expectedAlerts = new LinkedList<ClassifierAlert>();
		expectedAlerts.add(new RenameMethodAlert(commit, sourceFileChange,
				"~NA~", "REFACTOR", "METHOD_RENAME", "getName", "getUserName"));

		expectedAlerts.add(new UpdatedCallsiteAlert(commit, sourceFileChange,
				"getGreetingMessage", "REFACTOR", "METHOD_RENAME_UPDATE_CALLSITE",
				"getGreetingMessage", 20, 20,
				"getName", "getUserName"));

		expectedAlerts.add(new UpdatedCallsiteAlert(commit, sourceFileChange,
				"getRelativeAgeMessage", "REFACTOR", "METHOD_RENAME_UPDATE_CALLSITE",
				"getRelativeAgeMessage", 27, 27,
				"getName", "getUserName"));

		this.runTest(commit, sourceFileChange, expectedAlerts, true);

	}

}
