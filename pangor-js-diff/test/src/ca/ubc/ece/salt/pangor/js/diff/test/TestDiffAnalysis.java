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
	protected void runTest(List<SourceCodeFileChange> sourceFileChanges,
						  List<ClassifierFeatureVector> expected,
						   boolean checkSize) throws Exception {

		Commit commit = getCommit();
		for(SourceCodeFileChange sourceFileChange : sourceFileChanges) {
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
	public void testApp() throws Exception {

		/* The test files. */
		String src = "./test/input/diff/app_old.js";
		String dst = "./test/input/diff/app_new.js";

		/* Read the source files. */
		List<SourceCodeFileChange> sourceCodeFileChanges = new LinkedList<SourceCodeFileChange>();
		sourceCodeFileChanges.add(getSourceCodeFileChange(src, dst));

		/* Build the expected feature vectors. */
		Commit commit = getCommit();
		List<ClassifierFeatureVector> expected = new LinkedList<ClassifierFeatureVector>();

		this.runTest(sourceCodeFileChanges, expected, false);

	}

	@Test
	public void testCreate() throws Exception {

		/* The test files. */
		String src = "./test/input/diff/create_old.js";
		String dst = "./test/input/diff/create_new.js";

		/* Read the source files. */
		List<SourceCodeFileChange> sourceCodeFileChanges = new LinkedList<SourceCodeFileChange>();
		sourceCodeFileChanges.add(getSourceCodeFileChange(src, dst));

		/* Build the expected feature vectors. */
		Commit commit = getCommit();
		List<ClassifierFeatureVector> expected = new LinkedList<ClassifierFeatureVector>();
		expected.add(new ClassifierFeatureVector(commit, "DESTINATION", "./test/input/diff/create_new.js", "NA", "{1}", "DIFF", "VAL", "a_Change:TOP"));
		expected.add(new ClassifierFeatureVector(commit, "DESTINATION", "./test/input/diff/create_new.js", "NA", "{2}", "DIFF", "VAL", "b_Change:CHANGED"));
		expected.add(new ClassifierFeatureVector(commit, "DESTINATION", "./test/input/diff/create_new.js", "NA", "{3}", "DIFF", "VAL", "c_Change:CHANGED"));
		expected.add(new ClassifierFeatureVector(commit, "DESTINATION", "./test/input/diff/create_new.js", "NA", "{4}", "DIFF", "VAL", "d_Change:TOP"));
		expected.add(new ClassifierFeatureVector(commit, "DESTINATION", "./test/input/diff/create_new.js", "NA", "{5}", "DIFF", "VAL", "e_Change:TOP"));

		expected.add(new ClassifierFeatureVector(commit, "DESTINATION", "./test/input/diff/create_new.js", "NA", "{18}", "DIFF", "CONTROL", "Change:CHANGED"));
		expected.add(new ClassifierFeatureVector(commit, "DESTINATION", "./test/input/diff/create_new.js", "NA", "{19}", "DIFF", "CONTROL", "Change:CHANGED"));

		this.runTest(sourceCodeFileChanges, expected, false);

	}

	@Test
	public void testUnOp() throws Exception {

		/* The test files. */
		String src = "./test/input/diff/unary_old.js";
		String dst = "./test/input/diff/unary_new.js";

		/* Read the source files. */
		List<SourceCodeFileChange> sourceCodeFileChanges = new LinkedList<SourceCodeFileChange>();
		sourceCodeFileChanges.add(getSourceCodeFileChange(src, dst));

		/* Build the expected feature vectors. */
		Commit commit = getCommit();
		List<ClassifierFeatureVector> expected = new LinkedList<ClassifierFeatureVector>();
		expected.add(new ClassifierFeatureVector(commit, "DESTINATION", "./test/input/diff/unary_new.js", "MethodNA", "{3}", "DIFF", "VAL", "c_Change:CHANGED"));
		expected.add(new ClassifierFeatureVector(commit, "DESTINATION", "./test/input/diff/unary_new.js", "MethodNA", "{4}", "DIFF", "VAL", "c_Change:CHANGED"));

		this.runTest(sourceCodeFileChanges, expected, false);

	}

	@Test
	public void testBinOp() throws Exception {

		/* The test files. */
		String src = "./test/input/diff/binary_old.js";
		String dst = "./test/input/diff/binary_new.js";

		/* Read the source files. */
		List<SourceCodeFileChange> sourceCodeFileChanges = new LinkedList<SourceCodeFileChange>();
		sourceCodeFileChanges.add(getSourceCodeFileChange(src, dst));

		/* Build the expected feature vectors. */
		Commit commit = getCommit();
		List<ClassifierFeatureVector> expected = new LinkedList<ClassifierFeatureVector>();
		expected.add(new ClassifierFeatureVector(commit, "DESTINATION", "./test/input/diff/binary_new.js", "160", "{3}", "DIFF", "VAL", "c_Change:CHANGED"));
		expected.add(new ClassifierFeatureVector(commit, "DESTINATION", "./test/input/diff/binary_new.js", "256", "{4}", "DIFF", "VAL", "c_Change:CHANGED"));

		this.runTest(sourceCodeFileChanges, expected, false);

	}

	@Test
	public void testDyn() throws Exception {

		/* The test files. */
		String src = "./test/input/diff/dyn_old.js";
		String dst = "./test/input/diff/dyn_new.js";

		/* Read the source files. */
		List<SourceCodeFileChange> sourceCodeFileChanges = new LinkedList<SourceCodeFileChange>();
		sourceCodeFileChanges.add(getSourceCodeFileChange(src, dst));

		/* Build the expected feature vectors. */
		Commit commit = getCommit();
		List<ClassifierFeatureVector> expected = new LinkedList<ClassifierFeatureVector>();

		/* The value-diff alerts. */
		expected.add(new ClassifierFeatureVector(commit, "DESTINATION", "./test/input/diff/dyn_new.js", "MethodNA", "{14}", "DIFF", "VAL", "cb_Change:CHANGED"));
		expected.add(new ClassifierFeatureVector(commit, "DESTINATION", "./test/input/diff/dyn_new.js", "MethodNA", "{16}", "DIFF", "VAL", "cb_Change:CHANGED"));
		expected.add(new ClassifierFeatureVector(commit, "DESTINATION", "./test/input/diff/dyn_new.js", "MethodNA", "{27}", "DIFF", "VAL", "semver_Change:TOP"));
		expected.add(new ClassifierFeatureVector(commit, "DESTINATION", "./test/input/diff/dyn_new.js", "MethodNA", "{33}", "DIFF", "VAL", "semver_Change:TOP"));
		expected.add(new ClassifierFeatureVector(commit, "DESTINATION", "./test/input/diff/dyn_new.js", "MethodNA", "{32}", "DIFF", "VAL", "err_Change:CHANGED"));
		expected.add(new ClassifierFeatureVector(commit, "DESTINATION", "./test/input/diff/dyn_new.js", "MethodNA", "{32}", "DIFF", "VAL", "data_Change:CHANGED"));
		expected.add(new ClassifierFeatureVector(commit, "DESTINATION", "./test/input/diff/dyn_new.js", "MethodNA", "{33}", "DIFF", "VAL", "err_Change:CHANGED"));
		expected.add(new ClassifierFeatureVector(commit, "DESTINATION", "./test/input/diff/dyn_new.js", "MethodNA", "{33}", "DIFF", "VAL", "data_Change:CHANGED"));
		expected.add(new ClassifierFeatureVector(commit, "DESTINATION", "./test/input/diff/dyn_new.js", "MethodNA", "{36}", "DIFF", "VAL", "PM2_SILENT_Change:CHANGED"));
		expected.add(new ClassifierFeatureVector(commit, "DESTINATION", "./test/input/diff/dyn_new.js", "MethodNA", "{43}", "DIFF", "VAL", "PM2_SILENT_Change:CHANGED"));

//		/* The environment-diff alerts. */
//		expected.add(new ClassifierFeatureVector(commit, "DESTINATION", "./test/input/diff/dyn_new.js", "MethodNA", "{27}", "DIFF", "ENV", "semver_ENV_Change:CHANGED"));
//		expected.add(new ClassifierFeatureVector(commit, "DESTINATION", "./test/input/diff/dyn_new.js", "MethodNA", "{33}", "DIFF", "ENV", "semver_ENV_Change:CHANGED"));
//		expected.add(new ClassifierFeatureVector(commit, "DESTINATION", "./test/input/diff/dyn_new.js", "MethodNA", "{32}", "DIFF", "ENV", "data_ENV_Change:CHANGED"));
//		expected.add(new ClassifierFeatureVector(commit, "DESTINATION", "./test/input/diff/dyn_new.js", "MethodNA", "{32}", "DIFF", "ENV", "err_ENV_Change:CHANGED"));
//		expected.add(new ClassifierFeatureVector(commit, "DESTINATION", "./test/input/diff/dyn_new.js", "MethodNA", "{33}", "DIFF", "ENV", "data_ENV_Change:CHANGED"));
//		expected.add(new ClassifierFeatureVector(commit, "DESTINATION", "./test/input/diff/dyn_new.js", "MethodNA", "{33}", "DIFF", "ENV", "err_ENV_Change:CHANGED"));
//
//		/* The control-flow-diff alerts. */
//		expected.add(new ClassifierFeatureVector(commit, "DESTINATION", "./test/input/diff/dyn_new.js", "MethodNA", "{14}", "DIFF", "CONTROL", "Change:CHANGED"));
//		expected.add(new ClassifierFeatureVector(commit, "DESTINATION", "./test/input/diff/dyn_new.js", "MethodNA", "{15}", "DIFF", "CONTROL", "Change:CHANGED"));
//		expected.add(new ClassifierFeatureVector(commit, "DESTINATION", "./test/input/diff/dyn_new.js", "MethodNA", "{16}", "DIFF", "CONTROL", "Change:CHANGED"));
//		expected.add(new ClassifierFeatureVector(commit, "DESTINATION", "./test/input/diff/dyn_new.js", "MethodNA", "{32}", "DIFF", "CONTROL", "Change:CHANGED"));
//		expected.add(new ClassifierFeatureVector(commit, "DESTINATION", "./test/input/diff/dyn_new.js", "MethodNA", "{33}", "DIFF", "CONTROL", "Change:CHANGED"));
//		expected.add(new ClassifierFeatureVector(commit, "DESTINATION", "./test/input/diff/dyn_new.js", "MethodNA", "{36}", "DIFF", "CONTROL", "Change:CHANGED"));
//		expected.add(new ClassifierFeatureVector(commit, "DESTINATION", "./test/input/diff/dyn_new.js", "MethodNA", "{37}", "DIFF", "CONTROL", "Change:CHANGED"));
//		expected.add(new ClassifierFeatureVector(commit, "DESTINATION", "./test/input/diff/dyn_new.js", "MethodNA", "{40}", "DIFF", "CONTROL", "Change:CHANGED"));
//		expected.add(new ClassifierFeatureVector(commit, "DESTINATION", "./test/input/diff/dyn_new.js", "MethodNA", "{41}", "DIFF", "CONTROL", "Change:CHANGED"));
//		expected.add(new ClassifierFeatureVector(commit, "DESTINATION", "./test/input/diff/dyn_new.js", "MethodNA", "{42}", "DIFF", "CONTROL", "Change:CHANGED"));
//		expected.add(new ClassifierFeatureVector(commit, "DESTINATION", "./test/input/diff/dyn_new.js", "MethodNA", "{43}", "DIFF", "CONTROL", "Change:CHANGED"));
//		expected.add(new ClassifierFeatureVector(commit, "DESTINATION", "./test/input/diff/dyn_new.js", "MethodNA", "{45}", "DIFF", "CONTROL", "Change:CHANGED"));
//		expected.add(new ClassifierFeatureVector(commit, "DESTINATION", "./test/input/diff/dyn_new.js", "MethodNA", "{46}", "DIFF", "CONTROL", "Change:CHANGED"));
//		expected.add(new ClassifierFeatureVector(commit, "DESTINATION", "./test/input/diff/dyn_new.js", "MethodNA", "{47}", "DIFF", "CONTROL", "Change:CHANGED"));
//		expected.add(new ClassifierFeatureVector(commit, "DESTINATION", "./test/input/diff/dyn_new.js", "MethodNA", "{48}", "DIFF", "CONTROL", "Change:CHANGED"));
//		expected.add(new ClassifierFeatureVector(commit, "DESTINATION", "./test/input/diff/dyn_new.js", "MethodNA", "{49}", "DIFF", "CONTROL", "Change:CHANGED"));

//		/* The ast-diff alerts. */
//		expected.add(new ClassifierFeatureVector(commit, "DESTINATION", "./test/input/diff/dyn_new.js", "MethodNA", "{27}", "DIFF", "AST", "INSERTED"));
//		expected.add(new ClassifierFeatureVector(commit, "DESTINATION", "./test/input/diff/dyn_new.js", "MethodNA", "{32}", "DIFF", "AST", "INSERTED"));
//		expected.add(new ClassifierFeatureVector(commit, "DESTINATION", "./test/input/diff/dyn_new.js", "MethodNA", "{33}", "DIFF", "AST", "INSERTED"));
//		expected.add(new ClassifierFeatureVector(commit, "DESTINATION", "./test/input/diff/dyn_new.js", "MethodNA", "{36}", "DIFF", "AST", "INSERTED"));
//		expected.add(new ClassifierFeatureVector(commit, "DESTINATION", "./test/input/diff/dyn_new.js", "MethodNA", "{37}", "DIFF", "AST", "INSERTED"));
//		expected.add(new ClassifierFeatureVector(commit, "DESTINATION", "./test/input/diff/dyn_new.js", "MethodNA", "{40}", "DIFF", "AST", "MOVED"));
//		expected.add(new ClassifierFeatureVector(commit, "DESTINATION", "./test/input/diff/dyn_new.js", "MethodNA", "{41}", "DIFF", "AST", "MOVED"));
//		expected.add(new ClassifierFeatureVector(commit, "DESTINATION", "./test/input/diff/dyn_new.js", "MethodNA", "{42}", "DIFF", "AST", "MOVED"));
//		expected.add(new ClassifierFeatureVector(commit, "DESTINATION", "./test/input/diff/dyn_new.js", "MethodNA", "{43}", "DIFF", "AST", "INSERTED"));
//		expected.add(new ClassifierFeatureVector(commit, "DESTINATION", "./test/input/diff/dyn_new.js", "MethodNA", "{45}", "DIFF", "AST", "MOVED"));
//		expected.add(new ClassifierFeatureVector(commit, "DESTINATION", "./test/input/diff/dyn_new.js", "MethodNA", "{46}", "DIFF", "AST", "MOVED"));
//		expected.add(new ClassifierFeatureVector(commit, "DESTINATION", "./test/input/diff/dyn_new.js", "MethodNA", "{47}", "DIFF", "AST", "MOVED"));
//		expected.add(new ClassifierFeatureVector(commit, "DESTINATION", "./test/input/diff/dyn_new.js", "MethodNA", "{48}", "DIFF", "AST", "MOVED"));
//		expected.add(new ClassifierFeatureVector(commit, "DESTINATION", "./test/input/diff/dyn_new.js", "MethodNA", "{49}", "DIFF", "AST", "MOVED"));
//
//		expected.add(new ClassifierFeatureVector(commit, "SOURCE", "./test/input/diff/dyn_new.js", "MethodNA", "{30}", "DIFF", "AST", "MOVED"));
//		expected.add(new ClassifierFeatureVector(commit, "SOURCE", "./test/input/diff/dyn_new.js", "MethodNA", "{31}", "DIFF", "AST", "MOVED"));
//		expected.add(new ClassifierFeatureVector(commit, "SOURCE", "./test/input/diff/dyn_new.js", "MethodNA", "{32}", "DIFF", "AST", "MOVED"));
//		expected.add(new ClassifierFeatureVector(commit, "SOURCE", "./test/input/diff/dyn_new.js", "MethodNA", "{34}", "DIFF", "AST", "MOVED"));
//		expected.add(new ClassifierFeatureVector(commit, "SOURCE", "./test/input/diff/dyn_new.js", "MethodNA", "{35}", "DIFF", "AST", "MOVED"));
//		expected.add(new ClassifierFeatureVector(commit, "SOURCE", "./test/input/diff/dyn_new.js", "MethodNA", "{36}", "DIFF", "AST", "MOVED"));
//		expected.add(new ClassifierFeatureVector(commit, "SOURCE", "./test/input/diff/dyn_new.js", "MethodNA", "{37}", "DIFF", "AST", "MOVED"));
//		expected.add(new ClassifierFeatureVector(commit, "SOURCE", "./test/input/diff/dyn_new.js", "MethodNA", "{38}", "DIFF", "AST", "MOVED"));
//
//		/* The line-diff alerts. */
//		expected.add(new ClassifierFeatureVector(commit, "DESTINATION", "./test/input/diff/dyn_new.js", "MethodNA", "{27}", "DIFF", "LINE", "INSERTED"));
//		expected.add(new ClassifierFeatureVector(commit, "DESTINATION", "./test/input/diff/dyn_new.js", "MethodNA", "{31}", "DIFF", "LINE", "INSERTED"));
//		expected.add(new ClassifierFeatureVector(commit, "DESTINATION", "./test/input/diff/dyn_new.js", "MethodNA", "{32}", "DIFF", "LINE", "INSERTED"));
//		expected.add(new ClassifierFeatureVector(commit, "DESTINATION", "./test/input/diff/dyn_new.js", "MethodNA", "{33}", "DIFF", "LINE", "INSERTED"));
//		expected.add(new ClassifierFeatureVector(commit, "DESTINATION", "./test/input/diff/dyn_new.js", "MethodNA", "{34}", "DIFF", "LINE", "INSERTED"));
//		expected.add(new ClassifierFeatureVector(commit, "DESTINATION", "./test/input/diff/dyn_new.js", "MethodNA", "{35}", "DIFF", "LINE", "INSERTED"));
//		expected.add(new ClassifierFeatureVector(commit, "DESTINATION", "./test/input/diff/dyn_new.js", "MethodNA", "{36}", "DIFF", "LINE", "INSERTED"));
//		expected.add(new ClassifierFeatureVector(commit, "DESTINATION", "./test/input/diff/dyn_new.js", "MethodNA", "{37}", "DIFF", "LINE", "INSERTED"));
//		expected.add(new ClassifierFeatureVector(commit, "DESTINATION", "./test/input/diff/dyn_new.js", "MethodNA", "{38}", "DIFF", "LINE", "INSERTED"));
//		expected.add(new ClassifierFeatureVector(commit, "DESTINATION", "./test/input/diff/dyn_new.js", "MethodNA", "{39}", "DIFF", "LINE", "INSERTED"));
//		expected.add(new ClassifierFeatureVector(commit, "DESTINATION", "./test/input/diff/dyn_new.js", "MethodNA", "{40}", "DIFF", "LINE", "INSERTED"));
//		expected.add(new ClassifierFeatureVector(commit, "DESTINATION", "./test/input/diff/dyn_new.js", "MethodNA", "{41}", "DIFF", "LINE", "INSERTED"));
//		expected.add(new ClassifierFeatureVector(commit, "DESTINATION", "./test/input/diff/dyn_new.js", "MethodNA", "{42}", "DIFF", "LINE", "INSERTED"));
//		expected.add(new ClassifierFeatureVector(commit, "DESTINATION", "./test/input/diff/dyn_new.js", "MethodNA", "{43}", "DIFF", "LINE", "INSERTED"));
//		expected.add(new ClassifierFeatureVector(commit, "DESTINATION", "./test/input/diff/dyn_new.js", "MethodNA", "{45}", "DIFF", "LINE", "INSERTED"));
//		expected.add(new ClassifierFeatureVector(commit, "DESTINATION", "./test/input/diff/dyn_new.js", "MethodNA", "{46}", "DIFF", "LINE", "INSERTED"));
//		expected.add(new ClassifierFeatureVector(commit, "DESTINATION", "./test/input/diff/dyn_new.js", "MethodNA", "{47}", "DIFF", "LINE", "INSERTED"));
//		expected.add(new ClassifierFeatureVector(commit, "DESTINATION", "./test/input/diff/dyn_new.js", "MethodNA", "{48}", "DIFF", "LINE", "INSERTED"));
//		expected.add(new ClassifierFeatureVector(commit, "DESTINATION", "./test/input/diff/dyn_new.js", "MethodNA", "{49}", "DIFF", "LINE", "INSERTED"));
//		expected.add(new ClassifierFeatureVector(commit, "DESTINATION", "./test/input/diff/dyn_new.js", "MethodNA", "{50}", "DIFF", "LINE", "INSERTED"));
//		expected.add(new ClassifierFeatureVector(commit, "DESTINATION", "./test/input/diff/dyn_new.js", "MethodNA", "{54}", "DIFF", "LINE", "INSERTED"));
//
//		expected.add(new ClassifierFeatureVector(commit, "SOURCE", "./test/input/diff/dyn_new.js", "MethodNA", "{30}", "DIFF", "LINE", "REMOVED"));
//		expected.add(new ClassifierFeatureVector(commit, "SOURCE", "./test/input/diff/dyn_new.js", "MethodNA", "{31}", "DIFF", "LINE", "REMOVED"));
//		expected.add(new ClassifierFeatureVector(commit, "SOURCE", "./test/input/diff/dyn_new.js", "MethodNA", "{32}", "DIFF", "LINE", "REMOVED"));
//		expected.add(new ClassifierFeatureVector(commit, "SOURCE", "./test/input/diff/dyn_new.js", "MethodNA", "{34}", "DIFF", "LINE", "REMOVED"));
//		expected.add(new ClassifierFeatureVector(commit, "SOURCE", "./test/input/diff/dyn_new.js", "MethodNA", "{35}", "DIFF", "LINE", "REMOVED"));
//		expected.add(new ClassifierFeatureVector(commit, "SOURCE", "./test/input/diff/dyn_new.js", "MethodNA", "{36}", "DIFF", "LINE", "REMOVED"));
//		expected.add(new ClassifierFeatureVector(commit, "SOURCE", "./test/input/diff/dyn_new.js", "MethodNA", "{37}", "DIFF", "LINE", "REMOVED"));
//		expected.add(new ClassifierFeatureVector(commit, "SOURCE", "./test/input/diff/dyn_new.js", "MethodNA", "{38}", "DIFF", "LINE", "REMOVED"));
//
//		/* The line-total alerts. */
//		expected.add(new ClassifierFeatureVector(commit, "SOURCE", "./test/input/diff/dyn_new.js", "MethodNA", "NA", "DIFF", "TOTAL_LINES", "43"));
//		expected.add(new ClassifierFeatureVector(commit, "DESTINATION", "./test/input/diff/dyn_new.js", "MethodNA", "NA", "DIFF", "TOTAL_LINES", "56"));

		this.runTest(sourceCodeFileChanges, expected, false);
	}

	@Test
	public void testPM2() throws Exception {

		/* The test files. */
		String src = "./test/input/diff/pm2_old.js";
		String dst = "./test/input/diff/pm2_new.js";

		/* Read the source files. */
		List<SourceCodeFileChange> sourceCodeFileChanges = new LinkedList<SourceCodeFileChange>();
		sourceCodeFileChanges.add(getSourceCodeFileChange(src, dst));

		/* Build the expected feature vectors. */
		Commit commit = getCommit();
		List<ClassifierFeatureVector> expected = new LinkedList<ClassifierFeatureVector>();
		expected.add(new ClassifierFeatureVector(commit, "DESTINATION", "./test/input/diff/pm2_new.js", "MethodNA", "{480}", "DIFF", "VAL", "cb_Change:CHANGED"));
		expected.add(new ClassifierFeatureVector(commit, "DESTINATION", "./test/input/diff/pm2_new.js", "MethodNA", "{482}", "DIFF", "VAL", "cb_Change:CHANGED"));
		expected.add(new ClassifierFeatureVector(commit, "DESTINATION", "./test/input/diff/pm2_new.js", "MethodNA", "{1662}", "DIFF", "VAL", "semver_Change:TOP"));
		expected.add(new ClassifierFeatureVector(commit, "DESTINATION", "./test/input/diff/pm2_new.js", "MethodNA", "{1668}", "DIFF", "VAL", "semver_Change:TOP"));
		expected.add(new ClassifierFeatureVector(commit, "DESTINATION", "./test/input/diff/pm2_new.js", "MethodNA", "{1667}", "DIFF", "VAL", "err_Change:CHANGED"));
		expected.add(new ClassifierFeatureVector(commit, "DESTINATION", "./test/input/diff/pm2_new.js", "MethodNA", "{1667}", "DIFF", "VAL", "data_Change:CHANGED"));
		expected.add(new ClassifierFeatureVector(commit, "DESTINATION", "./test/input/diff/pm2_new.js", "MethodNA", "{1668}", "DIFF", "VAL", "err_Change:CHANGED"));
		expected.add(new ClassifierFeatureVector(commit, "DESTINATION", "./test/input/diff/pm2_new.js", "MethodNA", "{1668}", "DIFF", "VAL", "data_Change:CHANGED"));
		expected.add(new ClassifierFeatureVector(commit, "DESTINATION", "./test/input/diff/pm2_new.js", "MethodNA", "{1671}", "DIFF", "VAL", "PM2_SILENT_Change:CHANGED"));
		expected.add(new ClassifierFeatureVector(commit, "DESTINATION", "./test/input/diff/pm2_new.js", "MethodNA", "{1678}", "DIFF", "VAL", "PM2_SILENT_Change:CHANGED"));

		/* The environment-diff alerts. */
		expected.add(new ClassifierFeatureVector(commit, "DESTINATION", "./test/input/diff/pm2_new.js", "MethodNA", "{1662}", "DIFF", "ENV", "semver_ENV_Change:CHANGED"));
		expected.add(new ClassifierFeatureVector(commit, "DESTINATION", "./test/input/diff/pm2_new.js", "MethodNA", "{1668}", "DIFF", "ENV", "semver_ENV_Change:CHANGED"));
		expected.add(new ClassifierFeatureVector(commit, "DESTINATION", "./test/input/diff/pm2_new.js", "MethodNA", "{1667}", "DIFF", "ENV", "data_ENV_Change:CHANGED"));
		expected.add(new ClassifierFeatureVector(commit, "DESTINATION", "./test/input/diff/pm2_new.js", "MethodNA", "{1667}", "DIFF", "ENV", "err_ENV_Change:CHANGED"));
		expected.add(new ClassifierFeatureVector(commit, "DESTINATION", "./test/input/diff/pm2_new.js", "MethodNA", "{1668}", "DIFF", "ENV", "data_ENV_Change:CHANGED"));
		expected.add(new ClassifierFeatureVector(commit, "DESTINATION", "./test/input/diff/pm2_new.js", "MethodNA", "{1668}", "DIFF", "ENV", "err_ENV_Change:CHANGED"));

		/* The control-flow-diff alerts. */
		expected.add(new ClassifierFeatureVector(commit, "DESTINATION", "./test/input/diff/pm2_new.js", "MethodNA", "{480}", "DIFF", "CONTROL", "Change:CHANGED"));
		expected.add(new ClassifierFeatureVector(commit, "DESTINATION", "./test/input/diff/pm2_new.js", "MethodNA", "{481}", "DIFF", "CONTROL", "Change:CHANGED"));
		expected.add(new ClassifierFeatureVector(commit, "DESTINATION", "./test/input/diff/pm2_new.js", "MethodNA", "{482}", "DIFF", "CONTROL", "Change:CHANGED"));
		expected.add(new ClassifierFeatureVector(commit, "DESTINATION", "./test/input/diff/pm2_new.js", "MethodNA", "{1667}", "DIFF", "CONTROL", "Change:CHANGED"));
		expected.add(new ClassifierFeatureVector(commit, "DESTINATION", "./test/input/diff/pm2_new.js", "MethodNA", "{1668}", "DIFF", "CONTROL", "Change:CHANGED"));
		expected.add(new ClassifierFeatureVector(commit, "DESTINATION", "./test/input/diff/pm2_new.js", "MethodNA", "{1671}", "DIFF", "CONTROL", "Change:CHANGED"));
		expected.add(new ClassifierFeatureVector(commit, "DESTINATION", "./test/input/diff/pm2_new.js", "MethodNA", "{1672}", "DIFF", "CONTROL", "Change:CHANGED"));
		expected.add(new ClassifierFeatureVector(commit, "DESTINATION", "./test/input/diff/pm2_new.js", "MethodNA", "{1675}", "DIFF", "CONTROL", "Change:CHANGED"));
		expected.add(new ClassifierFeatureVector(commit, "DESTINATION", "./test/input/diff/pm2_new.js", "MethodNA", "{1676}", "DIFF", "CONTROL", "Change:CHANGED"));
		expected.add(new ClassifierFeatureVector(commit, "DESTINATION", "./test/input/diff/pm2_new.js", "MethodNA", "{1677}", "DIFF", "CONTROL", "Change:CHANGED"));
		expected.add(new ClassifierFeatureVector(commit, "DESTINATION", "./test/input/diff/pm2_new.js", "MethodNA", "{1678}", "DIFF", "CONTROL", "Change:CHANGED"));
		expected.add(new ClassifierFeatureVector(commit, "DESTINATION", "./test/input/diff/pm2_new.js", "MethodNA", "{1680}", "DIFF", "CONTROL", "Change:CHANGED"));
		expected.add(new ClassifierFeatureVector(commit, "DESTINATION", "./test/input/diff/pm2_new.js", "MethodNA", "{1681}", "DIFF", "CONTROL", "Change:CHANGED"));
		expected.add(new ClassifierFeatureVector(commit, "DESTINATION", "./test/input/diff/pm2_new.js", "MethodNA", "{1682}", "DIFF", "CONTROL", "Change:CHANGED"));
		expected.add(new ClassifierFeatureVector(commit, "DESTINATION", "./test/input/diff/pm2_new.js", "MethodNA", "{1683}", "DIFF", "CONTROL", "Change:CHANGED"));
		expected.add(new ClassifierFeatureVector(commit, "DESTINATION", "./test/input/diff/pm2_new.js", "MethodNA", "{1684}", "DIFF", "CONTROL", "Change:CHANGED"));

		/* The ast-diff alerts. */
		expected.add(new ClassifierFeatureVector(commit, "DESTINATION", "./test/input/diff/pm2_new.js", "MethodNA", "{1662}", "DIFF", "AST", "INSERTED"));
		expected.add(new ClassifierFeatureVector(commit, "DESTINATION", "./test/input/diff/pm2_new.js", "MethodNA", "{1667}", "DIFF", "AST", "INSERTED"));
		expected.add(new ClassifierFeatureVector(commit, "DESTINATION", "./test/input/diff/pm2_new.js", "MethodNA", "{1668}", "DIFF", "AST", "INSERTED"));
		expected.add(new ClassifierFeatureVector(commit, "DESTINATION", "./test/input/diff/pm2_new.js", "MethodNA", "{1671}", "DIFF", "AST", "INSERTED"));
		expected.add(new ClassifierFeatureVector(commit, "DESTINATION", "./test/input/diff/pm2_new.js", "MethodNA", "{1672}", "DIFF", "AST", "INSERTED"));
		expected.add(new ClassifierFeatureVector(commit, "DESTINATION", "./test/input/diff/pm2_new.js", "MethodNA", "{1675}", "DIFF", "AST", "MOVED"));
		expected.add(new ClassifierFeatureVector(commit, "DESTINATION", "./test/input/diff/pm2_new.js", "MethodNA", "{1676}", "DIFF", "AST", "MOVED"));
		expected.add(new ClassifierFeatureVector(commit, "DESTINATION", "./test/input/diff/pm2_new.js", "MethodNA", "{1677}", "DIFF", "AST", "MOVED"));
		expected.add(new ClassifierFeatureVector(commit, "DESTINATION", "./test/input/diff/pm2_new.js", "MethodNA", "{1678}", "DIFF", "AST", "INSERTED"));
		expected.add(new ClassifierFeatureVector(commit, "DESTINATION", "./test/input/diff/pm2_new.js", "MethodNA", "{1680}", "DIFF", "AST", "MOVED"));
		expected.add(new ClassifierFeatureVector(commit, "DESTINATION", "./test/input/diff/pm2_new.js", "MethodNA", "{1681}", "DIFF", "AST", "MOVED"));
		expected.add(new ClassifierFeatureVector(commit, "DESTINATION", "./test/input/diff/pm2_new.js", "MethodNA", "{1682}", "DIFF", "AST", "MOVED"));
		expected.add(new ClassifierFeatureVector(commit, "DESTINATION", "./test/input/diff/pm2_new.js", "MethodNA", "{1683}", "DIFF", "AST", "MOVED"));
		expected.add(new ClassifierFeatureVector(commit, "DESTINATION", "./test/input/diff/pm2_new.js", "MethodNA", "{1684}", "DIFF", "AST", "MOVED"));

		expected.add(new ClassifierFeatureVector(commit, "SOURCE", "./test/input/diff/pm2_new.js", "MethodNA", "{1665}", "DIFF", "AST", "MOVED"));
		expected.add(new ClassifierFeatureVector(commit, "SOURCE", "./test/input/diff/pm2_new.js", "MethodNA", "{1666}", "DIFF", "AST", "MOVED"));
		expected.add(new ClassifierFeatureVector(commit, "SOURCE", "./test/input/diff/pm2_new.js", "MethodNA", "{1667}", "DIFF", "AST", "MOVED"));
		expected.add(new ClassifierFeatureVector(commit, "SOURCE", "./test/input/diff/pm2_new.js", "MethodNA", "{1669}", "DIFF", "AST", "MOVED"));
		expected.add(new ClassifierFeatureVector(commit, "SOURCE", "./test/input/diff/pm2_new.js", "MethodNA", "{1670}", "DIFF", "AST", "MOVED"));
		expected.add(new ClassifierFeatureVector(commit, "SOURCE", "./test/input/diff/pm2_new.js", "MethodNA", "{1671}", "DIFF", "AST", "MOVED"));
		expected.add(new ClassifierFeatureVector(commit, "SOURCE", "./test/input/diff/pm2_new.js", "MethodNA", "{1672}", "DIFF", "AST", "MOVED"));
		expected.add(new ClassifierFeatureVector(commit, "SOURCE", "./test/input/diff/pm2_new.js", "MethodNA", "{1673}", "DIFF", "AST", "MOVED"));

		/* The line-diff alerts. */
		expected.add(new ClassifierFeatureVector(commit, "DESTINATION", "./test/input/diff/pm2_new.js", "MethodNA", "{1662}", "DIFF", "LINE", "INSERTED"));
		expected.add(new ClassifierFeatureVector(commit, "DESTINATION", "./test/input/diff/pm2_new.js", "MethodNA", "{1666}", "DIFF", "LINE", "INSERTED"));
		expected.add(new ClassifierFeatureVector(commit, "DESTINATION", "./test/input/diff/pm2_new.js", "MethodNA", "{1667}", "DIFF", "LINE", "INSERTED"));
		expected.add(new ClassifierFeatureVector(commit, "DESTINATION", "./test/input/diff/pm2_new.js", "MethodNA", "{1668}", "DIFF", "LINE", "INSERTED"));
		expected.add(new ClassifierFeatureVector(commit, "DESTINATION", "./test/input/diff/pm2_new.js", "MethodNA", "{1669}", "DIFF", "LINE", "INSERTED"));
		expected.add(new ClassifierFeatureVector(commit, "DESTINATION", "./test/input/diff/pm2_new.js", "MethodNA", "{1670}", "DIFF", "LINE", "INSERTED"));
		expected.add(new ClassifierFeatureVector(commit, "DESTINATION", "./test/input/diff/pm2_new.js", "MethodNA", "{1671}", "DIFF", "LINE", "INSERTED"));
		expected.add(new ClassifierFeatureVector(commit, "DESTINATION", "./test/input/diff/pm2_new.js", "MethodNA", "{1672}", "DIFF", "LINE", "INSERTED"));
		expected.add(new ClassifierFeatureVector(commit, "DESTINATION", "./test/input/diff/pm2_new.js", "MethodNA", "{1673}", "DIFF", "LINE", "INSERTED"));
		expected.add(new ClassifierFeatureVector(commit, "DESTINATION", "./test/input/diff/pm2_new.js", "MethodNA", "{1674}", "DIFF", "LINE", "INSERTED"));
		expected.add(new ClassifierFeatureVector(commit, "DESTINATION", "./test/input/diff/pm2_new.js", "MethodNA", "{1675}", "DIFF", "LINE", "INSERTED"));
		expected.add(new ClassifierFeatureVector(commit, "DESTINATION", "./test/input/diff/pm2_new.js", "MethodNA", "{1676}", "DIFF", "LINE", "INSERTED"));
		expected.add(new ClassifierFeatureVector(commit, "DESTINATION", "./test/input/diff/pm2_new.js", "MethodNA", "{1677}", "DIFF", "LINE", "INSERTED"));
		expected.add(new ClassifierFeatureVector(commit, "DESTINATION", "./test/input/diff/pm2_new.js", "MethodNA", "{1678}", "DIFF", "LINE", "INSERTED"));
		expected.add(new ClassifierFeatureVector(commit, "DESTINATION", "./test/input/diff/pm2_new.js", "MethodNA", "{1680}", "DIFF", "LINE", "INSERTED"));
		expected.add(new ClassifierFeatureVector(commit, "DESTINATION", "./test/input/diff/pm2_new.js", "MethodNA", "{1681}", "DIFF", "LINE", "INSERTED"));
		expected.add(new ClassifierFeatureVector(commit, "DESTINATION", "./test/input/diff/pm2_new.js", "MethodNA", "{1682}", "DIFF", "LINE", "INSERTED"));
		expected.add(new ClassifierFeatureVector(commit, "DESTINATION", "./test/input/diff/pm2_new.js", "MethodNA", "{1683}", "DIFF", "LINE", "INSERTED"));
		expected.add(new ClassifierFeatureVector(commit, "DESTINATION", "./test/input/diff/pm2_new.js", "MethodNA", "{1684}", "DIFF", "LINE", "INSERTED"));
		expected.add(new ClassifierFeatureVector(commit, "DESTINATION", "./test/input/diff/pm2_new.js", "MethodNA", "{1685}", "DIFF", "LINE", "INSERTED"));
		expected.add(new ClassifierFeatureVector(commit, "DESTINATION", "./test/input/diff/pm2_new.js", "MethodNA", "{1689}", "DIFF", "LINE", "INSERTED"));

		expected.add(new ClassifierFeatureVector(commit, "SOURCE", "./test/input/diff/pm2_new.js", "MethodNA", "{1665}", "DIFF", "LINE", "REMOVED"));
		expected.add(new ClassifierFeatureVector(commit, "SOURCE", "./test/input/diff/pm2_new.js", "MethodNA", "{1666}", "DIFF", "LINE", "REMOVED"));
		expected.add(new ClassifierFeatureVector(commit, "SOURCE", "./test/input/diff/pm2_new.js", "MethodNA", "{1667}", "DIFF", "LINE", "REMOVED"));
		expected.add(new ClassifierFeatureVector(commit, "SOURCE", "./test/input/diff/pm2_new.js", "MethodNA", "{1669}", "DIFF", "LINE", "REMOVED"));
		expected.add(new ClassifierFeatureVector(commit, "SOURCE", "./test/input/diff/pm2_new.js", "MethodNA", "{1670}", "DIFF", "LINE", "REMOVED"));
		expected.add(new ClassifierFeatureVector(commit, "SOURCE", "./test/input/diff/pm2_new.js", "MethodNA", "{1671}", "DIFF", "LINE", "REMOVED"));
		expected.add(new ClassifierFeatureVector(commit, "SOURCE", "./test/input/diff/pm2_new.js", "MethodNA", "{1672}", "DIFF", "LINE", "REMOVED"));
		expected.add(new ClassifierFeatureVector(commit, "SOURCE", "./test/input/diff/pm2_new.js", "MethodNA", "{1673}", "DIFF", "LINE", "REMOVED"));

		/* The line-total alerts. */
		expected.add(new ClassifierFeatureVector(commit, "SOURCE", "./test/input/diff/pm2_new.js", "MethodNA", "NA", "DIFF", "TOTAL_LINES", "2373"));
		expected.add(new ClassifierFeatureVector(commit, "DESTINATION", "./test/input/diff/pm2_new.js", "MethodNA", "NA", "DIFF", "TOTAL_LINES", "2386"));

		this.runTest(sourceCodeFileChanges, expected, false);
	}

	@Test
	public void testPM2_mocha() throws Exception {

		/* The test files. */
		String src = "./test/input/diff/pm2_mocha_old.js";
		String dst = "./test/input/diff/pm2_mocha_new.js";

		/* Read the source files. */
		List<SourceCodeFileChange> sourceCodeFileChanges = new LinkedList<SourceCodeFileChange>();
		sourceCodeFileChanges.add(getSourceCodeFileChange(src, dst));

		/* Build the expected feature vectors. */
		Commit commit = getCommit();
		List<ClassifierFeatureVector> expected = new LinkedList<ClassifierFeatureVector>();

		// No checks, no error is pass.

		this.runTest(sourceCodeFileChanges, expected, false);
	}

	@Test
	public void testPM2_treekill() throws Exception {

		/* Read the source files. */
		List<SourceCodeFileChange> sourceCodeFileChanges = new LinkedList<SourceCodeFileChange>();
		sourceCodeFileChanges.add(getSourceCodeFileChange(
				"./test/input/diff/pm2_treekill_old.js",
				"./test/input/diff/pm2_treekill_new.js"));

		/* Build the expected feature vectors. */
		Commit commit = getCommit();
		List<ClassifierFeatureVector> expected = new LinkedList<ClassifierFeatureVector>();

		// No checks, no error is pass.

		this.runTest(sourceCodeFileChanges, expected, false);
	}

	@Test
	public void testPM2_reload() throws Exception {

		/* Read the source files. */
		List<SourceCodeFileChange> sourceCodeFileChanges = new LinkedList<SourceCodeFileChange>();
		sourceCodeFileChanges.add(getSourceCodeFileChange(
				"./test/input/diff/pm2_reload_old.js",
				"./test/input/diff/pm2_reload_new.js"));

		/* Build the expected feature vectors. */
		Commit commit = getCommit();
		List<ClassifierFeatureVector> expected = new LinkedList<ClassifierFeatureVector>();

		// No checks, no error is pass.

		this.runTest(sourceCodeFileChanges, expected, false);
	}

	@Test
	public void testPM2_self() throws Exception {

		/* Read the source files. */
		List<SourceCodeFileChange> sourceCodeFileChanges = new LinkedList<SourceCodeFileChange>();
		sourceCodeFileChanges.add(getSourceCodeFileChange(
				"./test/input/diff/pm2_self_old.js",
				"./test/input/diff/pm2_self_new.js"));

		/* Build the expected feature vectors. */
		Commit commit = getCommit();
		List<ClassifierFeatureVector> expected = new LinkedList<ClassifierFeatureVector>();

		// No checks, no error is pass.

		this.runTest(sourceCodeFileChanges, expected, false);
	}

	@Test
	public void testPM2_interactor() throws Exception {

		/* Read the source files. */
		List<SourceCodeFileChange> sourceCodeFileChanges = new LinkedList<SourceCodeFileChange>();
		sourceCodeFileChanges.add(getSourceCodeFileChange(
				"./test/input/diff/pm2_interactor_old.js",
				"./test/input/diff/pm2_interactor_new.js"));

		/* Build the expected feature vectors. */
		Commit commit = getCommit();
		List<ClassifierFeatureVector> expected = new LinkedList<ClassifierFeatureVector>();

		// No checks, no error is pass.

		this.runTest(sourceCodeFileChanges, expected, false);
	}

	/**
	 * This is an example of where our analysis fails to display an updated
	 * value in the store. The object literal is passed to process.send as an
	 * argument. Because the object literal is not stored in a variable
	 * accessible from the local environment, the object literal is not
	 * visible (through the environment) when visited by ValueCFGVisitor.
	 *
	 * Solution TODO: Create a dummy variable in the environment pointing to
	 * 				  the object literal.
	 */
	@Test
	public void testPM2_human_event() throws Exception {

		/* Read the source files. */
		List<SourceCodeFileChange> sourceCodeFileChanges = new LinkedList<SourceCodeFileChange>();
		sourceCodeFileChanges.add(getSourceCodeFileChange(
				"./test/input/diff/pm2_human_event_old.js",
				"./test/input/diff/pm2_human_event_new.js"));

		/* Build the expected feature vectors. */
		Commit commit = getCommit();
		List<ClassifierFeatureVector> expected = new LinkedList<ClassifierFeatureVector>();

		// No checks, no error is pass.

		this.runTest(sourceCodeFileChanges, expected, false);
	}

	/**
	 * This is an example of where our analysis fails to display an updated
	 * value. Because the property being assigned is accessed through a
	 * dynamic access (ie. 'God.clusters_db[clu.pm2_env.pm_id]...'), the
	 * value of which is not available to the analysis, the analysis cannot
	 * create a property name where the value should be stored in the object.
	 *
	 * Solution TODO: Create a dummy property. This will make the solution
	 * 				  less sound, but is probably ok in practice.
	 */
	@Test
	public void testPM2_dynprop() throws Exception {

		/* Read the source files. */
		List<SourceCodeFileChange> sourceCodeFileChanges = new LinkedList<SourceCodeFileChange>();
		sourceCodeFileChanges.add(getSourceCodeFileChange(
				"./test/input/diff/pm2_dynprop_old.js",
				"./test/input/diff/pm2_dynprop_new.js"));

		/* Build the expected feature vectors. */
		Commit commit = getCommit();
		List<ClassifierFeatureVector> expected = new LinkedList<ClassifierFeatureVector>();

		// No checks, no error is pass.

		this.runTest(sourceCodeFileChanges, expected, false);
	}

	@Test
	public void testPM2_reverse() throws Exception {

		/* Read the source files. */
		List<SourceCodeFileChange> sourceCodeFileChanges = new LinkedList<SourceCodeFileChange>();
		sourceCodeFileChanges.add(getSourceCodeFileChange(
				"./test/input/diff/pm2_reverse_old.js",
				"./test/input/diff/pm2_reverse_new.js"));

		/* Build the expected feature vectors. */
		Commit commit = getCommit();
		List<ClassifierFeatureVector> expected = new LinkedList<ClassifierFeatureVector>();

		// No checks, no error is pass.

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
	 * @return The Datalog query that selects identifier uses.
	 * @throws ParserException when iris fails to parse the query string.
	 */
	public static Map<IQuery,Transformer> getUseQueries() throws ParserException {

		Map<IQuery, Transformer> queries = new HashMap<IQuery, Transformer>();

		Pair<IQuery, Transformer> valueQuery = getValueQuery();
		queries.put(valueQuery.getLeft(), valueQuery.getRight());

//		Pair<IQuery, Transformer> environmentQuery = getEnvironmentQuery();
//		queries.put(environmentQuery.getLeft(), environmentQuery.getRight());
//
//		Pair<IQuery, Transformer> controlQuery = getControlQuery();
//		queries.put(controlQuery.getLeft(), controlQuery.getRight());

//		Pair<IQuery, Transformer> astQuery = getAstQuery();
//		queries.put(astQuery.getLeft(), astQuery.getRight());
//
//		Pair<IQuery, Transformer> lineQuery = getLineQuery();
//		queries.put(lineQuery.getLeft(), lineQuery.getRight());
//
//		Pair<IQuery, Transformer> totalLinesQuery = getTotalLinesQuery();
//		queries.put(totalLinesQuery.getLeft(), totalLinesQuery.getRight());

		return queries;

	}

	/**
	 * @return The query for extracting value-diff alerts.
	 * @throws ParserException for incorrect query strings.
	 */
	private static Pair<IQuery, Transformer> getValueQuery() throws ParserException {

		String qs = "";
		qs += "?- Value(?Version,?File,?Line,?StatementID,?Identifier,?ValChange)";
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
						tuple.get(3).toString().replace("\'",  ""),			// AST Node ID
						tuple.get(2).toString().replace("\'", ""),			// Line
						"DIFF",												// Type
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
						tuple.get(3).toString().replace("\'",  ""),			// AST Node ID
						tuple.get(2).toString().replace("\'", ""),			// Line
						"DIFF",												// Type
						"ENV",												// Subtype
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
						tuple.get(3).toString().replace("\'",  ""),			// AST Node ID
						tuple.get(2).toString().replace("\'", ""),			// Line
						"DIFF",												// Type
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
						tuple.get(3).toString().replace("\'",  ""),			// AST Node ID
						tuple.get(2).toString().replace("\'", ""),			// Line
						"DIFF",												// Type
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
						"NA",												// Method
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
						"NA",												// AST Node ID
						tuple.get(2).toString().replace("\'", ""),			// Line
						"DIFF",												// Type
						"LINE",												// Subtype
						tuple.get(3).toString().replace("\'", ""));			// Description
		};

		return Pair.of(query, transformer);

	}

}