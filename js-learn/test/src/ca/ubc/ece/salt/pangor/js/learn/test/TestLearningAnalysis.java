package ca.ubc.ece.salt.pangor.js.learn.test;


import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.Assert;
import org.junit.Test;

import ca.ubc.ece.salt.gumtree.ast.ClassifiedASTNode.ChangeType;
import ca.ubc.ece.salt.pangor.analysis.Commit;
import ca.ubc.ece.salt.pangor.analysis.CommitAnalysis;
import ca.ubc.ece.salt.pangor.analysis.DomainAnalysis;
import ca.ubc.ece.salt.pangor.analysis.SourceCodeFileChange;
import ca.ubc.ece.salt.pangor.api.AbstractAPI;
import ca.ubc.ece.salt.pangor.api.KeywordDefinition.KeywordType;
import ca.ubc.ece.salt.pangor.api.KeywordUse;
import ca.ubc.ece.salt.pangor.api.KeywordUse.KeywordContext;
import ca.ubc.ece.salt.pangor.api.TopLevelAPI;
import ca.ubc.ece.salt.pangor.js.api.JSAPIFactory;
import ca.ubc.ece.salt.pangor.js.learn.analysis.LearningDomainAnalysis;
import ca.ubc.ece.salt.pangor.learn.analysis.KeywordFilter;
import ca.ubc.ece.salt.pangor.learn.analysis.LearningDataSet;

public class TestLearningAnalysis {

	/**
	 * Tests data mining data set construction.
	 * @param args The command line arguments (i.e., old and new file names).
	 * @throws Exception
	 */
	protected void runTest(SourceCodeFileChange sourceFileChange,
						   List<MockFeatureVector> expected,
						   boolean printAlerts) throws Exception {

		Commit commit = getCommit();
		commit.addSourceCodeFileChange(sourceFileChange);

		/* The packages we want to use. */
		KeywordFilter fsFilter = KeywordFilter.buildPackageFilter("fs");
		KeywordFilter pathFilter = KeywordFilter.buildPackageFilter("path");
		KeywordFilter DateFilter = KeywordFilter.buildPackageFilter("global");

		/* Set up the FeatureVectorManager, which will store all the feature
		 * vectors produced by our analysis and perform pre-processing tasks
		 * for data mining. */
		List<KeywordFilter> filters = Arrays.asList(fsFilter, pathFilter, DateFilter);
		LearningDataSet dataSet = LearningDataSet.createLearningDataSet(filters);

		/* Set up the analysis. */
		List<DomainAnalysis> domains = new LinkedList<DomainAnalysis>();
		LearningDomainAnalysis analysis = LearningDomainAnalysis.createLearningAnalysis();
		domains.add(analysis);

		/* Set up the commit analysis. */
		CommitAnalysis commitAnalysis = new CommitAnalysis(dataSet, domains);

		/* Run the analysis. */
		commitAnalysis.analyze(commit);

        /* Pre-process the alerts. */
        dataSet.preProcess();

        /* Print the data set. */
        System.out.println(dataSet.getLearningFeatureVectorHeader());
        System.out.println(dataSet.getLearningFeatureVector());

        /* Verify the expected feature vectors match the actual feature vectors. */
        for(MockFeatureVector fv : expected) {
        	Assert.assertTrue(dataSet.contains(fv.functionName, fv.expectedKeywords));
        }
	}

	/*
	 * Tests if predictor actually realize that parse() belongs to "Date" which
	 * is globally available. To make things more interesting, "path" is also
	 * imported (and also has method parse()), but we use other keywords from
	 * Date to help the predictor
	 */
	@Test
	public void testDate() throws Exception {

		/* The test files. */
		String src = "./test/input/learning/date_old.js";
		String dst = "./test/input/learning/date_new.js";

		/* Define the expected keywords. */
		TopLevelAPI topLevelAPI = JSAPIFactory.buildTopLevelAPI();
		AbstractAPI DateAPI = topLevelAPI.getFirstKeyword(KeywordType.CLASS, "Date").api;
		KeywordUse parse = new KeywordUse(KeywordType.METHOD, KeywordContext.METHOD_CALL, "parse", ChangeType.INSERTED,
				DateAPI);

		MockFeatureVector function = new MockFeatureVector("testFunction");
		function.expectedKeywords.add(Pair.of(parse, 1));

		/* Read the source files. */
		SourceCodeFileChange sourceCodeFileChange = getSourceCodeFileChange(src, dst);

		this.runTest(sourceCodeFileChange, Arrays.asList(function), true);
	}

	/*
	 * Tests if predictor actually realize that parse() belongs to "path" (which
	 * was imported), and not to "Date", which is globally available
	 */
//	@Test
//	public void testPath() throws Exception {
//		String src = "./test/input/learning/path_old.js";
//		String dst = "./test/input/learning/path_new.js";
//
//		PackageAPI path = JSAPIFactory.buildPathPackage();
//
//		KeywordUse parse = new KeywordUse(KeywordType.METHOD, KeywordContext.METHOD_CALL, "parse", ChangeType.INSERTED,
//				path);
//		KeywordUse pkg = new KeywordUse(KeywordType.PACKAGE, KeywordContext.REQUIRE, "path", ChangeType.INSERTED, path);
//
//		MockFeatureVector function = new MockFeatureVector("testFunction");
//		function.expectedKeywords.add(Pair.of(parse, 1));
//
//		MockFeatureVector script = new MockFeatureVector("~script~");
//		script.expectedKeywords.add(Pair.of(pkg, 1));
//
//		this.runTest(new String[] { src, dst }, Arrays.asList(function, script));
//	}

	/*
	 * Tests if predictor recognizes JS reserved words.
	 */
	@Test
	public void testReservedWords() throws Exception {

		/* The test files. */
		String src = "./test/input/learning/keyword_old.js";
		String dst = "./test/input/learning/keyword_new.js";

		/* Define the expected keywords. */
		TopLevelAPI api = JSAPIFactory.buildTopLevelAPI();
		KeywordUse typeof = new KeywordUse(KeywordType.RESERVED, KeywordContext.CONDITION, "typeof", ChangeType.INSERTED, api);

		MockFeatureVector script = new MockFeatureVector("~script~");
		script.expectedKeywords.add(Pair.of(typeof, 1));

		/* Read the source files. */
		SourceCodeFileChange sourceCodeFileChange = TestLearningAnalysis.getSourceCodeFileChange(src, dst);

		this.runTest(sourceCodeFileChange, Arrays.asList(script), true);
	}

	@Test
	public void testFalsey() throws Exception {

		/* The test files. */
		String src = "./test/input/learning/falsey_old.js";
		String dst = "./test/input/learning/falsey_new.js";

		/* Define the expected keywords. */
		TopLevelAPI api = JSAPIFactory.buildTopLevelAPI();
		KeywordUse falsey = new KeywordUse(KeywordType.RESERVED, KeywordContext.CONDITION, "falsey", ChangeType.INSERTED, api);

		MockFeatureVector script = new MockFeatureVector("~script~");
		script.expectedKeywords.add(Pair.of(falsey, 1));

		/* Read the source files. */
		SourceCodeFileChange sourceCodeFileChange = TestLearningAnalysis.getSourceCodeFileChange(src, dst);

		this.runTest(sourceCodeFileChange, Arrays.asList(script), true);
	}

	@Test
	public void testFalseyNot() throws Exception {

		/* The test files. */
		String src = "./test/input/learning/falsey2_old.js";
		String dst = "./test/input/learning/falsey2_new.js";

		/* Define the expected keywords. */
		TopLevelAPI api = JSAPIFactory.buildTopLevelAPI();
		KeywordUse falsey = new KeywordUse(KeywordType.RESERVED, KeywordContext.CONDITION, "falsey", ChangeType.INSERTED, api);

		MockFeatureVector script = new MockFeatureVector("~script~");
		script.expectedKeywords.add(Pair.of(falsey, 1));

		/* Read the source files. */
		SourceCodeFileChange sourceCodeFileChange = TestLearningAnalysis.getSourceCodeFileChange(src, dst);

		this.runTest(sourceCodeFileChange, Arrays.asList(script), true);
	}

	@Test
	public void testFalseyWhile() throws Exception {
		String src = "./test/input/learning/falsey3_old.js";
		String dst = "./test/input/learning/falsey3_new.js";

		TopLevelAPI api = JSAPIFactory.buildTopLevelAPI();

		KeywordUse falsey = new KeywordUse(KeywordType.RESERVED, KeywordContext.CONDITION, "falsey", ChangeType.INSERTED, api);

		MockFeatureVector script = new MockFeatureVector("~script~");
		script.expectedKeywords.add(Pair.of(falsey, 1));

		SourceCodeFileChange sourceCodeFileChange = TestLearningAnalysis.getSourceCodeFileChange(src, dst);

		this.runTest(sourceCodeFileChange, Arrays.asList(script), true);
	}

	@Test
	public void testFalseyConditional() throws Exception {
		String src = "./test/input/learning/falsey4_old.js";
		String dst = "./test/input/learning/falsey4_new.js";

		TopLevelAPI api = JSAPIFactory.buildTopLevelAPI();

		KeywordUse falsey = new KeywordUse(KeywordType.RESERVED, KeywordContext.CONDITION, "falsey", ChangeType.INSERTED, api);

		MockFeatureVector script = new MockFeatureVector("~script~");
		script.expectedKeywords.add(Pair.of(falsey, 1));

		SourceCodeFileChange sourceCodeFileChange = TestLearningAnalysis.getSourceCodeFileChange(src, dst);

		this.runTest(sourceCodeFileChange, Arrays.asList(script), true);
	}

	@Test
	public void testFalseyAnd() throws Exception {
		String src = "./test/input/learning/falsey5_old.js";
		String dst = "./test/input/learning/falsey5_new.js";

		TopLevelAPI api = JSAPIFactory.buildTopLevelAPI();

		KeywordUse falsey = new KeywordUse(KeywordType.RESERVED, KeywordContext.CONDITION, "falsey", ChangeType.INSERTED, api);

		MockFeatureVector script = new MockFeatureVector("~script~");
		script.expectedKeywords.add(Pair.of(falsey, 1));

		SourceCodeFileChange sourceCodeFileChange = TestLearningAnalysis.getSourceCodeFileChange(src, dst);

		this.runTest(sourceCodeFileChange, Arrays.asList(script), true);
	}

	@Test
	public void testVar() throws Exception {
		String src = "./test/input/learning/var_old.js";
		String dst = "./test/input/learning/var_new.js";

		TopLevelAPI api = JSAPIFactory.buildTopLevelAPI();

		KeywordUse falsey = new KeywordUse(KeywordType.RESERVED, KeywordContext.STATEMENT, "var", ChangeType.INSERTED, api);

		MockFeatureVector script = new MockFeatureVector("~script~");
		script.expectedKeywords.add(Pair.of(falsey, 1));

		SourceCodeFileChange sourceCodeFileChange = TestLearningAnalysis.getSourceCodeFileChange(src, dst);

		this.runTest(sourceCodeFileChange, Arrays.asList(script), true);
	}

	@Test
	public void testCallbackError() throws Exception {
		String src = "./test/input/learning/cbe_old.js";
		String dst = "./test/input/learning/cbe_new.js";

		TopLevelAPI api = JSAPIFactory.buildTopLevelAPI();

		KeywordUse errorParam = new KeywordUse(KeywordType.RESERVED, KeywordContext.PARAMETER_DECLARATION, "error", ChangeType.INSERTED, api);
		KeywordUse falsey = new KeywordUse(KeywordType.RESERVED, KeywordContext.CONDITION, "falsey", ChangeType.INSERTED, api);
		KeywordUse errorArg = new KeywordUse(KeywordType.RESERVED, KeywordContext.ARGUMENT, "error", ChangeType.INSERTED, api);

		MockFeatureVector script = new MockFeatureVector("print");
		script.expectedKeywords.add(Pair.of(errorParam, 1));
		script.expectedKeywords.add(Pair.of(falsey, 1));
		script.expectedKeywords.add(Pair.of(errorArg, 1));

		SourceCodeFileChange sourceCodeFileChange = TestLearningAnalysis.getSourceCodeFileChange(src, dst);

		this.runTest(sourceCodeFileChange, Arrays.asList(script), true);
	}

	@Test
	public void testStatements() throws Exception {
		String src = "./test/input/learning/statement_old.js";
		String dst = "./test/input/learning/statement_new.js";

		TopLevelAPI api = JSAPIFactory.buildTopLevelAPI();

		KeywordUse returnStatement = new KeywordUse(KeywordType.RESERVED, KeywordContext.STATEMENT, "return", ChangeType.INSERTED, api);
		KeywordUse varStatement = new KeywordUse(KeywordType.RESERVED, KeywordContext.STATEMENT, "var", ChangeType.INSERTED, api);
		KeywordUse breakStatement = new KeywordUse(KeywordType.RESERVED, KeywordContext.STATEMENT, "break", ChangeType.INSERTED, api);
		KeywordUse continueStatement = new KeywordUse(KeywordType.RESERVED, KeywordContext.STATEMENT, "continue", ChangeType.INSERTED, api);

		MockFeatureVector testFunction = new MockFeatureVector("~testFunction~");
		testFunction.expectedKeywords.add(Pair.of(returnStatement, 1));

		MockFeatureVector script = new MockFeatureVector("~script~");
		script.expectedKeywords.add(Pair.of(varStatement, 1));
		script.expectedKeywords.add(Pair.of(breakStatement, 1));
		script.expectedKeywords.add(Pair.of(continueStatement, 1));

		SourceCodeFileChange sourceCodeFileChange = TestLearningAnalysis.getSourceCodeFileChange(src, dst);

		this.runTest(sourceCodeFileChange, Arrays.asList(script), true);
	}

	/*
	 * Tests if predictor recognizes our soft keywords.
	 */
	@Test
	public void testSoftKeywords() throws Exception {
		String src = "./test/input/learning/soft_old.js";
		String dst = "./test/input/learning/soft_new.js";

		TopLevelAPI api = JSAPIFactory.buildTopLevelAPI();

		KeywordUse typeof = new KeywordUse(KeywordType.RESERVED, KeywordContext.CONDITION, "typeof", ChangeType.INSERTED, api);
//		KeywordUse blank = new KeywordUse(KeywordType.RESERVED, KeywordContext.CONDITION, "blank", ChangeType.INSERTED, api);
//		KeywordUse zero = new KeywordUse(KeywordType.RESERVED, KeywordContext.CONDITION, "zero", ChangeType.INSERTED, api);
		KeywordUse undefined = new KeywordUse(KeywordType.RESERVED, KeywordContext.CONDITION, "undefined", ChangeType.INSERTED, api);
		KeywordUse callback = new KeywordUse(KeywordType.RESERVED, KeywordContext.METHOD_CALL, "callback", ChangeType.INSERTED, api);
		KeywordUse error = new KeywordUse(KeywordType.RESERVED, KeywordContext.EXCEPTION_CATCH, "error", ChangeType.INSERTED, api);

		MockFeatureVector script = new MockFeatureVector("~script~");
		script.expectedKeywords.add(Pair.of(typeof, 4));
//		script.expectedKeywords.add(Pair.of(blank, 1));
//		script.expectedKeywords.add(Pair.of(zero, 1));
		script.expectedKeywords.add(Pair.of(undefined, 1));
		script.expectedKeywords.add(Pair.of(callback, 1));
		script.expectedKeywords.add(Pair.of(error, 1));

		SourceCodeFileChange sourceCodeFileChange = TestLearningAnalysis.getSourceCodeFileChange(src, dst);

		this.runTest(sourceCodeFileChange, Arrays.asList(script), true);
	}

	/*
	 * Tests if predictor recognizes the .bind method call
	 */
	@Test
	public void testBind() throws Exception {
		String src = "./test/input/learning/this_old.js";
		String dst = "./test/input/learning/this_new.js";

		TopLevelAPI api = JSAPIFactory.buildTopLevelAPI();

		KeywordUse bind = new KeywordUse(KeywordType.METHOD, KeywordContext.METHOD_CALL, "bind", ChangeType.INSERTED, api);

		MockFeatureVector script = new MockFeatureVector("~script~");
		script.expectedKeywords.add(Pair.of(bind, 1));

		SourceCodeFileChange sourceCodeFileChange = TestLearningAnalysis.getSourceCodeFileChange(src, dst);

		this.runTest(sourceCodeFileChange, Arrays.asList(script), true);
	}

//	@Test
//	public void testFileSystem() throws Exception {
//		String src = "./test/input/learning/lrn_fs_old.js";
//		String dst = "./test/input/learning/lrn_fs_new.js";
//
//		PackageAPI fs = JSAPIFactory.buildFileSystemPackage();
//
//		/* Create the expected keywords. */
//		KeywordUse openSync = new KeywordUse(KeywordType.METHOD, KeywordContext.METHOD_CALL, "openSync",
//				ChangeType.UNCHANGED, fs);
//		KeywordUse closeSync = new KeywordUse(KeywordType.METHOD, KeywordContext.METHOD_CALL, "closeSync",
//				ChangeType.INSERTED, fs);
//		KeywordUse existsSync = new KeywordUse(KeywordType.METHOD, KeywordContext.METHOD_CALL, "existsSync",
//				ChangeType.UNCHANGED, fs);
//		KeywordUse writeSync = new KeywordUse(KeywordType.METHOD, KeywordContext.METHOD_CALL, "writeSync",
//				ChangeType.UNCHANGED, fs);
//		KeywordUse fsU = new KeywordUse(KeywordType.PACKAGE, KeywordContext.REQUIRE, "fs", ChangeType.UNCHANGED, fs);
//		KeywordUse fsI = new KeywordUse(KeywordType.PACKAGE, KeywordContext.REQUIRE, "fs", ChangeType.INSERTED, fs);
//
//		/* Create the expected feature vectors. */
//		MockFeatureVector fv1 = new MockFeatureVector("writeHello");
//		fv1.expectedKeywords.add(Pair.of(openSync, 1));
//		fv1.expectedKeywords.add(Pair.of(closeSync, 1));
//		fv1.expectedKeywords.add(Pair.of(existsSync, 0));
//		fv1.expectedKeywords.add(Pair.of(writeSync, 1));
//		fv1.expectedKeywords.add(Pair.of(fsU, 0));
//		fv1.expectedKeywords.add(Pair.of(fsI, 0));
//
//		MockFeatureVector fv2 = new MockFeatureVector("writeHello");
//		fv2.expectedKeywords.add(Pair.of(openSync, 0));
//		fv2.expectedKeywords.add(Pair.of(closeSync, 0));
//		fv2.expectedKeywords.add(Pair.of(existsSync, 1));
//		fv2.expectedKeywords.add(Pair.of(writeSync, 0));
//		fv2.expectedKeywords.add(Pair.of(fsU, 2));
//		fv2.expectedKeywords.add(Pair.of(fsI, 1));
//
//		List<MockFeatureVector> expected = Arrays.asList(fv1, fv2);
//
//		this.runTest(new String[] {src, dst}, expected);
//	}

	/**
	 * A mock FeatureVector for verifying test cases.
	 */
	private class MockFeatureVector {

		public String functionName;
		public List<Pair<KeywordUse, Integer>> expectedKeywords;

		public MockFeatureVector(String functionName) {
			this.functionName = functionName;
			this.expectedKeywords = new LinkedList<Pair<KeywordUse, Integer>>();
		}

	}

	/**
	 * @return A dummy commit for testing.
	 */
	public static Commit getCommit() {
		return new Commit("test", "http://github.com/saltlab/Pangor", "c0", "c1", true);
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

}