package ca.ubc.ece.salt.sdjsb;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;

import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.mozilla.javascript.ast.AstNode;

import fr.labri.gumtree.actions.RootAndLeavesClassifier;
import fr.labri.gumtree.actions.TreeClassifier;
import fr.labri.gumtree.client.DiffOptions;
import fr.labri.gumtree.gen.js.RhinoTreeGenerator;
import fr.labri.gumtree.io.ParserASTNode;
import fr.labri.gumtree.matchers.MappingStore;
import fr.labri.gumtree.matchers.Matcher;
import fr.labri.gumtree.matchers.MatcherFactories;
import fr.labri.gumtree.tree.Tree;
import ca.ubc.ece.salt.sdjsb.checker.Alert;
import ca.ubc.ece.salt.sdjsb.checker.CheckerRegistry;

public class SDJSB  {
	
	private CheckerRegistry checkerRegistry;
	private DiffOptions diffOptions;

	/**
	 * The main entry point for command line executions of SDJSB.
	 * @param args SDJSB /path/to/src /path/to/dst
	 */
	public static void main(String[] args) {
		DiffOptions options = new DiffOptions();
		CmdLineParser parser = new CmdLineParser(options);

		try {
			parser.parseArgument(args);
		} catch (CmdLineException e) {
			System.err.println("Usage:\nSDJSB /path/to/src /path/to/dst");
			e.printStackTrace();
			return;
		}

        SDJSB client = new SDJSB(options);
        List<Alert> alerts = client.start();

        System.out.println("Alerts:");
        for(Alert alert : alerts){
            System.out.println("\t" + alert.getLongDescription());
        }
	}

	public SDJSB(DiffOptions diffOptions) {
		this.diffOptions = diffOptions;
	}

	public List<Alert> start() {
		/* Get the files we are comparing from the command line arguments. */
		File fSrc = new File(diffOptions.getSrc());
		File fDst = new File(diffOptions.getDst());

        /* Create the abstract GumTree representations of the ASTs.
         * 
         * Note: GumTree would use TreeGeneratorRegistry here to build the src
         * and dst trees. However, we're working with the JavaScript AstNodes
         * from the Rhino parser, so we need some language specific info from
         * RhinoTreeGenerator. */
        Tree src;
        Tree dst;
        Map<AstNode, Tree> srcTreeNodeMap;
        Map<AstNode, Tree> dstTreeNodeMap;
        RhinoTreeGenerator srcRhinoTreeGenerator = new RhinoTreeGenerator();
        RhinoTreeGenerator dstRhinoTreeGenerator = new RhinoTreeGenerator();
        try{
            src = srcRhinoTreeGenerator.fromFile(fSrc.getAbsolutePath());
            srcTreeNodeMap = srcRhinoTreeGenerator.getTreeNodeMap();
            dst = dstRhinoTreeGenerator.fromFile(fDst.getAbsolutePath());
            dstTreeNodeMap = dstRhinoTreeGenerator.getTreeNodeMap();
        } catch (IOException e) {
        	System.err.println(e.getMessage());
        	return null;
        }
		
		/* Match the source AST nodes to the destination AST nodes. The default
		 * algorithm for doing this is the GumTree algorithm. */
		Matcher matcher = MatcherFactories.newMatcher(src, dst);
		matcher.match();
		
		/* Produce the diff object that we will use to infer properties of
		 * repairs. */
		try{
            this.produce(src, dst, srcTreeNodeMap, dstTreeNodeMap, matcher);
		} catch (IOException e) {
        	System.err.println(e.getMessage());
        	return null;
		}
		
		/* Get and print the alerts. */
		List<Alert> alerts = this.checkerRegistry.getAlerts();
		
		return alerts;
	}
	
	/**
	 * Computes the set of changes that transforms the source AST into the
	 * destination AST. Each change is one of {delete, add, move, update}.
	 * @param src The GumTree AST generated for the source file.
	 * @param dst The GumTree AST generated for the destination file.
	 * @param matcher The set of source nodes matched to destination nodes.
	 * @throws IOException
	 */
	private void produce(Tree src, Tree dst, Map<AstNode, Tree> srcTreeNodeMap, Map<AstNode, Tree> dstTreeNodeMap, Matcher matcher) throws IOException {
		
		/* Classify parts of each tree as deleted, added, moved or updated. The
		 * source tree nodes can be deleted or updated, while the destination
		 * tree nodes can be added, moved or updated. Moved and deleted nodes
		 * are mapped from the source tree to the destination tree. 
		 * 
		 * The classified nodes are stored in hash maps:
		 *  getSrcDeleteTrees() - gets the map containing all delete ops.
		 * 	get[Src|Dst]MvTrees() - gets the map containing all move operations.
		 *  get[Src|Dst]UpdateTrees() - gets the map containing all update ops.
		 *  getDstAddTrees() - gets the map containing all */
		TreeClassifier c = new RootAndLeavesClassifier(src, dst, matcher);

		/* We use mapping ids to keep track of mapping changes from the source
		 * to the destination. */
		MappingStore mappings = matcher.getMappings();

		/* Create the 'event bus' for the repair checkers. */
		this.checkerRegistry = new CheckerRegistry(srcTreeNodeMap, dstTreeNodeMap, c);
		
		/* Iterate the source tree. Call the CheckerRegistry to trigger events. */
		for (Tree t: src.getTrees()) {
			if (c.getSrcMvTrees().contains(t)) {
				ParserASTNode<AstNode> parserNode = t.getASTNode(); // TODO: We should pass the source AND destination nodes.
				this.checkerRegistry.sourceMove(parserNode.getASTNode());
			} if (c.getSrcUpdTrees().contains(t)) {
				ParserASTNode<AstNode> parserNode = t.getASTNode();
				this.checkerRegistry.sourceUpdate(parserNode.getASTNode());
			} if (c.getSrcDelTrees().contains(t)) {
				ParserASTNode<AstNode> parserNode = t.getASTNode();
				this.checkerRegistry.sourceDelete(parserNode.getASTNode());
			}
		}

		/* Iterate the destination tree. Call the CheckerRegistry to trigger events. */
		for (Tree t: dst.getTrees()) {
			if (c.getDstMvTrees().contains(t)) {
				ParserASTNode<AstNode> parserNode = t.getASTNode();
				this.checkerRegistry.destinationMove(parserNode.getASTNode());
			} if (c.getDstUpdTrees().contains(t)) {
				ParserASTNode<AstNode> parserNode = t.getASTNode();
				this.checkerRegistry.destinationUpdate(parserNode.getASTNode());
			} if (c.getDstAddTrees().contains(t)) {
				ParserASTNode<AstNode> parserNode = t.getASTNode();
				this.checkerRegistry.destinationInsert(parserNode.getASTNode());
			}
		}
		
		/* Trigger the finished event. */
		this.checkerRegistry.finished();
		
	}

}