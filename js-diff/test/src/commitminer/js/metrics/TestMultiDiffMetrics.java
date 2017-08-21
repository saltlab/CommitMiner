package commitminer.js.metrics;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.junit.Test;

import commitminer.analysis.Commit;
import commitminer.analysis.CommitAnalysis;
import commitminer.analysis.SourceCodeFileChange;
import commitminer.analysis.Commit.Type;
import commitminer.analysis.annotation.AnnotationFactBase;
import commitminer.analysis.annotation.AnnotationMetricsPostprocessor;
import commitminer.analysis.factories.ICommitAnalysisFactory;
import commitminer.analysis.options.Options;
import commitminer.js.diff.factories.CommitAnalysisFactoryAnnotationMetrics;

public class TestMultiDiffMetrics {

	/**
	 * Tests data mining data set construction.
	 * @param args The command line arguments (i.e., old and new file names).
	 * @throws Exception
	 */
	protected void runTest(
			String src, String dst, String out) throws Exception {
		
		/* For comparison. */
		AnnotationFactBase gumtreeFactBase;
		AnnotationFactBase meyersFactBase;

		/* Set the options for this run. */
		Options options = Options.createInstance(Options.DiffMethod.GUMTREE, Options.ChangeImpact.DEPENDENCIES);

		/* Read the source files. */
		SourceCodeFileChange sourceCodeFileChange = getSourceCodeFileChange(src, dst);

		/* Build the dummy commit. */
		Commit commit = getCommit();
		commit.addSourceCodeFileChange(sourceCodeFileChange);

		/* Set up the analysis. */
		ICommitAnalysisFactory commitFactory = new CommitAnalysisFactoryAnnotationMetrics(Options.ChangeImpact.DEPENDENCIES);
		CommitAnalysis commitAnalysis = commitFactory.newInstance();

		/* Run the meyers analysis. */
		options.setDiffMethod(Options.DiffMethod.MEYERS);
		commitAnalysis.analyze(commit);
		meyersFactBase = AnnotationFactBase.getInstance(sourceCodeFileChange);

		/* Run the gumtree analysis. */
		AnnotationFactBase.removeInstance(sourceCodeFileChange);
		options.setDiffMethod(Options.DiffMethod.GUMTREE);
		commitAnalysis.analyze(commit);
		gumtreeFactBase = AnnotationFactBase.getInstance(sourceCodeFileChange);

		/* Write metrics to a file. */
		AnnotationMetricsPostprocessor postProc = new AnnotationMetricsPostprocessor(out);
		postProc.writeHeader();
		postProc.process(commit, sourceCodeFileChange, gumtreeFactBase, meyersFactBase);
		
	}
	
	@Test
	public void testPM2() throws Exception {
	
		String src = "./test/input/diff/pm2_old.js";
		String dst = "./test/input/diff/pm2_new.js";
		String out = "./output/metrics/pm2.metrics";

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