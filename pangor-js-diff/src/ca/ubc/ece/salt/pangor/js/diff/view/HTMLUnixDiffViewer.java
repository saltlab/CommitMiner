package ca.ubc.ece.salt.pangor.js.diff.view;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.LinkedList;

import ca.ubc.ece.salt.pangor.js.diff.line.DiffMatchPatch;

/**
 * Prints source code as annotated HTML.
 */
public class HTMLUnixDiffViewer {

	/**
	 * Creates an html file annotated with alerts.
	 * @param inputPath The original source code file (text)
	 * @param outputPath The path to write the annotated html file
	 * @param alerts The alerts used to annotate the file
	 * @throws IOException when files cannot be read or written
	 */
	public static void annotate(String srcPath,
						  		String dstPath) throws IOException {

		String srcCode = new String(Files.readAllBytes(Paths.get(srcPath)));
		String dstCode = new String(Files.readAllBytes(Paths.get(dstPath)));

		String[] srcLines = srcCode.split("\n");
		String[] dstLines = dstCode.split("\n");

		String outSrc = "";
		String outDst = "";

		DiffMatchPatch dmp = new DiffMatchPatch();

		LinkedList<DiffMatchPatch.Diff> diffs;
		diffs = dmp.diff_main_line_mode(srcCode, dstCode);

		int i = 0; // Track the line number in the source file.
		int j = 0; // Track the line number in the destination file.

		int sem = 0; // Semaphore for aligning the deleted and inserted lines.

		for (DiffMatchPatch.Diff diff : diffs) {

			/* If this is a DELETE operation, we track the number of lines
			 * removed so we can align the deleted lines with the inserted
			 * lines. */
			if(diff.operation == DiffMatchPatch.Operation.DELETE) {
				if(sem > 0) throw new Error("Invalid state: DELETE operation with sem > 0");
				sem = diff.text.length();
			}

			/* If an EQUALS diff follows a DELETE diff, we need to add blank
			 * lines in the destination file to align the diff. */
			if(diff.operation == DiffMatchPatch.Operation.EQUAL && sem > 0)
				for(; sem > 0; sem--) outDst += "\n";

			System.out.println(diff.operation.toString());

			/* Print the lines. */
			for (int y = 0; y < diff.text.length(); y++) {
			  switch(diff.operation) {
			  case EQUAL:
				  // Print lines from both files side by side
				  i++;
				  j++;
				  outSrc += i + "\t" + srcLines[i-1] + "\n";
				  outDst += j + "\t" + dstLines[j-1] + "\n";
				  break;
			  case DELETE:
				  // Print source line and, if needed, blank lines in destination
				  i++;
				  outSrc += i + "\t" + srcLines[i-1] + "\n";
				  break;
			  case INSERT:
				  // Print destination line and, if needed, blank lines in source
				  j++;
				  outDst += j + "\t" + dstLines[j-1] + "\n";

				  /* Add lines to the source file as needed. */
				  if(sem > 0) sem--;
				  else outSrc += "\n";

				  break;
			  }
			}

			/* Add lines to the destination file as needed. */
			if(diff.operation == DiffMatchPatch.Operation.INSERT)
				for(; sem > 0; sem--)
					outDst += "\n";

		}


		Files.write(Paths.get("./output/unixDiffSrc"), outSrc.getBytes(), StandardOpenOption.CREATE);
		Files.write(Paths.get("./output/unixDiffDst"), outDst.getBytes(), StandardOpenOption.CREATE);

	}

}
