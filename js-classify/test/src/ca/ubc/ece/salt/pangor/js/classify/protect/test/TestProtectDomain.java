package ca.ubc.ece.salt.pangor.js.classify.protect.test;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.deri.iris.api.basics.IQuery;
import org.deri.iris.api.basics.IRule;
import org.deri.iris.compiler.Parser;
import org.deri.iris.compiler.ParserException;
import org.junit.Assert;
import org.junit.Test;

import ca.ubc.ece.salt.pangor.analysis.Commit;
import ca.ubc.ece.salt.pangor.analysis.Commit.Type;
import ca.ubc.ece.salt.pangor.analysis.CommitAnalysis;
import ca.ubc.ece.salt.pangor.analysis.SourceCodeFileChange;
import ca.ubc.ece.salt.pangor.analysis.factories.ICommitAnalysisFactory;
import ca.ubc.ece.salt.pangor.classify.analysis.ClassifierDataSet;
import ca.ubc.ece.salt.pangor.classify.analysis.ClassifierFeatureVector;
import ca.ubc.ece.salt.pangor.classify.analysis.Transformer;
import ca.ubc.ece.salt.pangor.js.classify.protect.ProtectCommitAnalysisFactory;

public class TestProtectDomain {

	/**
	 * Tests data mining data set construction.
	 * @param args The command line arguments (i.e., old and new file names).
	 * @throws Exception
	 */
	protected void runTest(SourceCodeFileChange sourceFileChange,
						  List<ClassifierFeatureVector> expected,
						   boolean printAlerts) throws Exception {

		Commit commit = getCommit();
		commit.addSourceCodeFileChange(sourceFileChange);

		/* Builds the data set with our custom queries. */
		ClassifierDataSet dataSet = new ClassifierDataSet(null,
				new LinkedList<IRule>(), getUseQueries());

		/* Set up the commit analysis. */
		ICommitAnalysisFactory analysisFactory = new ProtectCommitAnalysisFactory(dataSet);
		CommitAnalysis commitAnalysis = analysisFactory.newInstance();

		/* Run the analysis. */
		commitAnalysis.analyze(commit);

        /* Print the data set. */
		dataSet.printDataSet();

        /* Verify the expected feature vectors match the actual feature vectors. */
		List<ClassifierFeatureVector> actual = dataSet.getFeatureVectors();
		Assert.assertTrue(actual.size() == expected.size());
        for(ClassifierFeatureVector fv : expected) {
        	Assert.assertTrue(actual.contains(fv));
        }
	}

	@Test
	public void testFalsey() throws Exception {

		/* The test files. */
		String src = "./test/input/learning/falsey_old.js";
		String dst = "./test/input/learning/falsey_new.js";

		/* Read the source files. */
		SourceCodeFileChange sourceCodeFileChange = getSourceCodeFileChange(src, dst);

		/* Build the expected feature vectors. */
		Commit commit = getCommit();
		List<ClassifierFeatureVector> expected = new LinkedList<ClassifierFeatureVector>();
		expected.add(new ClassifierFeatureVector(commit, "DESTINATION", "./test/input/learning/falsey_new.js", "MethodNA", "6", "TST", "PROTECT", "x_FALSEY_NE_IR"));

		this.runTest(sourceCodeFileChange, expected, true);
	}

	@Test
	public void testFalseyMulti() throws Exception {

		/* The test files. */
		String src = "./test/input/special_type_handling/sth_falsey_old.js";
		String dst = "./test/input/special_type_handling/sth_falsey_new.js";

		/* Read the source files. */
		SourceCodeFileChange sourceCodeFileChange = getSourceCodeFileChange(src, dst);

		/* Build the expected feature vectors. */
		Commit commit = getCommit();
		List<ClassifierFeatureVector> expected = new LinkedList<ClassifierFeatureVector>();
		expected.add(new ClassifierFeatureVector(commit, "DESTINATION", "./test/input/special_type_handling/sth_falsey_new.js", "MethodNA", "7", "TST", "PROTECT", "a_FALSEY_NE_IR"));
		expected.add(new ClassifierFeatureVector(commit, "DESTINATION", "./test/input/special_type_handling/sth_falsey_new.js", "MethodNA", "9", "TST", "PROTECT", "a_FALSEY_UNKNOWN_IR"));
		expected.add(new ClassifierFeatureVector(commit, "DESTINATION", "./test/input/special_type_handling/sth_falsey_new.js", "MethodNA", "10", "TST", "PROTECT", "a_FALSEY_UNKNOWN_IR"));
		expected.add(new ClassifierFeatureVector(commit, "DESTINATION", "./test/input/special_type_handling/sth_falsey_new.js", "MethodNA", "11", "TST", "PROTECT", "a_FALSEY_UNKNOWN_IR"));
		expected.add(new ClassifierFeatureVector(commit, "DESTINATION", "./test/input/special_type_handling/sth_falsey_new.js", "MethodNA", "10", "TST", "PROTECT", "b_FALSEY_NE_IR"));
		expected.add(new ClassifierFeatureVector(commit, "DESTINATION", "./test/input/special_type_handling/sth_falsey_new.js", "MethodNA", "10", "TST", "PROTECT", "c_FALSEY_NE_IR"));
		expected.add(new ClassifierFeatureVector(commit, "DESTINATION", "./test/input/special_type_handling/sth_falsey_new.js", "MethodNA", "11", "TST", "PROTECT", "b_FALSEY_NE_IR"));
		expected.add(new ClassifierFeatureVector(commit, "DESTINATION", "./test/input/special_type_handling/sth_falsey_new.js", "MethodNA", "11", "TST", "PROTECT", "c_FALSEY_NE_IR"));

		this.runTest(sourceCodeFileChange, expected, true);

	}

	@Test
	public void testExports() throws Exception {

		/* The test files. */
		String src = "./test/input/interproc/exports_old.js";
		String dst = "./test/input/interproc/exports_new.js";

		/* Read the source files. */
		SourceCodeFileChange sourceCodeFileChange = getSourceCodeFileChange(src, dst);

		/* Build the expected feature vectors. */
		Commit commit = getCommit();
		List<ClassifierFeatureVector> expected = new LinkedList<ClassifierFeatureVector>();
		expected.add(new ClassifierFeatureVector(commit, "DESTINATION", "./test/input/learning/falsey_new.js", "MethodNA", "6", "TST", "PROTECT", "x_FALSEY_NE_IR"));

		this.runTest(sourceCodeFileChange, expected, true);
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
	 * @throws ParserException when iris fails to parse the query string.
	 */
	public static Map<IQuery,Transformer> getUseQueries() throws ParserException {

		Map<IQuery, Transformer> queries = new HashMap<IQuery, Transformer>();

		/* The query that produces the results. */
		Parser parser = new Parser();
		parser.parse("?- SpecialType(?Version,?File,?Line,?StatementID,?Identifier,?Type,?Element,?Change).");
		IQuery query = parser.getQueries().get(0);

		/* Transforms the query results to a ClassifierFeatureVector. */
		Transformer transformer = (commit, tuple) -> {
				return new ClassifierFeatureVector(commit,
						tuple.get(0).toString().replace("\'", ""),			// Version
						tuple.get(1).toString().replace("\'", ""), 			// Class
						"MethodNA",											// Method
						tuple.get(2).toString().replace("\'", ""),			// Line
						"TST",												// Type
						"PROTECT",											// Subtype
						tuple.get(4).toString().replace("\'", "")
							+ "_" + tuple.get(5).toString().replace("\'", "")
							+ "_" + tuple.get(6).toString().replace("\'", "")
							+ "_" + tuple.get(7).toString().replace("\'", ""));	// Description
		};

		queries.put(query, transformer);

		return queries;

	}

}