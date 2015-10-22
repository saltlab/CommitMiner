package ca.ubc.ece.salt.pangor.analysis.simple;

import java.util.LinkedList;
import java.util.List;

import ca.ubc.ece.salt.gumtree.ast.ClassifiedASTNode;
import ca.ubc.ece.salt.pangor.cfg.CFG;
import ca.ubc.ece.salt.pangor.cfg.CFGFactory;

/**
 * NOTE: This class is for testing only.
 */
public class SimpleCFGFactory implements CFGFactory {

	@Override
	public List<CFG> createCFGs(ClassifiedASTNode root) {

		/* Store the CFGs from all the functions. */
		List<CFG> cfgs = new LinkedList<CFG>();

		return cfgs;
	}

	@Override
	public boolean acceptsExtension(String extension) {

		/* GumTree should be able to parse ASTs for .java and .js files. */
		return (extension.equals("java") || extension.equals("js"));

	}

}
