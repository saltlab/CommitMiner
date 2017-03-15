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
import ca.ubc.ece.salt.pangor.js.diff.defuse.DefUseCommitAnalysisFactory;
import ca.ubc.ece.salt.pangor.js.diff.view.HTMLMultiDiffViewer;
import ca.ubc.ece.salt.pangor.js.diff.view.HTMLUnixDiffViewer;

public class TestDefUseHTMLView {

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
		ICommitAnalysisFactory commitFactory = new DefUseCommitAnalysisFactory(dataSet);
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
	public void test_tutorial() throws Exception {

		String src = "./test/input/diff/tutorial_old.js";
		String dst = "./test/input/diff/tutorial_new.js";
		String out = "./output/tutorial.html";

		runTest(src, dst, out, false);

	}

	@Test
	public void test_querygenerator_cve20151369() throws Exception {

		String src = "./test/input/vic/sequelize-4827513fe6b9071ef49052d6203e331ba1971755_old.js";
		String dst = "./test/input/vic/sequelize-4827513fe6b9071ef49052d6203e331ba1971755_new.js";
		String out = "./output/sequelize-4827513fe6b9071ef49052d6203e331ba1971755.html";

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

		Pair<IQuery, Transformer> defQuery = getDefQuery();
		queries.put(defQuery.getLeft(), defQuery.getRight());

		Pair<IQuery, Transformer> useQuery = getUseQuery();
		queries.put(useQuery.getLeft(), useQuery.getRight());


		return queries;

	}

	/**
	 * @return The query for extracting DEF alerts.
	 * @throws ParserException for incorrect query strings.
	 */
	private static Pair<IQuery, Transformer> getDefQuery() throws ParserException {

		String qs = "";
		qs += "?- Def(?Version,?Address,?Position,?Length).";

		/* The query that produces the results. */
		Parser parser = new Parser();
		parser.parse(qs);
		IQuery query = parser.getQueries().get(0);

		/* Transforms the query results to a ClassifierFeatureVector. */
		Transformer transformer = (commit, tuple) -> {
				return new ClassifierFeatureVector(commit,
						tuple.get(0).toString().replace("\'", ""),			// Version
						"NA", 												// Class
						"NA",												// AST Node ID
						tuple.get(1).toString().replace("\'", ""),			// Address
						tuple.get(2).toString().replace("\'", ""),			// Position
						tuple.get(3).toString().replace("\'", ""),			// Length
						"POINTS-TO",										// Type
						"DEF",												// Subtype
						"NA");												// Description
		};

		return Pair.of(query, transformer);

	}

	/**
	 * @return The query for extracting USE alerts.
	 * @throws ParserException for incorrect query strings.
	 */
	private static Pair<IQuery, Transformer> getUseQuery() throws ParserException {

		String qs = "";
		qs += "?- Use(?Version,?File,?Address,?Position,?Length).";

		/* The query that produces the results. */
		Parser parser = new Parser();
		parser.parse(qs);
		IQuery query = parser.getQueries().get(0);

		/* Transforms the query results to a ClassifierFeatureVector. */
		Transformer transformer = (commit, tuple) -> {
				return new ClassifierFeatureVector(commit,
						tuple.get(0).toString().replace("\'", ""),			// Version
						tuple.get(1).toString().replace("\'", ""), 												// Class
						"NA",												// AST Node ID
						tuple.get(2).toString().replace("\'", ""),			// Address
						tuple.get(3).toString().replace("\'", ""),			// Position
						tuple.get(4).toString().replace("\'", ""),			// Length
						"POINTS-TO",										// Type
						"USE",												// Subtype
						"NA");												// Description
		};

		return Pair.of(query, transformer);

	}

}