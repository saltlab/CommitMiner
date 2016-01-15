package ca.ubc.ece.salt.pangor.test.analysis;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import org.deri.iris.api.basics.ILiteral;
import org.deri.iris.api.basics.IPredicate;
import org.deri.iris.api.basics.IQuery;
import org.deri.iris.api.basics.IRule;
import org.deri.iris.api.basics.ITuple;
import org.deri.iris.api.factory.IBasicFactory;
import org.deri.iris.api.factory.ITermFactory;
import org.deri.iris.api.terms.ITerm;
import org.deri.iris.factory.Factory;
import org.junit.Assert;
import org.junit.Test;

import ca.ubc.ece.salt.pangor.analysis.CommitAnalysis;
import ca.ubc.ece.salt.pangor.analysis.DomainAnalysis;
import ca.ubc.ece.salt.pangor.analysis.simple.SimpleCFGFactory;
import ca.ubc.ece.salt.pangor.analysis.simple.SimpleDataSet;
import ca.ubc.ece.salt.pangor.analysis.simple.SimpleDstFileAnalysis;
import ca.ubc.ece.salt.pangor.analysis.simple.SimpleSrcFileAnalysis;
import ca.ubc.ece.salt.pangor.batch.GitProjectAnalysis;

public class TestBatchAnalysis {

	@Test
	public void test() throws IOException, Exception {

		/* Create the Datalog queries. */
		IBasicFactory basicFactory = Factory.BASIC;
		ITermFactory termFactory = Factory.TERM;

		ITerm termX = termFactory.createVariable("X"); // X
		ITerm termY = termFactory.createVariable("Y"); // Y
		ITuple tuple = basicFactory.createTuple(termX, termY); // X,Y

		IPredicate sourcePredicate = basicFactory.createPredicate("SourceRoot", 2); // SourceRoot(,)
		ILiteral source = basicFactory.createLiteral(true, sourcePredicate, tuple); // SourceRoot(X,Y)
		IQuery sourceQuery = basicFactory.createQuery(source); // ?- SourceRoot(X,Y).

		IPredicate destinationPredicate = basicFactory.createPredicate("DestinationRoot", 2); // DestinationRoot(,)
		ILiteral destination = basicFactory.createLiteral(true, destinationPredicate, tuple); // DestinationRoot(X,Y)
		IQuery destinationQuery = basicFactory.createQuery(destination); // ?- DestinationRoot(X,Y).

		List<IRule> rules = new LinkedList<IRule>();
		List<IQuery> queries = new LinkedList<IQuery>();
		queries.add(sourceQuery);
		queries.add(destinationQuery);

		/* Set up the data set (stores alerts aka feature vectors). */
		SimpleDataSet dataSet = new SimpleDataSet(rules, queries);

		/* We will use a simple file analysis for this test. */
		SimpleSrcFileAnalysis srcAnalysis = new SimpleSrcFileAnalysis();
		SimpleDstFileAnalysis dstAnalysis = new SimpleDstFileAnalysis();

		/* The SimpleCFGFactory doesn't do anything, so it is generic to any file type. */
		SimpleCFGFactory cfgFactory = new SimpleCFGFactory();

		/* Set up the domain analysis. */
		DomainAnalysis domainAnalysis;
		domainAnalysis = new DomainAnalysis(srcAnalysis, dstAnalysis,
											cfgFactory, false);
		List<DomainAnalysis> domainAnalyses = new LinkedList<DomainAnalysis>();
		domainAnalyses.add(domainAnalysis);

		/* Set up the commit analysis. */
		CommitAnalysis commitAnalysis;
		commitAnalysis = new CommitAnalysis(dataSet, domainAnalyses);

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