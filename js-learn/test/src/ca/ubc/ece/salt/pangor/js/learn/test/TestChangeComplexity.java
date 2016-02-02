package ca.ubc.ece.salt.pangor.js.learn.test;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;

import org.apache.commons.io.FileUtils;
import org.deri.iris.api.basics.IPredicate;
import org.deri.iris.storage.IRelation;
import org.junit.Test;

import ca.ubc.ece.salt.pangor.analysis.SourceCodeFileChange;
import ca.ubc.ece.salt.pangor.cfd.CFDContext;
import ca.ubc.ece.salt.pangor.cfd.ControlFlowDifferencing;
import ca.ubc.ece.salt.pangor.js.cfg.JavaScriptCFGFactory;
import ca.ubc.ece.salt.pangor.js.learn.analysis.ChangeComplexitySCFA;
import ca.ubc.ece.salt.pangor.js.learn.analysis.ChangeComplexityVisitor.ChangeComplexity;

public class TestChangeComplexity {

	/**
	 * Runs one test on the ChangeComplexity source code file analysis.
	 * @param src The buggy file.
	 * @param dst The repaired file.
	 * @param removed The number of removed statements.
	 * @param inserted The number of inserted statements.
	 * @param updated The number of updated statements.
	 * @param print If true, prints the number of modified statements.
	 * @throws Exception
	 */
	private void runTest(String src, String dst, int removed, int inserted, int updated, boolean print) throws Exception {

		/* Read the source files. */
		SourceCodeFileChange sourceCodeFileChange = getSourceCodeFileChange(src, dst);

		/* Do the differencing. */
		JavaScriptCFGFactory factory = new JavaScriptCFGFactory();
		String[] args =  new String[] {sourceCodeFileChange.buggyFile, sourceCodeFileChange.repairedFile};
		ControlFlowDifferencing cfd = new ControlFlowDifferencing(factory, args,
																  sourceCodeFileChange.buggyCode,
																  sourceCodeFileChange.repairedCode);
		CFDContext context = cfd.getContext();

		/* Analyze the file. */
		ChangeComplexitySCFA srcAnalysis = new ChangeComplexitySCFA(false);
		srcAnalysis.analyze(sourceCodeFileChange,
						 new HashMap<IPredicate, IRelation>(),
						 context.srcScript, null);

		ChangeComplexitySCFA dstAnalysis = new ChangeComplexitySCFA(true);
		dstAnalysis.analyze(sourceCodeFileChange,
						 new HashMap<IPredicate, IRelation>(),
						 context.dstScript, null);

		ChangeComplexity srcCC = srcAnalysis.getChangeComplexity();
		ChangeComplexity dstCC = dstAnalysis.getChangeComplexity();

		/* Print the results. */
		if(print) {
			System.out.println("Removed: " + srcCC.removedStatements);
			System.out.println("Inserted: " + dstCC.insertedStatements);
			System.out.println("Updated: " + dstCC.updatedStatements);
		}

		/* Check the results. */
		org.junit.Assert.assertEquals("# removed does not match", removed, srcCC.removedStatements);
		org.junit.Assert.assertEquals("# inserted does not match", inserted, dstCC.insertedStatements);
		org.junit.Assert.assertEquals("# updated does not match", updated, dstCC.updatedStatements);

	}

	@Test
	public void testInsert() throws Exception {

		String src = "./test/input/learning/date_old.js";
		String dst = "./test/input/learning/date_new.js";

		this.runTest(src, dst, 0, 3, 0, true);

	}

	@Test
	public void testRemove() throws Exception {

		String src = "./test/input/learning/date_new.js";
		String dst = "./test/input/learning/date_old.js";

		this.runTest(src, dst, 3, 0, 0, true);

	}

	@Test
	public void testUpdate() throws Exception {

		String src = "./test/input/learning/var_old.js";
		String dst = "./test/input/learning/var_new.js";

		this.runTest(src, dst, 0, 0, 1, true);

	}

	@Test
	public void testActual() throws Exception {

		String src = "./test/input/special_type_handling/Common_old.js";
		String dst = "./test/input/special_type_handling/Common_new.js";

		this.runTest(src, dst, 7, 10, 2, true);

	}

	@Test
	public void testGruntUpdated() throws Exception {
		String src = "./test/input/complexity/grunt_old.js";
		String dst = "./test/input/complexity/grunt_new.js";

		this.runTest(src, dst, 1, 0, 0, true);
	}

	@Test
	public void testMediaCenterJS() throws Exception {
		String src = "./test/input/complexity/plugin-functions_old.js";
		String dst = "./test/input/complexity/plugin-functions_new.js";

		this.runTest(src, dst, 0, 2, 0, true);
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
