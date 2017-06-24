package commitminer.js.diff.test;


import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;

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
import commitminer.js.annotation.Annotation;
import commitminer.js.annotation.AnnotationDataSet;
import commitminer.js.annotation.AnnotationFactBase;
import commitminer.js.diff.DiffCommitAnalysisFactory;

public class EvalUserDiffAnalysis {

	/**
	 * Tests data mining data set construction.
	 * @param args The command line arguments (i.e., old and new file names).
	 * @throws Exception
	 */
	protected void runTest(SourceCodeFileChange sourceFileChange,
						   Set<Annotation> expectedAnnotations,
						   boolean checkSize) throws Exception {

		Commit commit = getCommit();
		commit.addSourceCodeFileChange(sourceFileChange);

		/* Builds the data set with our custom queries. */
		AnnotationDataSet dataSet = new AnnotationDataSet(new AnnotationFactBase(sourceFileChange));

		/* Set up the analysis. */
		ICommitAnalysisFactory commitFactory = new DiffCommitAnalysisFactory(dataSet);
		CommitAnalysis commitAnalysis = commitFactory.newInstance();

		/* Run the analysis. */
		commitAnalysis.analyze(commit);

        /* Print the data set. */
		dataSet.printDataSet();

        /* Verify the expected annotations match the actual annotations. */
		SortedSet<Annotation> actualAnnotations = dataSet.getAnnotationFactBase().getAnnotations();
		for(Annotation expectedAnnotation : expectedAnnotations) {
			Assert.assertTrue(actualAnnotations.contains(expectedAnnotation));
		}

	}

	@Test
	public void testTutorial() throws Exception {

		/* The test files. */
		String src = "./test/input/diff/tutorial_old.js";
		String dst = "./test/input/diff/tutorial_new.js";

		/* Read the source files. */
		SourceCodeFileChange sourceCodeFileChange = getSourceCodeFileChange(src, dst);

		/* Build the expected feature vectors. */
		Set<Annotation> expected = new HashSet<Annotation>();

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

}