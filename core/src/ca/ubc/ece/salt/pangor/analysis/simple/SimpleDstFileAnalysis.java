package ca.ubc.ece.salt.pangor.analysis.simple;

import java.util.List;

import ca.ubc.ece.salt.gumtree.ast.ClassifiedASTNode;
import ca.ubc.ece.salt.pangor.analysis.Facts;
import ca.ubc.ece.salt.pangor.analysis.SourceCodeFileAnalysis;
import ca.ubc.ece.salt.pangor.cfg.CFG;

/**
 * Performs a simple analysis of a source code file. The analysis registers
 * a pattern for each file, which is the type name of the root node.
 */
public class SimpleDstFileAnalysis extends SourceCodeFileAnalysis<SimpleAlert> {

	public SimpleDstFileAnalysis() { }

	@Override
	public void analyze(Facts<SimpleAlert> facts, ClassifiedASTNode root, List<CFG> cfgs) throws Exception {
		String pattern = "Destination Root: " + root.getASTNodeType();
		facts.addPattern(new SimplePattern(pattern));
		facts.addPreCondition(new SimplePattern(pattern));
	}

}
