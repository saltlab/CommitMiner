package ca.ubc.ece.salt.pangor.analysis;

import java.io.IOException;

import org.junit.Assert;
import org.junit.Test;

import ca.ubc.ece.salt.pangor.analysis.simple.SimpleAlert;
import ca.ubc.ece.salt.pangor.analysis.simple.SimpleCFGFactory;
import ca.ubc.ece.salt.pangor.analysis.simple.SimpleDataSet;
import ca.ubc.ece.salt.pangor.analysis.simple.SimpleDstFileAnalysis;
import ca.ubc.ece.salt.pangor.analysis.simple.SimpleSrcFileAnalysis;
import ca.ubc.ece.salt.pangor.batch.GitProjectAnalysis;

public class TestBatchAnalysis {

	@Test
	public void test() throws IOException, Exception {

		/* Set up the data set (stores alerts aka feature vectors). */
		SimpleDataSet<SimpleAlert> dataSet = new SimpleDataSet<SimpleAlert>();

		/* We will use a simple file analysis for this test. */
		SimpleSrcFileAnalysis srcAnalysis = new SimpleSrcFileAnalysis();
		SimpleDstFileAnalysis dstAnalysis = new SimpleDstFileAnalysis();

		/* The SimpleCFGFactory doesn't do anything, so it is generic to any file type. */
		SimpleCFGFactory cfgFactory = new SimpleCFGFactory();

		/* Set up the commit analysis (analyzes one commit). */
		CommitAnalysis<SimpleAlert, SimpleDataSet<SimpleAlert>,
					   SimpleSrcFileAnalysis, SimpleDstFileAnalysis> commitAnalysis;
		commitAnalysis = new CommitAnalysis<SimpleAlert, SimpleDataSet<SimpleAlert>,
					   SimpleSrcFileAnalysis, SimpleDstFileAnalysis>(
							   dataSet, srcAnalysis, dstAnalysis, cfgFactory, false);

		/* Set up the project analysis (analyzes one project). */
		GitProjectAnalysis projectAnalysis = GitProjectAnalysis.fromURI(
				"https://github.com/naman14/Timber.git", "./repositories/",
				"^.*$", commitAnalysis);

		/* Run the analysis. */
		projectAnalysis.analyze();

		/* Check that there are at least 232 x 2 = 464 alerts (one for each commit). */
		Assert.assertTrue(dataSet.getAlerts().size() >= 1322);

		/* Print the results. */
		System.out.println(dataSet.printAlerts());

	}

}