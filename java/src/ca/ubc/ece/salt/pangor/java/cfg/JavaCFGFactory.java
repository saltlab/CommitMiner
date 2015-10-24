package ca.ubc.ece.salt.pangor.java.cfg;

import java.util.LinkedList;
import java.util.List;

import org.eclipse.jdt.core.dom.CompilationUnit;

import ca.ubc.ece.salt.gumtree.ast.ClassifiedASTNode;
import ca.ubc.ece.salt.pangor.cfg.CFG;
import ca.ubc.ece.salt.pangor.cfg.CFGFactory;
import fr.labri.gumtree.gen.jdt.JdtTreeGenerator;
import fr.labri.gumtree.io.TreeGenerator;

/**
 * A CFG factory for Java.
 * NOTE: This class only works with the Mozilla Rhino AST.
 */
public class JavaCFGFactory implements CFGFactory {

	/**
	 * @param root The JDT CompilationUnit AST node.
	 */
	@Override
	public List<CFG> createCFGs(ClassifiedASTNode root) {

		/* Check we are working with the correct AST type. */
		if(!(root instanceof CompilationUnit)) throw new IllegalArgumentException("The AST must be parsed from Apache Rhino.");

		@SuppressWarnings("unused")
		CompilationUnit cu = (CompilationUnit)root;

		/* Store the CFGs from all the functions. */
		List<CFG> cfgs = new LinkedList<CFG>();

		return cfgs;
	}

	@Override
	public TreeGenerator getTreeGenerator(String extension) {
		if(acceptsExtension(extension)) return new JdtTreeGenerator();
		return null;
	}

	@Override
	public boolean acceptsExtension(String extension) {
		return extension.equals("java");
	}

}
