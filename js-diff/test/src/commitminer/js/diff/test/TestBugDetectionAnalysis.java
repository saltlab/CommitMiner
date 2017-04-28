package commitminer.js.diff.test;


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

import commitminer.analysis.Commit;
import commitminer.analysis.CommitAnalysis;
import commitminer.analysis.SourceCodeFileChange;
import commitminer.analysis.Commit.Type;
import commitminer.analysis.factories.ICommitAnalysisFactory;
import commitminer.classify.ClassifierDataSet;
import commitminer.classify.ClassifierFeatureVector;
import commitminer.classify.Transformer;
import commitminer.js.diff.typeerror.TypeCommitAnalysisFactory;

public class TestBugDetectionAnalysis {

	/**
	 * Tests data mining data set construction.
	 * @param args The command line arguments (i.e., old and new file names).
	 * @throws Exception
	 */
	protected void runTest(List<SourceCodeFileChange> sourceFileChanges,
						  List<ClassifierFeatureVector> expected,
						   boolean checkSize) throws Exception {

		Commit commit = getCommit();
		for(SourceCodeFileChange sourceFileChange : sourceFileChanges) {
			commit.addSourceCodeFileChange(sourceFileChange);
		}

		/* Builds the data set with our custom queries. */
		ClassifierDataSet dataSet = new ClassifierDataSet(null,
				new LinkedList<IRule>(), getAlertQueries());

		/* Set up the analysis. */
		ICommitAnalysisFactory commitFactory = new TypeCommitAnalysisFactory(dataSet); //new DiffCommitAnalysisFactory(dataSet);
		CommitAnalysis commitAnalysis = commitFactory.newInstance();

		/* Run the analysis. */
		commitAnalysis.analyze(commit);

        /* Print the data set. */
		dataSet.printDataSet();

        /* Verify the expected feature vectors match the actual feature vectors. */
		List<ClassifierFeatureVector> actual = dataSet.getFeatureVectors();
		if(checkSize) Assert.assertTrue(actual.size() == expected.size());
        for(ClassifierFeatureVector fv : expected) {
        	Assert.assertTrue(contains(actual,fv));
        }
	}

	private static boolean contains(List<ClassifierFeatureVector> fvs, ClassifierFeatureVector test) {
		for(ClassifierFeatureVector fv : fvs) {
			if(fv.commit.equals(test.commit)
					&& fv.version.equals(test.version)
					&& fv.klass.equals(test.klass)
					&& fv.line.equals(test.line)
					&& fv.type.equals(test.type)
					&& fv.subtype.equals(test.subtype)
					&& fv.description.equals(test.description)) {
				return true;
			}
		}
		return false;
	}

	@Test
	public void testTypeError() throws Exception {

		/* The test files. */
		String src = "./test/input/diff/type-error_old.js";
		String dst = "./test/input/diff/type-error_new.js";

		/* Read the source files. */
		List<SourceCodeFileChange> sourceCodeFileChanges = new LinkedList<SourceCodeFileChange>();
		sourceCodeFileChanges.add(getSourceCodeFileChange(src, dst));

		/* Build the expected feature vectors. */
		Commit commit = getCommit();
		List<ClassifierFeatureVector> expected = new LinkedList<ClassifierFeatureVector>();
//		expected.add(new ClassifierFeatureVector(commit, "DESTINATION", "./test/input/diff/binary_new.js", "160", "{3}", "DIFF", "VAL", "c_Change:CHANGED"));
//		expected.add(new ClassifierFeatureVector(commit, "DESTINATION", "./test/input/diff/binary_new.js", "256", "{4}", "DIFF", "VAL", "c_Change:CHANGED"));

		this.runTest(sourceCodeFileChanges, expected, false);

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
	 * @return The Datalog query that generates alerts.
	 * @throws ParserException when iris fails to parse the query string.
	 */
	public static Map<IQuery,Transformer> getAlertQueries() throws ParserException {

		Map<IQuery, Transformer> queries = new HashMap<IQuery, Transformer>();

		Pair<IQuery, Transformer> valueQuery = getTypeErrorQuery();
		queries.put(valueQuery.getLeft(), valueQuery.getRight());

		return queries;

	}

	/**
	 * @return The query for extracting TypeError alerts.
	 * @throws ParserException for incorrect query strings.
	 */
	private static Pair<IQuery, Transformer> getTypeErrorQuery() throws ParserException {

//		String qs = "";
//		qs += "?- Value(?Version,?File,?Line,?StatementID,?Identifier,?ValChange)";
//		qs += ", NOT_EQUAL(?ValChange, 'Change:UNCHANGED')";
//		qs += ", NOT_EQUAL(?ValChange, 'Change:BOTTOM').";

		String qs = "";
//		qs += "?- Type(?Version,?File,?Line,?StatementID,?Identifier,?Type,?Lattice).";
//		qs += ", NOT_EQUAL(?ValChange, 'Change:UNCHANGED')";
//		qs += ", NOT_EQUAL(?ValChange, 'Change:BOTTOM').";

		qs += "?- Type('SOURCE',?File,?LineA,?StatementID,?Identifier,'undef','BOTTOM')";
		qs += ", Type('DESTINATION',?File,?LineB,?StatementID,?Identifier,'undef','TOP').";

		/* The query that produces the results. */
		Parser parser = new Parser();
		parser.parse(qs);
		IQuery query = parser.getQueries().get(0);

		/* Transforms the query results to a ClassifierFeatureVector. */
		Transformer transformer = (commit, tuple) -> {
				return new ClassifierFeatureVector(commit,
						"DESTINATION",										// Version
						tuple.get(0).toString().replace("\'", ""), 			// Class
						tuple.get(1).toString().replace("\'",  ""),			// AST Node ID
						tuple.get(4).toString().replace("\'", ""),			// Line
						"DIFF",												// Type
						"TypeError",										// Subtype
						tuple.get(2).toString().replace("\'", "")
							+ " may be undefined"
							+ " at source line "
							+ tuple.get(3).toString().replace("\'",  "")
							+ " and destination line + "
							+ tuple.get(4).toString().replace("\'", ""));	// Description
		};

		return Pair.of(query, transformer);

	}

}