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
import commitminer.analysis.annotation.AnnotationDataSet;
import commitminer.analysis.annotation.AnnotationFactBase;
import commitminer.analysis.factories.ICommitAnalysisFactory;
import commitminer.analysis.options.Options;
import commitminer.classify.ClassifierFeatureVector;
import commitminer.classify.Transformer;
import commitminer.js.diff.DiffCommitAnalysisFactory;
import commitminer.js.diff.view.HTMLMultiDiffViewer;
import commitminer.js.diff.view.HTMLUnixDiffViewer;

public class TestMultiDiffHTMLView {

	/**
	 * Tests data mining data set construction.
	 * @param args The command line arguments (i.e., old and new file names).
	 * @throws Exception
	 */
	protected void runTest(
			String src, String dst, String out) throws Exception {

		/* Set the options for this run. */
		Options.createInstance(Options.DiffMethod.GUMTREE, Options.ChangeImpact.MULTIDIFF);

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
		ICommitAnalysisFactory commitFactory = new DiffCommitAnalysisFactory(dataSet);
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
		String out = "./web/env.html";

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
	public void testPM2() throws Exception {
	
		String src = "./test/input/diff/pm2_old.js";
		String dst = "./test/input/diff/pm2_new.js";
		String out = "./web/pm2.html";

		runTest(src, dst, out);
		
	}

	@Test
	public void testRuntime() throws Exception {
	
		String src = "./test/input/diff/karma_new.js";
		String dst = "./test/input/diff/karma_new.js";
		String out = "./web/runs.html";

		runTest(src, dst, out);
		
	}

	@Test
	public void testCheerio() throws Exception {
		String src = "./test/input/user_study/cheerio-e65ad72cad8fb696e0f3475b127c93492feca04d_old.js";
		String dst = "./test/input/user_study/cheerio-e65ad72cad8fb696e0f3475b127c93492feca04d_new.js";
		String out = "./web/cheerio.html";
		runTest(src, dst, out);
	}

	@Test
	public void testKarma() throws Exception {
		String src = "./test/input/user_study/karma-82f1c1207b34955602b7590a34f8bf50b1a5ba6a_old.js";
		String dst = "./test/input/user_study/karma-82f1c1207b34955602b7590a34f8bf50b1a5ba6a_new.js";
		String out = "./web/karma.html";
		runTest(src, dst, out);
	}

	@Test
	public void testGenerator() throws Exception {
		String src = "./test/input/user_study/generator-f3a63f7a71cda9f977f66a11736858d43418b074_old.js";
		String dst = "./test/input/user_study/generator-f3a63f7a71cda9f977f66a11736858d43418b074_new.js";
		String out = "./web/generator.html";
		runTest(src, dst, out);
	}

	@Test
	public void testPopcornTime() throws Exception {
		String src = "./test/input/user_study/popcorn-desktop-db90cb014dc349c5587422c288c471f6de88f9f9_old.js";
		String dst = "./test/input/user_study/popcorn-desktop-db90cb014dc349c5587422c288c471f6de88f9f9_new.js";
		String out = "./web/popcorn.html";
		runTest(src, dst, out);
	}

	/* USER STUDY CANDIDATE. */
	@Test
	public void testNPM() throws Exception {
		String src = "./test/input/user_study/npm-af94ccd140a72c4b7625e6dde6d76ab586a8134f_old.js";
		String dst = "./test/input/user_study/npm-af94ccd140a72c4b7625e6dde6d76ab586a8134f_new.js";
		String out = "./web/npm.html";
		runTest(src, dst, out);
	}

	/* USER STUDY CANDIDATE. */
	@Test
	public void testMongoDB() throws Exception {
		String src = "./test/input/user_study/mongodb-a243fbeb788d0d6191800decb0629be4b9f4dd63_old.js";
		String dst = "./test/input/user_study/mongodb-a243fbeb788d0d6191800decb0629be4b9f4dd63_new.js";
		String out = "./web/mongo.html";
		runTest(src, dst, out);
	}

	/* USER STUDY CANDIDATE. */
	@Test
	public void testPM2B() throws Exception {
		String src = "./test/input/user_study/pm2-402190264f92afb845f41ac671331a91b708c18f_old.js";
		String dst = "./test/input/user_study/pm2-402190264f92afb845f41ac671331a91b708c18f_new.js";
		String out = "./web/pm2b.html";
		runTest(src, dst, out);
	}

	/* USER STUDY CANDIDATE. */
	@Test
	public void testSubstack() throws Exception {
		String src = "./test/input/user_study/substack-83f9c24dc292aee00db4613208660502130be739_old.js";
		String dst = "./test/input/user_study/substack-83f9c24dc292aee00db4613208660502130be739_new.js";
		String out = "./web/substack.html";
		runTest(src, dst, out);
	}

	/* USER STUDY CANDIDATE. */
	@Test
	public void testMediacenterJS() throws Exception {
		String src = "./test/input/user_study/mediacenterjs-f8f1713fc8a15d652c571ee0e5176889dcea095d_old.js";
		String dst = "./test/input/user_study/mediacenterjs-f8f1713fc8a15d652c571ee0e5176889dcea095d_new.js";
		String out = "./web/mediacenterjs.html";
		runTest(src, dst, out);
	}

	/* USER STUDY CANDIDATE. */
	@Test
	public void testSailsB() throws Exception {
		String src = "./test/input/user_study/sails-10cd52fe0e8cef3c25fde9130fac1e39a76a2e6f_old.js";
		String dst = "./test/input/user_study/sails-10cd52fe0e8cef3c25fde9130fac1e39a76a2e6f_new.js";
		String out = "./web/sailsb.html";
		runTest(src, dst, out);
	}

	@Test
	public void testDebug() throws Exception {
		String src = "./test/input/user_study/debug_old.js";
		String dst = "./test/input/user_study/debug_new.js";
		String out = "./web/debug.html";
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

}