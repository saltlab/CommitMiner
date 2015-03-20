package ca.ubc.ece.salt.sdjsb.cfg;

import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

import org.mozilla.javascript.Node;
import org.mozilla.javascript.ast.AstNode;
import org.mozilla.javascript.ast.AstRoot;
import org.mozilla.javascript.ast.Block;
import org.mozilla.javascript.ast.BreakStatement;
import org.mozilla.javascript.ast.ContinueStatement;
import org.mozilla.javascript.ast.ExpressionStatement;
import org.mozilla.javascript.ast.FunctionNode;
import org.mozilla.javascript.ast.IfStatement;
import org.mozilla.javascript.ast.ScriptNode;
import org.mozilla.javascript.ast.SwitchStatement;
import org.mozilla.javascript.ast.TryStatement;
import org.mozilla.javascript.ast.VariableDeclaration;
import org.mozilla.javascript.ast.WithStatement;

/**
 * Builds a control flow graph.
 * @author qhanam
 */
public class CFGFactory {
	
	/**
	 * Builds intra-procedural control flow graphs for the given artifact.
	 * @param root
	 * @return
	 */
	public static List<CFG> createCFGs(AstRoot root) {
		
		/* Store the CFGs from all the functions. */
		List<CFG> cfgs = new LinkedList<CFG>();
		
		/* Start by getting the CFG for the script. */
		CFGNode entry = new ScriptEntryCFGNode(root);
		CFGNode exit = new ScriptExitCFGNode(root);
		
        /* There is one entry point and one exit point for a script and function. */
        CFG cfg = new CFG(entry);
        cfg.addExitNode(exit);
        
        /* Build the CFG for the script. */
        CFG subGraph = CFGFactory.build(root);
        entry.mergeInto(subGraph.getEntryNode());
        subGraph.mergeInto(exit);
        cfgs.add(cfg);
		
		/* Get the list of functions in the script. */
		List<FunctionNode> functions = FunctionNodeVisitor.getFunctions(root);
		
		/* For each function, generate its CFG. */
		for (FunctionNode function : functions) {
			
            /* Start by getting the CFG for the script. */
            entry = new FunctionEntryCFGNode(function);
            exit = new FunctionExitCFGNode(function);
            
            /* There is one entry point and one exit point for a script and function. */
            cfg = new CFG(entry);
            cfg.addExitNode(exit);
            
            /* Build the CFG for the script. */
            subGraph = CFGFactory.build(function);
            entry.mergeInto(subGraph.getEntryNode());
            subGraph.mergeInto(exit);
            cfgs.add(cfg);

		}
		
		return cfgs;
	}
	
	/**
	 * Prints a serial representation of the CFG.
	 * @param node
	 */
	public static String printCFG(CFGNode node) {
		
		if(node instanceof FunctionExitCFGNode || node instanceof ScriptExitCFGNode) {
			return node.toString();
		}
		else if(node instanceof LinearCFGNode) {
			return node.toString() + "->" + CFGFactory.printCFG(((LinearCFGNode) node).getNext());
		}
		
		return "UNKNOWN";
		
	}
	
	/**
	 * Builds a CFG for a block.
	 * @param block The block statement.
	 */
	private static CFG build(Block block) {
		return CFGFactory.buildBlock(block);
	}
	
	/**
	 * Builds a CFG for a script.
	 * @param block The block statement.
	 */
	private static CFG build(AstRoot script) {
		return CFGFactory.buildBlock(script);
	}

	/**
	 * Builds a CFG for a function or script.
	 * @param block The block statement.
	 */
	private static CFG build(FunctionNode function) {
		return CFGFactory.buildSwitch(function.getBody());
	}

	/**
	 * Builds a CFG for a block, function or script.
	 * @param block
	 * @return The CFG for the block.
	 */
	private static CFG buildBlock(AstNode block) {
		/* Special cases:
		 * 	- First statement in block (set entry point for the CFG and won't need to merge previous into it).
		 * 	- Last statement: The exit nodes for the block will be the same as the exit nodes for this statement.
		 */
		
		AstNode firstChild = (AstNode) block.getFirstChild();
		CFG subGraph = CFGFactory.buildSwitch(firstChild);
		CFG cfg = new CFG(subGraph.getEntryNode());
		CFG previous = subGraph;
		
		for(Node statement : block) {
			
			assert(statement instanceof AstNode);

			if(statement == block || statement == block.getFirstChild()) continue;

			subGraph = CFGFactory.buildSwitch((AstNode)statement);
			previous.mergeInto(subGraph.getEntryNode());
			previous = subGraph;
			
		}
		
		cfg.setExitNodes(subGraph.getExitNodes());
		
		return cfg;
	}

	/**
	 * Builds a control flow subgraph for a statement.
	 * @param entry The entry point for the subgraph.
	 * @param exit The exit point for the subgraph.
	 * @return A list of exit nodes for the subgraph.
	 */
	private static CFG build(AstNode statement) {
		
		CFGNode node = new LinearCFGNode(statement);
		CFG cfg = new CFG(node);
		cfg.addExitNode(node);
		return cfg;

	}
	
	/**
	 * Calls the appropriate build method for the node type.
	 */
	private static CFG buildSwitch(AstNode node) {

		if (node instanceof Block) {
			return CFGFactory.build((Block) node);
		} else {
			return CFGFactory.build(node);
		}

	}
	
	/**
	 * Check if an AstNode is a statement.
	 * @param node
	 * @return
	 */
	private static boolean isStatement(Node node) {

		if(node instanceof VariableDeclaration ||
			node instanceof TryStatement || 
			node instanceof IfStatement ||
			node instanceof WithStatement ||
			node instanceof BreakStatement ||
			node instanceof ContinueStatement ||
			node instanceof SwitchStatement ||
			node instanceof ExpressionStatement) {
			return true;
		}

		return false;
	}

}