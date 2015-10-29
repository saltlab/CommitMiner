package ca.ubc.ece.salt.pangor.java.test.methodrename;

import java.io.IOException;

import org.junit.Test;

import ca.ubc.ece.salt.pangor.analysis.CommitAnalysis;
import ca.ubc.ece.salt.pangor.analysis.classify.ClassifierAlert;
import ca.ubc.ece.salt.pangor.analysis.classify.ClassifierDataSet;
import ca.ubc.ece.salt.pangor.analysis.simple.SimpleCFGFactory;
import ca.ubc.ece.salt.pangor.batch.GitProjectAnalysis;
import ca.ubc.ece.salt.pangor.java.analysis.ClassAnalysis;
import ca.ubc.ece.salt.pangor.java.analysis.methodrename.RenameMethodDstAnalysis;
import ca.ubc.ece.salt.pangor.java.analysis.methodrename.RenameMethodSrcAnalysis;

public class TestRenameRefactoringBatchAnalysis {

	@Test
	public void test() throws IOException, Exception {

		/* Set up the data set (stores alerts aka feature vectors). */
		String dataset = "/Users/qhanam/Documents/workspace_pangor/pangor/java-classify/test/output/dataset.csv";
		String supplementary = "/Users/qhanam/Documents/workspace_pangor/pangor/java-classify/test/output/supplementary";
		ClassifierDataSet dataSet = new ClassifierDataSet(dataset, supplementary);

		/* We will use a simple file analysis for this test. */
		ClassAnalysis<ClassifierAlert> srcAnalysis = new ClassAnalysis<ClassifierAlert>(
				new RenameMethodSrcAnalysis());
		ClassAnalysis<ClassifierAlert> dstAnalysis = new ClassAnalysis<ClassifierAlert>(
				new RenameMethodDstAnalysis());

		/* The SimpleCFGFactory doesn't do anything, so it is generic to any file type. */
		SimpleCFGFactory cfgFactory = new SimpleCFGFactory();

		/* Set up the commit analysis (analyzes one commit). */
		CommitAnalysis<ClassifierAlert, ClassifierDataSet,
					   ClassAnalysis<ClassifierAlert>,
					   ClassAnalysis<ClassifierAlert>> commitAnalysis;
		commitAnalysis = new CommitAnalysis<ClassifierAlert, ClassifierDataSet,
					   ClassAnalysis<ClassifierAlert>,
					   ClassAnalysis<ClassifierAlert>>(
							   dataSet, srcAnalysis, dstAnalysis, cfgFactory, false);

		/* Set up the project analysis (analyzes one project). */
		GitProjectAnalysis projectAnalysis = GitProjectAnalysis.fromURI(
				"https://github.com/naman14/Timber.git", "./repositories/",
				"^.*$", commitAnalysis);

		/* Run the analysis. */
		projectAnalysis.analyze();

		/* Check that there are at least 232 x 2 = 464 alerts (one for each commit). */
		//Assert.assertTrue(dataSet.getAlerts().size() >= 1322);

		/* Print the results. */
		//Should be in the dataset file.
		//System.out.println(dataSet.printAlerts());

	}

}