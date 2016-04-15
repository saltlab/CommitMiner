package ca.ubc.ece.salt.pangor.js.classify.use.test;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.deri.iris.api.basics.IQuery;
import org.deri.iris.api.basics.IRule;
import org.deri.iris.factory.Factory;
import org.junit.Test;

import ca.ubc.ece.salt.pangor.analysis.Commit;
import ca.ubc.ece.salt.pangor.analysis.Commit.Type;
import ca.ubc.ece.salt.pangor.analysis.CommitAnalysis;
import ca.ubc.ece.salt.pangor.analysis.DomainAnalysis;
import ca.ubc.ece.salt.pangor.analysis.SourceCodeFileChange;
import ca.ubc.ece.salt.pangor.classify.analysis.ClassifierDataSet;
import ca.ubc.ece.salt.pangor.classify.analysis.ClassifierFeatureVector;
import ca.ubc.ece.salt.pangor.classify.analysis.Transformer;
import ca.ubc.ece.salt.pangor.js.classify.use.UseDomainAnalysis;

public class TestUseDomain {

	/**
	 * Tests data mining data set construction.
	 * @param args The command line arguments (i.e., old and new file names).
	 * @throws Exception
	 */
	protected void runTest(SourceCodeFileChange sourceFileChange,
						   boolean printAlerts) throws Exception {

		Commit commit = getCommit();
		commit.addSourceCodeFileChange(sourceFileChange);

		/* Builds the data set with our custom queries. */
		ClassifierDataSet dataSet = new ClassifierDataSet(null,
				new LinkedList<IRule>(), getUseQueries());

		/* Set up the analysis. */
		List<DomainAnalysis> domains = new LinkedList<DomainAnalysis>();
		UseDomainAnalysis analysis = UseDomainAnalysis.createLearningAnalysis();
		domains.add(analysis);

		/* Set up the commit analysis. */
		CommitAnalysis commitAnalysis = new CommitAnalysis(dataSet, domains);

		/* Run the analysis. */
		commitAnalysis.analyze(commit);

        /* Print the data set. */
		dataSet.printDataSet();

        /* Verify the expected feature vectors match the actual feature vectors. */
		// TODO
//        for(MockFeatureVector fv : expected) {
//        	Assert.assertTrue(dataSet.contains(fv.functionName, fv.expectedKeywords));
//        }
	}

	@Test
	public void testFalsey() throws Exception {

		/* The test files. */
		String src = "./test/input/learning/falsey_old.js";
		String dst = "./test/input/learning/falsey_new.js";

		/* Read the source files. */
		SourceCodeFileChange sourceCodeFileChange = getSourceCodeFileChange(src, dst);

		this.runTest(sourceCodeFileChange, true);
	}

	/**
	 * @return A dummy commit for testing.
	 */
	public static Commit getCommit() {
		return new Commit("test", "http://github.com/saltlab/Pangor", "c0", "c1", Type.BUG_FIX);
	}

	/**
	 * @return A dummy source code file change for testing.
	 * @throws IOException
	 */
	public static SourceCodeFileChange getSourceCodeFileChange(String srcFile, String dstFile) throws IOException {
		String buggyCode = readFile(srcFile);
		String repairedCode = readFile(dstFile);
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

	/**
	 * @return The Datalog query that selects identifier uses.
	 */
	public static Map<IQuery,Transformer> getUseQueries() {

		Map<IQuery, Transformer> queries = new HashMap<IQuery, Transformer>();

		IQuery query = Factory.BASIC.createQuery(
				Factory.BASIC.createLiteral(true,
				Factory.BASIC.createPredicate("Use", 6),
				Factory.BASIC.createTuple(
						Factory.TERM.createVariable("Version"),
						Factory.TERM.createVariable("File"),
						Factory.TERM.createVariable("Line"),
						Factory.TERM.createVariable("StatementID"),
						Factory.TERM.createVariable("ChangeType"),
						Factory.TERM.createVariable("Identifier"))));


		Transformer transformer = (commit, tuple) -> {
				return new ClassifierFeatureVector(commit,
						tuple.get(0).toString().replace("\'", ""),			// Version
						tuple.get(1).toString().replace("\'", ""), 			// Class
						"MethodNA",											// Method
						tuple.get(2).toString().replace("\'", ""),			// Line
						"TST",												// Type
						"USE",												// Subtype
						tuple.get(4).toString().replace("\'", "")
							+ "_" + tuple.get(5).toString().replace("\'", ""));	// Description
		};

		queries.put(query, transformer);

		return queries;

	}

}