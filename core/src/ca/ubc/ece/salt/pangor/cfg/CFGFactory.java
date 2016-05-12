package ca.ubc.ece.salt.pangor.cfg;

import java.util.List;

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

}
