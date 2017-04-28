package commitminer.test.analysis;

import java.io.File;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.io.FileUtils;
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

import commitminer.analysis.Commit;
import commitminer.analysis.CommitAnalysis;
import commitminer.analysis.SourceCodeFileChange;
import commitminer.analysis.Commit.Type;
import commitminer.analysis.factories.ICommitAnalysisFactory;
import commitminer.analysis.simple.SimpleCommitAnalysisFactory;
import commitminer.analysis.simple.SimpleDataSet;
import commitminer.analysis.simple.SimpleFeatureVector;

/**
 * Tests {@code CommitAnalysis} and {@code SourceCodeFileAnalysis} with
 * {@code SimpleSourceCodeFileAnalysis}.
 */
public class TestSimpleAnalysis {

	@Test
	public void test() throws Exception {

		/* Set up a dummy commit. */
		Commit commit = new Commit("test", "http://github.com/saltlab/Pangor", "c0", "c1", Type.BUG_FIX);

		/* Add a source code file change. */
		String buggyFile = "/Users/qhanam/Documents/workspace_commitminer/pangor/core/test/input/java-source/User.java";
		String repairedFile = "/Users/qhanam/Documents/workspace_commitminer/pangor/core/test/input/java-destination/User.java";
		String buggyCode = readFile(buggyFile);
		String repairedCode = readFile(repairedFile);

		SourceCodeFileChange sourceFileChange = new SourceCodeFileChange(buggyFile, repairedFile, buggyCode, repairedCode);

		commit.addSourceCodeFileChange(sourceFileChange);

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

		/* Set up the commit analysis. */
		ICommitAnalysisFactory analysisFactory = new SimpleCommitAnalysisFactory(dataSet);
		CommitAnalysis commitAnalysis = analysisFactory.newInstance();

		/* Run the analysis. */
		commitAnalysis.analyze(commit);

		/* We should have one alert in the data set now. */
		System.out.println(dataSet.printAlerts());
		Assert.assertTrue(dataSet.contains(new SimpleFeatureVector(commit, "[?- SourceRoot(?X, ?Y).](('User', 'CompilationUnit'))")));
		Assert.assertTrue(dataSet.contains(new SimpleFeatureVector(commit, "[?- DestinationRoot(?X, ?Y).](('User', 'CompilationUnit'))")));

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
