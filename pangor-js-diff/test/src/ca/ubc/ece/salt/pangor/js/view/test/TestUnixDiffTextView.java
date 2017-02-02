package ca.ubc.ece.salt.pangor.js.view.test;


import org.junit.Test;

import ca.ubc.ece.salt.pangor.js.diff.view.HTMLUnixDiffViewer;

public class TestUnixDiffTextView {

	@Test
	public void testHelpSearch() throws Exception {

		/* The test files. */
		String src = "./test/input/diff/help-search_old.js";
		String dst = "./test/input/diff/help-search_new.js";

		/* Read the source files. */
		HTMLUnixDiffViewer.annotate(src, dst);

	}

}