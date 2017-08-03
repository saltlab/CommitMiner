package commitminer.diff.line;

import java.util.LinkedList;
import java.util.Queue;
import java.util.Set;
import java.util.TreeSet;

import com.github.gumtreediff.actions.TreeClassifier;
import com.github.gumtreediff.matchers.Matcher;
import com.github.gumtreediff.tree.ITree;
import com.github.gumtreediff.tree.TreeContext;

/**
 * Classifies {@code TreeNode}s using Meyers diff. This should used in place  of
 * the Gumtree Matcher for empirical evaluation of change impact analysis.
 */
public class LineMatcher extends TreeClassifier{
	
	String srcCode;
	String dstCode;

	Set<Range> deleted;
	Set<Range> inserted;

	public LineMatcher(String srcCode, String dstCode, 
					  TreeContext srcTree, TreeContext dstTree,
					  Matcher matcher) {
		super(srcTree, dstTree, matcher);
		this.srcCode = srcCode;
		this.dstCode = dstCode;
		this.deleted = new TreeSet<Range>();
		this.inserted = new TreeSet<Range>();
	}
	
	@Override
	public void classify() {

		/* Use Meyers-diff to get the ranges of inserted and deleted lines. */
		getModifiedRanges();

		/* Classify AST nodes with the Meyers-diff. */
		classify(src, deleted, srcDelTrees);
		classify(dst, inserted, dstAddTrees);

	}
	
	/**
	 * Traverse the tree and classify the nodes into the given set.
	 * @param treeContext The context of the tree to explore.
	 * @param ranges The ranges of modified values.
	 * @param modified The set of modified values (will be populated).
	 */
	private void classify(TreeContext treeContext, Set<Range> ranges, Set<ITree> modified) {

		ITree root = treeContext.getRoot();
		Queue<ITree> queue = new LinkedList<ITree>();
		queue.addAll(root.getChildren());
		
		while(!queue.isEmpty()) {
			ITree node = queue.remove();
			
			if(ranges.contains(new Value(node.getPos()))) {
				modified.add(node);
			}
			
			for(ITree child : node.getChildren()) {
				queue.add(child);
			}
		}
		
	}
	
	/**
	 * Populate the {@code deleted} and {@code inserted} range maps.
	 */
	private void getModifiedRanges() {

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

			/* Print the lines. */
			for (int y = 0; y < diff.text.length(); y++) {
				switch(diff.operation) {
				case EQUAL:
					// Increment the absolute position of the source and destination files.
					i++;
					j++;
					if(srcLines.length >= i)
						srcAbs += srcLines[i-1].length() + 1;
					if(dstLines.length >= j)
						dstAbs += dstLines[j-1].length() + 1;
				  break;
			  	case DELETE:
			  		// Increment the absolute position of the source file.
			  		// Mark this range as deleted.
			  		i++;
					deleted.add(new Range(srcAbs, srcAbs + srcLines[i-1].length()));
					srcAbs += srcLines[i-1].length() + 1;
					break;
			  	case INSERT:
					// Increment the absolute position of the destination file.
					// Mark this range as inserted.
					j++;
					inserted.add(new Range(dstAbs, dstAbs + dstLines[j-1].length()));
					dstAbs += dstLines[j-1].length() + 1;
					break;
				}

			}

		}
		
	}

}
