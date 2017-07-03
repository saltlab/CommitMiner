package commitminer.js.view.test;


import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

import org.junit.Test;

import commitminer.js.diff.view.HTMLUnixDiffViewer;

public class TestUnixDiffHTMLView {
	
	private static void runTest(String srcPath, String dstPath) throws IOException {

		String srcCode = new String(Files.readAllBytes(Paths.get(srcPath)));
		String dstCode = new String(Files.readAllBytes(Paths.get(dstPath)));

		/* Read the source files. */
		String annotated = HTMLUnixDiffViewer.annotate(srcCode, dstCode, dstCode);

		/* Write to a file. */
		Files.write(Paths.get("./output/unixDiff.html"), annotated.getBytes(), StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
		
	}

	@Test
	public void test() throws Exception {
		runTest("./test/input/diff/env_old.js", "./test/input/diff/env_new.js");
	}

	@Test
	public void testHelpSearch() throws Exception {
		runTest("./test/input/diff/help-search_old.js", "./test/input/diff/help-search_new.js");
	}

}