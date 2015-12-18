package ca.ubc.ece.salt.pangor.java.cfg;

import java.util.LinkedList;
import java.util.List;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.EmptyStatement;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.Statement;

import ca.ubc.ece.salt.gumtree.ast.ClassifiedASTNode;
import ca.ubc.ece.salt.pangor.cfg.CFG;
import ca.ubc.ece.salt.pangor.cfg.CFGFactory;
import ca.ubc.ece.salt.pangor.cfg.CFGNode;
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

		CompilationUnit cu = (CompilationUnit)root;

		/* Store the CFGs from all the methods. */
		List<CFG> cfgs = new LinkedList<CFG>();

		/* Get the list of methods in the class. */
//		List<MethodDeclaration> methods = MethodVisitor.getMethodDeclarations(cu);

		/* For each function, generate its CFG. */
//		for(MethodDeclaration method : methods) {
//			cfgs.add(JavaCFGFactory.buildMethodCFG(method));
//		}

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

	/**
	 * Builds a CFG from a method.
	 * @param method A MethodDelcaration AST node.
	 * @return The complete intra-procedural CFG for the method.
	 */
	private static CFG buildMethodCFG(MethodDeclaration method) {

		/* Set the entry and exit points for the CFG. */
		CFGNode methodEntry = new CFGNode(method, "METHOD_ENTRY");
		CFGNode methodExit = new CFGNode(new EmptyStatement(method.getAST()), "METHOD_EXIT");

		/* Build the CFG for the method. */
		CFG cfg = new CFG(methodEntry);
		cfg.addExitNode(methodExit);

		/* Build the CFG subgraph for the method body. */
		CFG subGraph = JavaCFGFactory.build(method);

		if(subGraph == null) {
			CFGNode emptyEntry = new CFGNode(new EmptyStatement(method.getAST()));
			CFGNode emptyExit = new CFGNode(new EmptyStatement(method.getAST()));
			subGraph = new CFG(emptyEntry);
			subGraph.addExitNode(emptyExit);
		}

		/* The next node in the graph is the first node of the subgraph. */
		methodEntry.addEdge(null, subGraph.getEntryNode());

		/* Merge the subgraph's exit nodes into the method exit. */
		for(CFGNode exitNode : subGraph.getExitNodes()) {
			exitNode.addEdge(null, methodExit);
		}

		/* The return nodes should point to the method exit. */
		for(CFGNode returnNode : subGraph.getReturnNodes()) {
			returnNode.addEdge(null, methodExit);
		}

		return cfg;

	}

	/**
	 * Builds a CFG for a method.
	 */
	private static CFG build(MethodDeclaration method) {
		return JavaCFGFactory.buildSwitch(method.getBody());
	}

	/**
	 * Builds a CFG for a block.
	 */
	@SuppressWarnings("unchecked")
	private static CFG build(Block block) {

		/* Special cases:
		 * 	- First statement in block (set entry point for the CFG and won't need to merge previous into it).
		 * 	- Last statement: The exit nodes for the block will be the same as the exit nodes for this statement.
		 */

		CFG cfg = null;
		CFG previous = null;

		for(Statement statement : (List<Statement>)block.statements()) {

			CFG subGraph = JavaCFGFactory.buildSwitch(statement);

			if(subGraph != null) {

				if(previous == null) {
					/* The first subgraph we find is the entry point to this graph. */
					cfg = new CFG(subGraph.getEntryNode());
				}
				else {
					/* Merge the previous subgraph into the entry point of this subgraph. */
					for(CFGNode exitNode : previous.getExitNodes()) {
						exitNode.addEdge(null, subGraph.getEntryNode());
					}
				}

				/* Propagate return, continue, break and throw nodes. */
				cfg.addAllReturnNodes(subGraph.getReturnNodes());
				cfg.addAllBreakNodes(subGraph.getBreakNodes());
				cfg.addAllContinueNodes(subGraph.getContinueNodes());
				cfg.addAllThrowNodes(subGraph.getThrowNodes());

				previous = subGraph;

			}

		}

		if(previous != null) {
			/* Propagate exit nodes from the last node in the block. */
			cfg.addAllExitNodes(previous.getExitNodes());
		}

		return cfg;

	}

	/**
	 * Builds a control flow subgraph for an expression statement (i.e. one
	 * that does not affect control flow). If all control flow statement types
	 * are handled properly then this should only be an
	 * {@code ExpressionStatement}.
	 * @param statement The expression or unhandled control flow statement.
	 * @return The CFG subgraph for this statement (i.e., the statement itself
	 * 		   as a CFG).
	 */
	private static CFG build(ASTNode statement) {

		CFGNode expressionNode = new CFGNode(statement);
		CFG cfg = new CFG(expressionNode);
		cfg.addExitNode(expressionNode);
		return cfg;

	}

	/**
	 * Calls the appropriate build method for the node type.
	 */
	private static CFG buildSwitch(ASTNode node) {

		if(node == null) return null;

		if(node instanceof Block) {
			return JavaCFGFactory.build((Block)node);
		} else {
			return JavaCFGFactory.build(node);
		}

	}

}
