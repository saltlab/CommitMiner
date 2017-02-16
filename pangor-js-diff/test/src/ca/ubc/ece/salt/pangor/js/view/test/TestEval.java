package ca.ubc.ece.salt.pangor.js.view.test;


import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
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
import ca.ubc.ece.salt.pangor.js.diff.view.HTMLMultiDiffViewer;
import ca.ubc.ece.salt.pangor.js.diff.view.HTMLUnixDiffViewer;

public class TestEval {

	/**
	 * Tests data mining data set construction.
	 * @param args The command line arguments (i.e., old and new file names).
	 * @throws Exception
	 */
	protected void runTest(
			String src, String dst, String out, boolean checkSize) throws Exception {

		/* Read the source files. */
		String srcCode = new String(Files.readAllBytes(Paths.get(src)));
		String dstCode = new String(Files.readAllBytes(Paths.get(dst)));

		/* Read the source files. */
		List<SourceCodeFileChange> sourceCodeFileChanges = new LinkedList<SourceCodeFileChange>();
		sourceCodeFileChanges.add(getSourceCodeFileChange(src, dst));

		Commit commit = getCommit();
		for(SourceCodeFileChange sourceFileChange : sourceCodeFileChanges) {
			commit.addSourceCodeFileChange(sourceFileChange);
		}

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

        /* Return the alerts. */
		List<ClassifierFeatureVector> alerts = dataSet.getFeatureVectors();

		/* Only annotate the destination file. The source file isn't especially useful. */
		String annotatedDst = HTMLMultiDiffViewer.annotate(dstCode, alerts, "DESTINATION");

		/* Combine the annotated file with the UnixDiff. */
		String annotatedCombined = HTMLUnixDiffViewer.annotate(srcCode, dstCode, annotatedDst);
		Files.write(Paths.get(out), annotatedCombined.getBytes(), StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);

	}

	@Test
	public void test_nodemon() throws Exception {

		String src = "./test/input/20/nodemon-9853e089b418705bf132f69f36114821ed1a211a_old.js";
		String dst = "./test/input/20/nodemon-9853e089b418705bf132f69f36114821ed1a211a_new.js";
		String out = "./output/20/nodemon-9853e089b418705bf132f69f36114821ed1a211a.html";

		runTest(src, dst, out, false);

	}

	@Test
	public void test_tagspaces() throws Exception {

		String src = "./test/input/20/tagspaces-615f9bd55ca805eb5865e5a9ff0f82d75240e89b_old.js";
		String dst = "./test/input/20/tagspaces-615f9bd55ca805eb5865e5a9ff0f82d75240e89b_new.js";
		String out = "./output/20/tagspaces-615f9bd55ca805eb5865e5a9ff0f82d75240e89b.html";

		runTest(src, dst, out, false);

	}

	@Test
	public void test_moment() throws Exception {

		String src = "./test/input/20/moment-3866df1baec8600e0f861a24acf6bffd443cd75a_old.js";
		String dst = "./test/input/20/moment-3866df1baec8600e0f861a24acf6bffd443cd75a_new.js";
		String out = "./output/20/moment-3866df1baec8600e0f861a24acf6bffd443cd75a.html";

		runTest(src, dst, out, false);

	}

	@Test
	public void test_generator() throws Exception {

		String src = "./test/input/20/generator-f3a63f7a71cda9f977f66a11736858d43418b074_old.js";
		String dst = "./test/input/20/generator-f3a63f7a71cda9f977f66a11736858d43418b074_new.js";
		String out = "./output/20/generator-f3a63f7a71cda9f977f66a11736858d43418b074.html";

		runTest(src, dst, out, false);

	}


	@Test
	public void test_cheerio() throws Exception {

		String src = "./test/input/20/cheerio-e65ad72cad8fb696e0f3475b127c93492feca04d_old.js";
		String dst = "./test/input/20/cheerio-e65ad72cad8fb696e0f3475b127c93492feca04d_new.js";
		String out = "./output/20/cheerio-e65ad72cad8fb696e0f3475b127c93492feca04d.html";

		runTest(src, dst, out, false);

	}

	@Test
	public void test_pm2() throws Exception {

		String src = "./test/input/20/pm2-966fcc35a4925eb2d446a3dd12c84036d1615c8c_old.js";
		String dst = "./test/input/20/pm2-966fcc35a4925eb2d446a3dd12c84036d1615c8c_new.js";
		String out = "./output/20/pm2-966fcc35a4925eb2d446a3dd12c84036d1615c8c.html";

		runTest(src, dst, out, false);

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

		Pair<IQuery, Transformer> valueQuery = getValueQuery();
		queries.put(valueQuery.getLeft(), valueQuery.getRight());

		Pair<IQuery, Transformer> environmentQuery = getEnvironmentQuery();
		queries.put(environmentQuery.getLeft(), environmentQuery.getRight());

		Pair<IQuery, Transformer> controlQuery = getControlQuery();
		queries.put(controlQuery.getLeft(), controlQuery.getRight());

		return queries;

	}

	/**
	 * @return The query for extracting value-diff alerts.
	 * @throws ParserException for incorrect query strings.
	 */
	private static Pair<IQuery, Transformer> getValueQuery() throws ParserException {

		String qs = "";
		qs += "?- Value(?Version,?File,?Line,?Pos,?Len,?StatementID,?Identifier,?ValChange)";
		qs += ", NOT_EQUAL(?ValChange, 'Change:UNCHANGED')";
		qs += ", NOT_EQUAL(?ValChange, 'Change:BOTTOM').";

		/* The query that produces the results. */
		Parser parser = new Parser();
		parser.parse(qs);
		IQuery query = parser.getQueries().get(0);

		/* Transforms the query results to a ClassifierFeatureVector. */
		Transformer transformer = (commit, tuple) -> {
				return new ClassifierFeatureVector(commit,
						tuple.get(0).toString().replace("\'", ""),			// Version
						tuple.get(1).toString().replace("\'", ""), 			// Class
						tuple.get(5).toString().replace("\'",  ""),			// AST Node ID
						tuple.get(2).toString().replace("\'", ""),			// Line
						tuple.get(3).toString().replace("\'", ""),			// Position
						tuple.get(4).toString().replace("\'", ""),			// Length
						"DIFF",												// Type
						"VAL",												// Subtype
						tuple.get(6).toString().replace("\'", "")
							+ "_" + tuple.get(7).toString().replace("\'", ""));	// Description
		};

		return Pair.of(query, transformer);

	}

	/**
	 * @return The query for extracting environment-diff alerts.
	 * @throws ParserException for incorrect query strings.
	 */
	private static Pair<IQuery, Transformer> getEnvironmentQuery() throws ParserException {

		String qs = "";
		qs += "?- Environment(?Version,?File,?Line,?Position,?Length,?StatementID,?Identifier,?Type,?EnvChange)";
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
						tuple.get(5).toString().replace("\'",  ""),			// AST Node ID
						tuple.get(2).toString().replace("\'", ""),			// Line
						tuple.get(3).toString().replace("\'", ""),			// Position
						tuple.get(4).toString().replace("\'", ""),			// Length
						"DIFF",												// Type
						"ENV",												// Subtype
						tuple.get(6).toString().replace("\'", "")
							+ "_" + tuple.get(7).toString().replace("\'", "")
							+ "_" + tuple.get(8).toString().replace("\'", ""));	// Description
		};

		return Pair.of(query, transformer);

	}

	/**
	 * @return The query for extracting control-flow-diff alerts.
	 * @throws ParserException for incorrect query strings.
	 */
	private static Pair<IQuery, Transformer> getControlQuery() throws ParserException {

		String qs = "";
		qs += "?- Control(?Version,?File,?Line,?Position,?Length,?StatementID,?Type,?Change)";
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
						tuple.get(5).toString().replace("\'",  ""),			// AST Node ID
						tuple.get(2).toString().replace("\'", ""),			// Line
						tuple.get(3).toString().replace("\'", ""),			// Position
						tuple.get(4).toString().replace("\'", ""),			// Length
						"DIFF",												// Type
						tuple.get(6).toString().replace("\'", ""),											// Subtype
						tuple.get(7).toString().replace("\'", ""));			// Description
		};

		return Pair.of(query, transformer);

	}

}