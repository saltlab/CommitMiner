package ca.ubc.ece.salt.pangor.js.diff.test;


import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.tuple.Pair;
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
import ca.ubc.ece.salt.pangor.js.diff.DiffCommitAnalysisFactory;

public class TestDiffAnalysis {

	/**
	 * Tests data mining data set construction.
	 * @param args The command line arguments (i.e., old and new file names).
	 * @throws Exception
	 */
	protected void runTest(SourceCodeFileChange sourceFileChange,
						  List<ClassifierFeatureVector> expected,
						   boolean checkSize) throws Exception {

		Commit commit = getCommit();
		commit.addSourceCodeFileChange(sourceFileChange);

		/* Builds the data set with our custom queries. */
		ClassifierDataSet dataSet = new ClassifierDataSet(null,
				new LinkedList<IRule>(), getUseQueries());

		/* Set up the analysis. */
		ICommitAnalysisFactory commitFactory = new DiffCommitAnalysisFactory(dataSet);
		CommitAnalysis commitAnalysis = commitFactory.newInstance();

		/* Run the analysis. */
		commitAnalysis.analyze(commit);

        /* Print the data set. */
		dataSet.printDataSet();

        /* Verify the expected feature vectors match the actual feature vectors. */
		List<ClassifierFeatureVector> actual = dataSet.getFeatureVectors();
		if(checkSize) Assert.assertTrue(actual.size() == expected.size());
        for(ClassifierFeatureVector fv : expected) {
        	Assert.assertTrue(actual.contains(fv));
        }
	}

	@Test
	public void testDyn() throws Exception {

		/* The test files. */
		String src = "./test/input/diff/dyn_old.js";
		String dst = "./test/input/diff/dyn_new.js";

		/* Read the source files. */
		SourceCodeFileChange sourceCodeFileChange = getSourceCodeFileChange(src, dst);

		/* Build the expected feature vectors. */
		Commit commit = getCommit();
		List<ClassifierFeatureVector> expected = new LinkedList<ClassifierFeatureVector>();

		/* The value-diff alerts. */
		expected.add(new ClassifierFeatureVector(commit, "DESTINATION", "./test/input/diff/dyn_new.js", "MethodNA", "14", "TST", "VAL", "cb_Change:CHANGED"));
		expected.add(new ClassifierFeatureVector(commit, "DESTINATION", "./test/input/diff/dyn_new.js", "MethodNA", "16", "TST", "VAL", "cb_Change:CHANGED"));
		expected.add(new ClassifierFeatureVector(commit, "DESTINATION", "./test/input/diff/dyn_new.js", "MethodNA", "27", "TST", "VAL", "semver_Change:CHANGED"));
		expected.add(new ClassifierFeatureVector(commit, "DESTINATION", "./test/input/diff/dyn_new.js", "MethodNA", "33", "TST", "VAL", "semver_Change:CHANGED"));
		expected.add(new ClassifierFeatureVector(commit, "DESTINATION", "./test/input/diff/dyn_new.js", "MethodNA", "32", "TST", "VAL", "err_Change:CHANGED"));
		expected.add(new ClassifierFeatureVector(commit, "DESTINATION", "./test/input/diff/dyn_new.js", "MethodNA", "32", "TST", "VAL", "data_Change:CHANGED"));
		expected.add(new ClassifierFeatureVector(commit, "DESTINATION", "./test/input/diff/dyn_new.js", "MethodNA", "33", "TST", "VAL", "err_Change:CHANGED"));
		expected.add(new ClassifierFeatureVector(commit, "DESTINATION", "./test/input/diff/dyn_new.js", "MethodNA", "33", "TST", "VAL", "data_Change:CHANGED"));
		expected.add(new ClassifierFeatureVector(commit, "DESTINATION", "./test/input/diff/dyn_new.js", "MethodNA", "36", "TST", "VAL", "PM2_SILENT_Change:CHANGED"));
		expected.add(new ClassifierFeatureVector(commit, "DESTINATION", "./test/input/diff/dyn_new.js", "MethodNA", "43", "TST", "VAL", "PM2_SILENT_Change:CHANGED"));

		/* The environment-diff alerts. */
		expected.add(new ClassifierFeatureVector(commit, "DESTINATION", "./test/input/diff/dyn_new.js", "MethodNA", "27", "TST", "ENV", "semver_ENV_Change:CHANGED"));
		expected.add(new ClassifierFeatureVector(commit, "DESTINATION", "./test/input/diff/dyn_new.js", "MethodNA", "33", "TST", "ENV", "semver_ENV_Change:CHANGED"));
		expected.add(new ClassifierFeatureVector(commit, "DESTINATION", "./test/input/diff/dyn_new.js", "MethodNA", "32", "TST", "ENV", "data_ENV_Change:CHANGED"));
		expected.add(new ClassifierFeatureVector(commit, "DESTINATION", "./test/input/diff/dyn_new.js", "MethodNA", "32", "TST", "ENV", "err_ENV_Change:CHANGED"));
		expected.add(new ClassifierFeatureVector(commit, "DESTINATION", "./test/input/diff/dyn_new.js", "MethodNA", "33", "TST", "ENV", "data_ENV_Change:CHANGED"));
		expected.add(new ClassifierFeatureVector(commit, "DESTINATION", "./test/input/diff/dyn_new.js", "MethodNA", "33", "TST", "ENV", "err_ENV_Change:CHANGED"));

		/* The control-flow-diff alerts. */
		expected.add(new ClassifierFeatureVector(commit, "DESTINATION", "./test/input/diff/dyn_new.js", "MethodNA", "14", "TST", "CONTROL", "Change:CHANGED"));
		expected.add(new ClassifierFeatureVector(commit, "DESTINATION", "./test/input/diff/dyn_new.js", "MethodNA", "15", "TST", "CONTROL", "Change:CHANGED"));
		expected.add(new ClassifierFeatureVector(commit, "DESTINATION", "./test/input/diff/dyn_new.js", "MethodNA", "16", "TST", "CONTROL", "Change:CHANGED"));
		expected.add(new ClassifierFeatureVector(commit, "DESTINATION", "./test/input/diff/dyn_new.js", "MethodNA", "32", "TST", "CONTROL", "Change:CHANGED"));
		expected.add(new ClassifierFeatureVector(commit, "DESTINATION", "./test/input/diff/dyn_new.js", "MethodNA", "33", "TST", "CONTROL", "Change:CHANGED"));
		expected.add(new ClassifierFeatureVector(commit, "DESTINATION", "./test/input/diff/dyn_new.js", "MethodNA", "36", "TST", "CONTROL", "Change:CHANGED"));
		expected.add(new ClassifierFeatureVector(commit, "DESTINATION", "./test/input/diff/dyn_new.js", "MethodNA", "37", "TST", "CONTROL", "Change:CHANGED"));
		expected.add(new ClassifierFeatureVector(commit, "DESTINATION", "./test/input/diff/dyn_new.js", "MethodNA", "40", "TST", "CONTROL", "Change:CHANGED"));
		expected.add(new ClassifierFeatureVector(commit, "DESTINATION", "./test/input/diff/dyn_new.js", "MethodNA", "41", "TST", "CONTROL", "Change:CHANGED"));
		expected.add(new ClassifierFeatureVector(commit, "DESTINATION", "./test/input/diff/dyn_new.js", "MethodNA", "42", "TST", "CONTROL", "Change:CHANGED"));
		expected.add(new ClassifierFeatureVector(commit, "DESTINATION", "./test/input/diff/dyn_new.js", "MethodNA", "43", "TST", "CONTROL", "Change:CHANGED"));
		expected.add(new ClassifierFeatureVector(commit, "DESTINATION", "./test/input/diff/dyn_new.js", "MethodNA", "45", "TST", "CONTROL", "Change:CHANGED"));
		expected.add(new ClassifierFeatureVector(commit, "DESTINATION", "./test/input/diff/dyn_new.js", "MethodNA", "46", "TST", "CONTROL", "Change:CHANGED"));
		expected.add(new ClassifierFeatureVector(commit, "DESTINATION", "./test/input/diff/dyn_new.js", "MethodNA", "47", "TST", "CONTROL", "Change:CHANGED"));
		expected.add(new ClassifierFeatureVector(commit, "DESTINATION", "./test/input/diff/dyn_new.js", "MethodNA", "48", "TST", "CONTROL", "Change:CHANGED"));
		expected.add(new ClassifierFeatureVector(commit, "DESTINATION", "./test/input/diff/dyn_new.js", "MethodNA", "49", "TST", "CONTROL", "Change:CHANGED"));

		/* The ast-diff alerts. */
		expected.add(new ClassifierFeatureVector(commit, "DESTINATION", "./test/input/diff/dyn_new.js", "MethodNA", "27", "TST", "AST", "INSERTED"));
		expected.add(new ClassifierFeatureVector(commit, "DESTINATION", "./test/input/diff/dyn_new.js", "MethodNA", "32", "TST", "AST", "INSERTED"));
		expected.add(new ClassifierFeatureVector(commit, "DESTINATION", "./test/input/diff/dyn_new.js", "MethodNA", "33", "TST", "AST", "INSERTED"));
		expected.add(new ClassifierFeatureVector(commit, "DESTINATION", "./test/input/diff/dyn_new.js", "MethodNA", "36", "TST", "AST", "INSERTED"));
		expected.add(new ClassifierFeatureVector(commit, "DESTINATION", "./test/input/diff/dyn_new.js", "MethodNA", "37", "TST", "AST", "INSERTED"));
		expected.add(new ClassifierFeatureVector(commit, "DESTINATION", "./test/input/diff/dyn_new.js", "MethodNA", "40", "TST", "AST", "MOVED"));
		expected.add(new ClassifierFeatureVector(commit, "DESTINATION", "./test/input/diff/dyn_new.js", "MethodNA", "41", "TST", "AST", "MOVED"));
		expected.add(new ClassifierFeatureVector(commit, "DESTINATION", "./test/input/diff/dyn_new.js", "MethodNA", "42", "TST", "AST", "MOVED"));
		expected.add(new ClassifierFeatureVector(commit, "DESTINATION", "./test/input/diff/dyn_new.js", "MethodNA", "43", "TST", "AST", "INSERTED"));
		expected.add(new ClassifierFeatureVector(commit, "DESTINATION", "./test/input/diff/dyn_new.js", "MethodNA", "45", "TST", "AST", "MOVED"));
		expected.add(new ClassifierFeatureVector(commit, "DESTINATION", "./test/input/diff/dyn_new.js", "MethodNA", "46", "TST", "AST", "MOVED"));
		expected.add(new ClassifierFeatureVector(commit, "DESTINATION", "./test/input/diff/dyn_new.js", "MethodNA", "47", "TST", "AST", "MOVED"));
		expected.add(new ClassifierFeatureVector(commit, "DESTINATION", "./test/input/diff/dyn_new.js", "MethodNA", "48", "TST", "AST", "MOVED"));
		expected.add(new ClassifierFeatureVector(commit, "DESTINATION", "./test/input/diff/dyn_new.js", "MethodNA", "49", "TST", "AST", "MOVED"));

		expected.add(new ClassifierFeatureVector(commit, "SOURCE", "./test/input/diff/dyn_new.js", "MethodNA", "30", "TST", "AST", "MOVED"));
		expected.add(new ClassifierFeatureVector(commit, "SOURCE", "./test/input/diff/dyn_new.js", "MethodNA", "31", "TST", "AST", "MOVED"));
		expected.add(new ClassifierFeatureVector(commit, "SOURCE", "./test/input/diff/dyn_new.js", "MethodNA", "32", "TST", "AST", "MOVED"));
		expected.add(new ClassifierFeatureVector(commit, "SOURCE", "./test/input/diff/dyn_new.js", "MethodNA", "34", "TST", "AST", "MOVED"));
		expected.add(new ClassifierFeatureVector(commit, "SOURCE", "./test/input/diff/dyn_new.js", "MethodNA", "35", "TST", "AST", "MOVED"));
		expected.add(new ClassifierFeatureVector(commit, "SOURCE", "./test/input/diff/dyn_new.js", "MethodNA", "36", "TST", "AST", "MOVED"));
		expected.add(new ClassifierFeatureVector(commit, "SOURCE", "./test/input/diff/dyn_new.js", "MethodNA", "37", "TST", "AST", "MOVED"));
		expected.add(new ClassifierFeatureVector(commit, "SOURCE", "./test/input/diff/dyn_new.js", "MethodNA", "38", "TST", "AST", "MOVED"));

		/* The test-diff alerts. */
		expected.add(new ClassifierFeatureVector(commit, "DESTINATION", "./test/input/diff/dyn_new.js", "MethodNA", "27", "TST", "LINE", "INSERT"));
		expected.add(new ClassifierFeatureVector(commit, "DESTINATION", "./test/input/diff/dyn_new.js", "MethodNA", "54", "TST", "LINE", "INSERT"));

		this.runTest(sourceCodeFileChange, expected, true);
	}

	@Test
	public void testPM2() throws Exception {

		/* The test files. */
		String src = "./test/input/diff/pm2_old.js";
		String dst = "./test/input/diff/pm2_new.js";

		/* Read the source files. */
		SourceCodeFileChange sourceCodeFileChange = getSourceCodeFileChange(src, dst);

		/* Build the expected feature vectors. */
		Commit commit = getCommit();
		List<ClassifierFeatureVector> expected = new LinkedList<ClassifierFeatureVector>();
		expected.add(new ClassifierFeatureVector(commit, "DESTINATION", "./test/input/diff/pm2_new.js", "MethodNA", "480", "TST", "VAL", "cb_Change:CHANGED"));
		expected.add(new ClassifierFeatureVector(commit, "DESTINATION", "./test/input/diff/pm2_new.js", "MethodNA", "482", "TST", "VAL", "cb_Change:CHANGED"));
		expected.add(new ClassifierFeatureVector(commit, "DESTINATION", "./test/input/diff/pm2_new.js", "MethodNA", "1662", "TST", "VAL", "semver_Change:CHANGED"));
		expected.add(new ClassifierFeatureVector(commit, "DESTINATION", "./test/input/diff/pm2_new.js", "MethodNA", "1668", "TST", "VAL", "semver_Change:CHANGED"));
		expected.add(new ClassifierFeatureVector(commit, "DESTINATION", "./test/input/diff/pm2_new.js", "MethodNA", "1667", "TST", "VAL", "err_Change:CHANGED"));
		expected.add(new ClassifierFeatureVector(commit, "DESTINATION", "./test/input/diff/pm2_new.js", "MethodNA", "1667", "TST", "VAL", "data_Change:CHANGED"));
		expected.add(new ClassifierFeatureVector(commit, "DESTINATION", "./test/input/diff/pm2_new.js", "MethodNA", "1668", "TST", "VAL", "err_Change:CHANGED"));
		expected.add(new ClassifierFeatureVector(commit, "DESTINATION", "./test/input/diff/pm2_new.js", "MethodNA", "1668", "TST", "VAL", "data_Change:CHANGED"));
		expected.add(new ClassifierFeatureVector(commit, "DESTINATION", "./test/input/diff/pm2_new.js", "MethodNA", "1671", "TST", "VAL", "PM2_SILENT_Change:CHANGED"));
		expected.add(new ClassifierFeatureVector(commit, "DESTINATION", "./test/input/diff/pm2_new.js", "MethodNA", "1678", "TST", "VAL", "PM2_SILENT_Change:CHANGED"));

		/* The environment-diff alerts. */
		expected.add(new ClassifierFeatureVector(commit, "DESTINATION", "./test/input/diff/pm2_new.js", "MethodNA", "1662", "TST", "ENV", "semver_ENV_Change:CHANGED"));
		expected.add(new ClassifierFeatureVector(commit, "DESTINATION", "./test/input/diff/pm2_new.js", "MethodNA", "1668", "TST", "ENV", "semver_ENV_Change:CHANGED"));
		expected.add(new ClassifierFeatureVector(commit, "DESTINATION", "./test/input/diff/pm2_new.js", "MethodNA", "1667", "TST", "ENV", "data_ENV_Change:CHANGED"));
		expected.add(new ClassifierFeatureVector(commit, "DESTINATION", "./test/input/diff/pm2_new.js", "MethodNA", "1667", "TST", "ENV", "err_ENV_Change:CHANGED"));
		expected.add(new ClassifierFeatureVector(commit, "DESTINATION", "./test/input/diff/pm2_new.js", "MethodNA", "1668", "TST", "ENV", "data_ENV_Change:CHANGED"));
		expected.add(new ClassifierFeatureVector(commit, "DESTINATION", "./test/input/diff/pm2_new.js", "MethodNA", "1668", "TST", "ENV", "err_ENV_Change:CHANGED"));

		/* The control-flow-diff alerts. */
		expected.add(new ClassifierFeatureVector(commit, "DESTINATION", "./test/input/diff/pm2_new.js", "MethodNA", "480", "TST", "CONTROL", "Change:CHANGED"));
		expected.add(new ClassifierFeatureVector(commit, "DESTINATION", "./test/input/diff/pm2_new.js", "MethodNA", "481", "TST", "CONTROL", "Change:CHANGED"));
		expected.add(new ClassifierFeatureVector(commit, "DESTINATION", "./test/input/diff/pm2_new.js", "MethodNA", "482", "TST", "CONTROL", "Change:CHANGED"));
		expected.add(new ClassifierFeatureVector(commit, "DESTINATION", "./test/input/diff/pm2_new.js", "MethodNA", "1667", "TST", "CONTROL", "Change:CHANGED"));
		expected.add(new ClassifierFeatureVector(commit, "DESTINATION", "./test/input/diff/pm2_new.js", "MethodNA", "1668", "TST", "CONTROL", "Change:CHANGED"));
		expected.add(new ClassifierFeatureVector(commit, "DESTINATION", "./test/input/diff/pm2_new.js", "MethodNA", "1671", "TST", "CONTROL", "Change:CHANGED"));
		expected.add(new ClassifierFeatureVector(commit, "DESTINATION", "./test/input/diff/pm2_new.js", "MethodNA", "1672", "TST", "CONTROL", "Change:CHANGED"));
		expected.add(new ClassifierFeatureVector(commit, "DESTINATION", "./test/input/diff/pm2_new.js", "MethodNA", "1675", "TST", "CONTROL", "Change:CHANGED"));
		expected.add(new ClassifierFeatureVector(commit, "DESTINATION", "./test/input/diff/pm2_new.js", "MethodNA", "1676", "TST", "CONTROL", "Change:CHANGED"));
		expected.add(new ClassifierFeatureVector(commit, "DESTINATION", "./test/input/diff/pm2_new.js", "MethodNA", "1677", "TST", "CONTROL", "Change:CHANGED"));
		expected.add(new ClassifierFeatureVector(commit, "DESTINATION", "./test/input/diff/pm2_new.js", "MethodNA", "1678", "TST", "CONTROL", "Change:CHANGED"));
		expected.add(new ClassifierFeatureVector(commit, "DESTINATION", "./test/input/diff/pm2_new.js", "MethodNA", "1680", "TST", "CONTROL", "Change:CHANGED"));
		expected.add(new ClassifierFeatureVector(commit, "DESTINATION", "./test/input/diff/pm2_new.js", "MethodNA", "1681", "TST", "CONTROL", "Change:CHANGED"));
		expected.add(new ClassifierFeatureVector(commit, "DESTINATION", "./test/input/diff/pm2_new.js", "MethodNA", "1682", "TST", "CONTROL", "Change:CHANGED"));
		expected.add(new ClassifierFeatureVector(commit, "DESTINATION", "./test/input/diff/pm2_new.js", "MethodNA", "1683", "TST", "CONTROL", "Change:CHANGED"));
		expected.add(new ClassifierFeatureVector(commit, "DESTINATION", "./test/input/diff/pm2_new.js", "MethodNA", "1684", "TST", "CONTROL", "Change:CHANGED"));

		/* The ast-diff alerts. */
		expected.add(new ClassifierFeatureVector(commit, "DESTINATION", "./test/input/diff/pm2_new.js", "MethodNA", "1662", "TST", "AST", "INSERTED"));
		expected.add(new ClassifierFeatureVector(commit, "DESTINATION", "./test/input/diff/pm2_new.js", "MethodNA", "1667", "TST", "AST", "INSERTED"));
		expected.add(new ClassifierFeatureVector(commit, "DESTINATION", "./test/input/diff/pm2_new.js", "MethodNA", "1668", "TST", "AST", "INSERTED"));
		expected.add(new ClassifierFeatureVector(commit, "DESTINATION", "./test/input/diff/pm2_new.js", "MethodNA", "1671", "TST", "AST", "INSERTED"));
		expected.add(new ClassifierFeatureVector(commit, "DESTINATION", "./test/input/diff/pm2_new.js", "MethodNA", "1672", "TST", "AST", "INSERTED"));
		expected.add(new ClassifierFeatureVector(commit, "DESTINATION", "./test/input/diff/pm2_new.js", "MethodNA", "1675", "TST", "AST", "MOVED"));
		expected.add(new ClassifierFeatureVector(commit, "DESTINATION", "./test/input/diff/pm2_new.js", "MethodNA", "1676", "TST", "AST", "MOVED"));
		expected.add(new ClassifierFeatureVector(commit, "DESTINATION", "./test/input/diff/pm2_new.js", "MethodNA", "1677", "TST", "AST", "MOVED"));
		expected.add(new ClassifierFeatureVector(commit, "DESTINATION", "./test/input/diff/pm2_new.js", "MethodNA", "1678", "TST", "AST", "INSERTED"));
		expected.add(new ClassifierFeatureVector(commit, "DESTINATION", "./test/input/diff/pm2_new.js", "MethodNA", "1680", "TST", "AST", "MOVED"));
		expected.add(new ClassifierFeatureVector(commit, "DESTINATION", "./test/input/diff/pm2_new.js", "MethodNA", "1681", "TST", "AST", "MOVED"));
		expected.add(new ClassifierFeatureVector(commit, "DESTINATION", "./test/input/diff/pm2_new.js", "MethodNA", "1682", "TST", "AST", "MOVED"));
		expected.add(new ClassifierFeatureVector(commit, "DESTINATION", "./test/input/diff/pm2_new.js", "MethodNA", "1683", "TST", "AST", "MOVED"));
		expected.add(new ClassifierFeatureVector(commit, "DESTINATION", "./test/input/diff/pm2_new.js", "MethodNA", "1684", "TST", "AST", "MOVED"));

		expected.add(new ClassifierFeatureVector(commit, "SOURCE", "./test/input/diff/pm2_new.js", "MethodNA", "1665", "TST", "AST", "MOVED"));
		expected.add(new ClassifierFeatureVector(commit, "SOURCE", "./test/input/diff/pm2_new.js", "MethodNA", "1666", "TST", "AST", "MOVED"));
		expected.add(new ClassifierFeatureVector(commit, "SOURCE", "./test/input/diff/pm2_new.js", "MethodNA", "1667", "TST", "AST", "MOVED"));
		expected.add(new ClassifierFeatureVector(commit, "SOURCE", "./test/input/diff/pm2_new.js", "MethodNA", "1669", "TST", "AST", "MOVED"));
		expected.add(new ClassifierFeatureVector(commit, "SOURCE", "./test/input/diff/pm2_new.js", "MethodNA", "1670", "TST", "AST", "MOVED"));
		expected.add(new ClassifierFeatureVector(commit, "SOURCE", "./test/input/diff/pm2_new.js", "MethodNA", "1671", "TST", "AST", "MOVED"));
		expected.add(new ClassifierFeatureVector(commit, "SOURCE", "./test/input/diff/pm2_new.js", "MethodNA", "1672", "TST", "AST", "MOVED"));
		expected.add(new ClassifierFeatureVector(commit, "SOURCE", "./test/input/diff/pm2_new.js", "MethodNA", "1673", "TST", "AST", "MOVED"));

		this.runTest(sourceCodeFileChange, expected, false);
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

//		Pair<IQuery, Transformer> valueQuery = getValueQuery();
//		queries.put(valueQuery.getLeft(), valueQuery.getRight());
//
//		Pair<IQuery, Transformer> environmentQuery = getEnvironmentQuery();
//		queries.put(environmentQuery.getLeft(), environmentQuery.getRight());
//
//		Pair<IQuery, Transformer> controlQuery = getControlQuery();
//		queries.put(controlQuery.getLeft(), controlQuery.getRight());
//
//		Pair<IQuery, Transformer> astQuery = getAstQuery();
//		queries.put(astQuery.getLeft(), astQuery.getRight());

//		Pair<IQuery, Transformer> lineQuery = getLineQuery();
//		queries.put(lineQuery.getLeft(), lineQuery.getRight());

		Pair<IQuery, Transformer> totalLinesQuery = getTotalLinesQuery();
		queries.put(totalLinesQuery.getLeft(), totalLinesQuery.getRight());

		return queries;

	}

	/**
	 * @return The query for extracting value-diff alerts.
	 * @throws ParserException for incorrect query strings.
	 */
	private static Pair<IQuery, Transformer> getValueQuery() throws ParserException {

		String qs = "";
		qs += "?- Value(?Version,?File,?Line,?StatementID,?Identifier,?ValChange)";
		qs += ", EQUAL(?ValChange, 'Change:CHANGED').";

		/* The query that produces the results. */
		Parser parser = new Parser();
		parser.parse(qs);
		IQuery query = parser.getQueries().get(0);

		/* Transforms the query results to a ClassifierFeatureVector. */
		Transformer transformer = (commit, tuple) -> {
				return new ClassifierFeatureVector(commit,
						tuple.get(0).toString().replace("\'", ""),			// Version
						tuple.get(1).toString().replace("\'", ""), 			// Class
						"MethodNA",											// Method
						tuple.get(2).toString().replace("\'", ""),			// Line
						"TST",												// Type
						"VAL",											// Subtype
						tuple.get(4).toString().replace("\'", "")
							+ "_" + tuple.get(5).toString().replace("\'", ""));	// Description
		};

		return Pair.of(query, transformer);

	}

	/**
	 * @return The query for extracting environment-diff alerts.
	 * @throws ParserException for incorrect query strings.
	 */
	private static Pair<IQuery, Transformer> getEnvironmentQuery() throws ParserException {

		String qs = "";
		qs += "?- Environment(?Version,?File,?Line,?StatementID,?Identifier,?Type,?EnvChange)";
		qs += ", EQUAL(?EnvChange, 'Change:CHANGED').";

		/* The query that produces the results. */
		Parser parser = new Parser();
		parser.parse(qs);
		IQuery query = parser.getQueries().get(0);

		/* Transforms the query results to a ClassifierFeatureVector. */
		Transformer transformer = (commit, tuple) -> {
				return new ClassifierFeatureVector(commit,
						tuple.get(0).toString().replace("\'", ""),			// Version
						tuple.get(1).toString().replace("\'", ""), 			// Class
						"MethodNA",											// Method
						tuple.get(2).toString().replace("\'", ""),			// Line
						"TST",												// Type
						"ENV",											// Subtype
						tuple.get(4).toString().replace("\'", "")
							+ "_" + tuple.get(5).toString().replace("\'", "")
							+ "_" + tuple.get(6).toString().replace("\'", ""));	// Description
		};

		return Pair.of(query, transformer);

	}

	/**
	 * @return The query for extracting control-flow-diff alerts.
	 * @throws ParserException for incorrect query strings.
	 */
	private static Pair<IQuery, Transformer> getControlQuery() throws ParserException {

		String qs = "";
		qs += "?- Control(?Version,?File,?Line,?StatementID,?Type,?Change)";
		qs += ", EQUAL(?Change, 'Change:CHANGED').";

		/* The query that produces the results. */
		Parser parser = new Parser();
		parser.parse(qs);
		IQuery query = parser.getQueries().get(0);

		/* Transforms the query results to a ClassifierFeatureVector. */
		Transformer transformer = (commit, tuple) -> {
				return new ClassifierFeatureVector(commit,
						tuple.get(0).toString().replace("\'", ""),			// Version
						tuple.get(1).toString().replace("\'", ""), 			// Class
						"MethodNA",											// Method
						tuple.get(2).toString().replace("\'", ""),			// Line
						"TST",												// Type
						"CONTROL",											// Subtype
						tuple.get(5).toString().replace("\'", ""));			// Description
		};

		return Pair.of(query, transformer);

	}

	/**
	 * @return The query for extracting AST-diff alerts.
	 * @throws ParserException for incorrect query strings.
	 */
	private static Pair<IQuery, Transformer> getAstQuery() throws ParserException {

		String qs = "";
		qs += "?- AST(?Version,?File,?Line,?StatementID,?Change)";
		qs += ", NOT_EQUAL(?Change, 'UNCHANGED').";

		/* The query that produces the results. */
		Parser parser = new Parser();
		parser.parse(qs);
		IQuery query = parser.getQueries().get(0);

		/* Transforms the query results to a ClassifierFeatureVector. */
		Transformer transformer = (commit, tuple) -> {
				return new ClassifierFeatureVector(commit,
						tuple.get(0).toString().replace("\'", ""),			// Version
						tuple.get(1).toString().replace("\'", ""), 			// Class
						"MethodNA",											// Method
						tuple.get(2).toString().replace("\'", ""),			// Line
						"TST",												// Type
						"AST",												// Subtype
						tuple.get(4).toString().replace("\'", ""));			// Description
		};

		return Pair.of(query, transformer);
	}

	/**
	 * @return The query for extracting total number of lines in a file.
	 * @throws ParserException for incorrect query strings.
	 */
	private static Pair<IQuery, Transformer> getTotalLinesQuery() throws ParserException {

		String qs = "";
		qs += "?- TotalLines(?Version,?File,?TotalLines).";

		/* The query that produces the results. */
		Parser parser = new Parser();
		parser.parse(qs);
		IQuery query = parser.getQueries().get(0);

		/* Transforms the query results to a ClassifierFeatureVector. */
		Transformer transformer = (commit, tuple) -> {
				return new ClassifierFeatureVector(commit,
						tuple.get(0).toString().replace("\'", ""),			// Version
						tuple.get(1).toString().replace("\'", ""), 			// Class
						"MethodNA",											// Method
						"NA",												// Line
						"DIFF",												// Type
						"TOTAL_LINES",										// Subtype
						tuple.get(2).toString().replace("\'", ""));			// Description
		};

		return Pair.of(query, transformer);

	}

	/**
	 * @return The query for extracting line-diff alerts.
	 * @throws ParserException for incorrect query strings.
	 */
	private static Pair<IQuery, Transformer> getLineQuery() throws ParserException {

		String qs = "";
		qs += "?- Line(?Version,?File,?Line,?Change)";
		qs += ", NOT_EQUAL(?Change, 'UNCHANGED').";

		/* The query that produces the results. */
		Parser parser = new Parser();
		parser.parse(qs);
		IQuery query = parser.getQueries().get(0);

		/* Transforms the query results to a ClassifierFeatureVector. */
		Transformer transformer = (commit, tuple) -> {
				return new ClassifierFeatureVector(commit,
						tuple.get(0).toString().replace("\'", ""),			// Version
						tuple.get(1).toString().replace("\'", ""), 			// Class
						"MethodNA",											// Method
						tuple.get(2).toString().replace("\'", ""),			// Line
						"TST",												// Type
						"LINE",												// Subtype
						tuple.get(3).toString().replace("\'", ""));			// Description
		};

		return Pair.of(query, transformer);

	}

}