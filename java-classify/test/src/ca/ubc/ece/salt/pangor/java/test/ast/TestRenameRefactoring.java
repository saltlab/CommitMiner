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
import ca.ubc.ece.salt.pangor.java.analysis.methodrename.RenameMethodAlert;
import ca.ubc.ece.salt.pangor.java.analysis.methodrename.RenameMethodDstAnalysis;
import ca.ubc.ece.salt.pangor.java.analysis.methodrename.RenameMethodSrcAnalysis;
import ca.ubc.ece.salt.pangor.java.analysis.methodrename.UpdatedCallsiteAlert;
import ca.ubc.ece.salt.pangor.java.cfg.JavaCFGFactory;

public class TestRenameRefactoring extends TestAnalysis{


	private void runTest(Commit commit, List<SourceCodeFileChange> sourceFileChanges,
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
		this.runTest(commit, sourceFileChanges, expectedAlerts, printAlerts, commitAnalysis, dataSet);

	}

	@Test
	public void testSimpleRename() throws Exception {

		/* The test files. */
		String srcFile = "/Users/qhanam/Documents/workspace_pangor/pangor/core/test/input/java-source/User.java";
		String dstFile = "/Users/qhanam/Documents/workspace_pangor/pangor/core/test/input/java-destination/User.java";

		/* Set up the dummy data. */
		Commit commit = TestAnalysis.getCommit();
		List<SourceCodeFileChange> sourceFileChanges = new LinkedList<SourceCodeFileChange>();
		SourceCodeFileChange user = TestAnalysis.getSourceCodeFileChange(srcFile, dstFile);
		sourceFileChanges.add(user);

		/* Define the expected results. */
		List<ClassifierAlert> expectedAlerts = new LinkedList<ClassifierAlert>();
		expectedAlerts.add(new RenameMethodAlert(commit, user,
				"~NA~", "REFACTOR", "METHOD_RENAME", "getName", "getUserName"));

		expectedAlerts.add(new UpdatedCallsiteAlert(commit, user,
				"getGreetingMessage", "REFACTOR", "METHOD_RENAME_UPDATE_CALLSITE",
				"getGreetingMessage", 20, 20,
				"getName", "getUserName"));

		expectedAlerts.add(new UpdatedCallsiteAlert(commit, user,
				"getRelativeAgeMessage", "REFACTOR", "METHOD_RENAME_UPDATE_CALLSITE",
				"getRelativeAgeMessage", 27, 27,
				"getName", "getUserName"));

		this.runTest(commit, sourceFileChanges, expectedAlerts, true);

	}

	@Test
	public void testMultiFileRename() throws Exception {

		/* The test files. */
		String srcFileUser = "/Users/qhanam/Documents/workspace_pangor/pangor/java-classify/test/input/java-source/User.java";
		String srcFileHRSystem = "/Users/qhanam/Documents/workspace_pangor/pangor/java-classify/test/input/java-source/HRSystem.java";
		String dstFileUser = "/Users/qhanam/Documents/workspace_pangor/pangor/java-classify/test/input/java-destination/User.java";
		String dstFileHRSystem = "/Users/qhanam/Documents/workspace_pangor/pangor/java-classify/test/input/java-destination/HRSystem.java";

		/* Set up the dummy data. */
		Commit commit = TestAnalysis.getCommit();
		List<SourceCodeFileChange> sourceFileChanges = new LinkedList<SourceCodeFileChange>();
		SourceCodeFileChange user = TestAnalysis.getSourceCodeFileChange(srcFileUser, dstFileUser);
		sourceFileChanges.add(user);
		SourceCodeFileChange hrsystem = TestAnalysis.getSourceCodeFileChange(srcFileHRSystem, dstFileHRSystem);
		sourceFileChanges.add(hrsystem);

		/* Define the expected results. */
		List<ClassifierAlert> expectedAlerts = new LinkedList<ClassifierAlert>();
		expectedAlerts.add(new RenameMethodAlert(commit, user,
				"~NA~", "REFACTOR", "METHOD_RENAME", "getName", "getUserName"));

		expectedAlerts.add(new UpdatedCallsiteAlert(commit, user,
				"getGreetingMessage", "REFACTOR", "METHOD_RENAME_UPDATE_CALLSITE",
				"getGreetingMessage", 20, 20,
				"getName", "getUserName"));

		expectedAlerts.add(new UpdatedCallsiteAlert(commit, user,
				"getRelativeAgeMessage", "REFACTOR", "METHOD_RENAME_UPDATE_CALLSITE",
				"getRelativeAgeMessage", 27, 27,
				"getName", "getUserName"));

		expectedAlerts.add(new RenameMethodAlert(commit, user,
				"~NA~", "REFACTOR", "METHOD_RENAME", "birthday", "getBirthday"));

		expectedAlerts.add(new UpdatedCallsiteAlert(commit, hrsystem,
				"main", "REFACTOR", "METHOD_RENAME_UPDATE_CALLSITE",
				"main", 28, 28,
				"birthday", "getBirthday"));

		this.runTest(commit, sourceFileChanges, expectedAlerts, true);

	}

	@Test
	public void testMultiFileFalsePositive() throws Exception {

		/* The test files. */
		String srcFileUser = "/Users/qhanam/Documents/workspace_pangor/pangor/java-classify/test/input/java-source/User.java";
		String srcFileHRSystem = "/Users/qhanam/Documents/workspace_pangor/pangor/java-classify/test/input/java-source/HRSystem.java";
		String dstFileUser = "/Users/qhanam/Documents/workspace_pangor/pangor/java-classify/test/input/java-destination/UserFP.java";
		String dstFileHRSystem = "/Users/qhanam/Documents/workspace_pangor/pangor/java-classify/test/input/java-destination/HRSystemFP.java";

		/* Set up the dummy data. */
		Commit commit = TestAnalysis.getCommit();
		List<SourceCodeFileChange> sourceFileChanges = new LinkedList<SourceCodeFileChange>();
		SourceCodeFileChange user = TestAnalysis.getSourceCodeFileChange(srcFileUser, dstFileUser);
		sourceFileChanges.add(user);
		SourceCodeFileChange hrsystem = TestAnalysis.getSourceCodeFileChange(srcFileHRSystem, dstFileHRSystem);
		sourceFileChanges.add(hrsystem);

		/* Define the expected results. */
		List<ClassifierAlert> expectedAlerts = new LinkedList<ClassifierAlert>();
		expectedAlerts.add(new RenameMethodAlert(commit, user,
				"~NA~", "REFACTOR", "METHOD_RENAME", "getName", "getUserName"));

		expectedAlerts.add(new UpdatedCallsiteAlert(commit, user,
				"getGreetingMessage", "REFACTOR", "METHOD_RENAME_UPDATE_CALLSITE",
				"getGreetingMessage", 20, 20,
				"getName", "getUserName"));

		expectedAlerts.add(new UpdatedCallsiteAlert(commit, user,
				"getRelativeAgeMessage", "REFACTOR", "METHOD_RENAME_UPDATE_CALLSITE",
				"getRelativeAgeMessage", 27, 27,
				"getName", "getUserName"));

		this.runTest(commit, sourceFileChanges, expectedAlerts, true);

	}

}