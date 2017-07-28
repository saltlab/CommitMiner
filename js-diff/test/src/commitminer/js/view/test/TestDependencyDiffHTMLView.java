package commitminer.js.view.test;


import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.deri.iris.api.basics.IQuery;
import org.deri.iris.compiler.Parser;
import org.deri.iris.compiler.ParserException;
import org.junit.Test;

import commitminer.analysis.Commit;
import commitminer.analysis.CommitAnalysis;
import commitminer.analysis.SourceCodeFileChange;
import commitminer.analysis.Commit.Type;
import commitminer.analysis.factories.ICommitAnalysisFactory;
import commitminer.analysis.options.Options;
import commitminer.classify.ClassifierFeatureVector;
import commitminer.classify.Transformer;
import commitminer.js.annotation.AnnotationDataSet;
import commitminer.js.annotation.AnnotationFactBase;
import commitminer.js.diff.DependencyCommitAnalysisFactory;
import commitminer.js.diff.view.HTMLMultiDiffViewer;
import commitminer.js.diff.view.HTMLUnixDiffViewer;

public class TestDependencyDiffHTMLView {

	/**
	 * Tests data mining data set construction.
	 * @param args The command line arguments (i.e., old and new file names).
	 * @throws Exception
	 */
	protected void runTest(
			String src, String dst, String out) throws Exception {
		
		/* Set the options for this run. */
		Options.createInstance(Options.DiffMethod.MEYERS, Options.ChangeImpact.DEPENDENCIES);

		/* Read the source files. */
		String srcCode = new String(Files.readAllBytes(Paths.get(src)));
		String dstCode = new String(Files.readAllBytes(Paths.get(dst)));

		/* Read the source files. */
		SourceCodeFileChange sourceCodeFileChange = getSourceCodeFileChange(src, dst);

		/* Build the dummy commit. */
		Commit commit = getCommit();
		commit.addSourceCodeFileChange(getSourceCodeFileChange(src, dst));

		/* Builds the data set with our custom queries. */
		AnnotationDataSet dataSet = new AnnotationDataSet( AnnotationFactBase.getInstance(sourceCodeFileChange));

		/* Set up the analysis. */
		ICommitAnalysisFactory commitFactory = new DependencyCommitAnalysisFactory(dataSet);
		CommitAnalysis commitAnalysis = commitFactory.newInstance();

		/* Run the analysis. */
		commitAnalysis.analyze(commit);

        /* Print the data set. */
		dataSet.printDataSet();

		/* Only annotate the destination file. The source file isn't especially useful. */
		String annotatedDst = HTMLMultiDiffViewer.annotate(dstCode, dataSet.getAnnotationFactBase());

		/* Combine the annotated file with the UnixDiff. */
		String annotatedCombined = HTMLUnixDiffViewer.annotate(srcCode, dstCode, annotatedDst);
		Files.write(Paths.get(out), annotatedCombined.getBytes(), StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);

	}

	@Test
	public void testEnv() throws Exception {

		String src = "./test/input/diff/env_old.js";
		String dst = "./test/input/diff/env_new.js";
		String out = "./web/env_dep.html";

		runTest(src, dst, out);

	}

	@Test
	public void testListing3() throws Exception {

		String src = "./test/input/diff/listing3_old.js";
		String dst = "./test/input/diff/listing3_new.js";
		String out = "./web/listing3.html";

		runTest(src, dst, out);

	}

	@Test
	public void testCreate() throws Exception {

		String src = "./test/input/diff/create_old.js";
		String dst = "./test/input/diff/create_new.js";
		String out = "./output/create.html";

		runTest(src, dst, out);

	}

	@Test
	public void testTutorial() throws Exception {

		String src = "./test/input/diff/tutorial_old.js";
		String dst = "./test/input/diff/tutorial_new.js";
		String out = "./output/tutorial.html";

		runTest(src, dst, out);

	}

	@Test
	public void testHelpSearch() throws Exception {

		String src = "./test/input/diff/help-search_old.js";
		String dst = "./test/input/diff/help-search_new.js";
		String out = "./output/help-search.html";

		runTest(src, dst, out);
	}

	@Test
	public void testSails() throws Exception {

		String src = "./test/input/eval/sails-074841dfa62f23a66113aa56f710e874149e35bf_old.js";
		String dst = "./test/input/eval/sails-074841dfa62f23a66113aa56f710e874149e35bf_new.js";
		String out = "./output/sails.html";

		runTest(src, dst, out);

	}

	@Test
	public void testBower() throws Exception {

		String src = "./test/input/eval/bower-fd472403c1f9992b57e15b4ff745732677b64ee1_old.js";
		String dst = "./test/input/eval/bower-fd472403c1f9992b57e15b4ff745732677b64ee1_new.js";
		String out = "./output/bower.html";

		runTest(src, dst, out);

	}

	@Test
	public void testConnect() throws Exception {

		String src = "./test/input/eval/connect-08337a38445b16f84192b40b74458bcea36f9a32_old.js";
		String dst = "./test/input/eval/connect-08337a38445b16f84192b40b74458bcea36f9a32_new.js";
		String out = "./output/connect.html";

		runTest(src, dst, out);

	}
	
	@Test
	public void testPM2() throws Exception {
	
		String src = "./test/input/diff/tst_old.js";
		String dst = "./test/input/diff/tst_new.js";
		String out = "./web/pm2_dep.html";

		runTest(src, dst, out);
		
	}

	/* USER STUDY CANDIDATE. */
	@Test
	public void testNPM() throws Exception {
		String src = "./test/input/user_study/npm-af94ccd140a72c4b7625e6dde6d76ab586a8134f_old.js";
		String dst = "./test/input/user_study/npm-af94ccd140a72c4b7625e6dde6d76ab586a8134f_new.js";
		String out = "./web/npm-dep.html";
		runTest(src, dst, out);
	}

	/* USER STUDY CANDIDATE. */
	@Test
	public void testMongoDB() throws Exception {
		String src = "./test/input/user_study/mongodb-a243fbeb788d0d6191800decb0629be4b9f4dd63_old.js";
		String dst = "./test/input/user_study/mongodb-a243fbeb788d0d6191800decb0629be4b9f4dd63_new.js";
		String out = "./web/mongo-dep.html";
		runTest(src, dst, out);
	}

	/* USER STUDY CANDIDATE. */
	@Test
	public void testSubstack() throws Exception {
		String src = "./test/input/user_study/substack-83f9c24dc292aee00db4613208660502130be739_old.js";
		String dst = "./test/input/user_study/substack-83f9c24dc292aee00db4613208660502130be739_new.js";
		String out = "./web/substack-dep.html";
		runTest(src, dst, out);
	}

	/* USER STUDY CANDIDATE. */
	@Test
	public void testMediacenterJS() throws Exception {
		String src = "./test/input/user_study/mediacenterjs-f8f1713fc8a15d652c571ee0e5176889dcea095d_old.js";
		String dst = "./test/input/user_study/mediacenterjs-f8f1713fc8a15d652c571ee0e5176889dcea095d_new.js";
		String out = "./web/mediacenterjs-dep.html";
		runTest(src, dst, out);
	}

	/* USER STUDY CANDIDATE. */
	@Test
	public void testSailsB() throws Exception {
		String src = "./test/input/user_study/sails-10cd52fe0e8cef3c25fde9130fac1e39a76a2e6f_old.js";
		String dst = "./test/input/user_study/sails-10cd52fe0e8cef3c25fde9130fac1e39a76a2e6f_new.js";
		String out = "./web/sailsb-dep.html";
		runTest(src, dst, out);
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