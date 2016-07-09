package ca.ubc.ece.salt.pangor.cfg;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Stack;

import ca.ubc.ece.salt.gumtree.ast.ClassifiedASTNode;
import fr.labri.gumtree.io.TreeGenerator;

/**
 * Builds a CFG given some AST.
 *
 * Classes that implement this interface will use one specific parser and
 * will therefore have an implementation for a specific AST.
 */
public interface CFGFactory {

	/**
	 * Builds intra-procedural control flow graphs for the given artifact.
	 * @param root The class or script to build CFGs for.
	 * @param astClassifier Generates unique IDs for AST nodes.
	 * @return One CFG for each function in the class or script.
	 */
	List<CFG> createCFGs(ClassifiedASTNode root);

	/**
	 * @param The file extension of the file that needs to be parsed.
	 * @return The GumTree Tree generator (AST parser) for the file extension.
	 */
	TreeGenerator getTreeGenerator(String extension);

	/**
	 * @param extension The source code file extension.
	 * @return true if the CFGFactory accepts the type of source code file
	 * specified by the extension.
	 */
	boolean acceptsExtension(String extension);

	/**
	 * Helper function for counting the number of incoming edges for each
	 * {@code CFGNode}.
	 * @param cfg The CFG to count and update. It should not have had its
	 * 			  incoming edges counted yet.
	 */
	static void countIncommingEdges(CFG cfg) {

		/* Track which nodes have allready been visited. */
		Set<CFGNode> visited = new HashSet<CFGNode>();

		/* Initialize the stack for a depth-first traversal. */
		Stack<CFGNode> stack = new Stack<CFGNode>();
		stack.add(cfg.getEntryNode());
		visited.add(cfg.getEntryNode());

		while(!stack.isEmpty()) {

			CFGNode current = stack.pop();

			/* 1. Increment the number of incoming edges at the destination
			 * 	  nodes of each outgoing edge.
			 * 2. Add all unvisited nodes to the stack. */
			for(CFGEdge edge : current.getEdges()) {
				edge.getTo().incrementIncommingEdges();
				if(visited.contains(edge.getTo())) {
					stack.push(edge.getTo());
					visited.add(edge.getTo());
				}
			}

		}

	}

}
