package commitminer.js.view.test;


import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

import org.junit.Test;

import commitminer.js.diff.view.HTMLUnixDiffViewer;

public class TestUnixDiffHTMLView {

	@Test
	public void testHelpSearch() throws Exception {

		/* The test files. */
		String srcPath = "./test/input/diff/help-search_old.js";
		String dstPath = "./test/input/diff/help-search_new.js";

		String srcCode = new String(Files.readAllBytes(Paths.get(srcPath)));
		String dstCode = new String(Files.readAllBytes(Paths.get(dstPath)));

		/* Read the source files. */
		String annotated = HTMLUnixDiffViewer.annotate(srcCode, dstCode, dstCode);

		/* Write to a file. */
		Files.write(Paths.get("./output/unixDiff.html"), annotated.getBytes(), StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);

	}

}