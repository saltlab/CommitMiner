package commitminer.diff.line;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.Set;
import java.util.TreeSet;

import com.github.gumtreediff.tree.TreeContext;

/**
 * Classifies {@code TreeNode}s using Meyers diff. This should used in place  of
 * the Gumtree Matcher for empirical evaluation of change impact analysis.
 */
public class LineMatcher {
	
	String srcCode;
	String dstCode;
	TreeContext srcTree;
	TreeContext dstTree;

	public LineMatcher(String srcCode, String dstCode, 
					  TreeContext srcTree, TreeContext dstTree) {
		this.srcCode = srcCode;
		this.dstCode = dstCode;
		this.srcTree = srcTree;
		this.dstTree = dstTree;
	}
	
	public void match() {
		
		Set<Range> deleted = new TreeSet<Range>();
		Set<Range> inserted = new TreeSet<Range>();
		
		String[] srcLines = srcCode.split("\n");
		String[] dstLines = dstCode.split("\n");

		DiffMatchPatch dmp = new DiffMatchPatch();

		LinkedList<DiffMatchPatch.Diff> diffs;
		diffs = dmp.diff_main_line_mode(srcCode, dstCode);

		int i = 0; // Track the line number in the source file.
		int j = 0; // Track the line number in the destination file.
		
		int srcAbs = 0; // Track the absolute position in the source file.
		int dstAbs = 0; // Track the absolute position in the destination file.

		for (DiffMatchPatch.Diff diff : diffs) {

			System.out.println(diff.operation.toString());
			
			/* Print the lines. */
			for (int y = 0; y < diff.text.length(); y++) {
				switch(diff.operation) {
				case EQUAL:
					// Increment the absolute position of the source and destination files.
					i++;
					j++;
					System.out.println("Equal src range: " + srcAbs + " - " + (srcAbs + srcLines[i-1].length()));
					System.out.println("Equal dst range: " + dstAbs + " - " + (dstAbs + dstLines[j-1].length()));
					srcAbs += srcLines[i-1].length() + 1;
					dstAbs += dstLines[j-1].length() + 1;
				  break;
			  	case DELETE:
			  		// Increment the absolute position of the source file.
			  		// TODO: Mark this range as deleted.
			  		i++;
					System.out.println("Deleted src range: " + srcAbs + " - " + (srcAbs + srcLines[i-1].length()));
					deleted.add(new Range(srcAbs, srcAbs + srcLines[i-1].length()));
					srcAbs += srcLines[i-1].length() + 1;
					break;
			  	case INSERT:
					// Increment the absolute position of the destination file.
					// TODO: Mark this range as inserted.
					j++;
					System.out.println("Inserted dst range: " + dstAbs + " - " + (dstAbs + dstLines[j-1].length()));
					inserted.add(new Range(dstAbs, dstAbs + dstLines[j-1].length()));
					dstAbs += dstLines[j-1].length() + 1;
					break;
				}

			}

		}
		
		System.out.println("5? " + inserted.contains(new Value(5)));
		System.out.println("47? " + inserted.contains(new Value(44)));
		System.out.println("65? " + inserted.contains(new Value(65)));

	}

}
