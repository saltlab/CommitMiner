package ca.ubc.ece.salt.pangor.analysis.simple;

import java.util.List;

import ca.ubc.ece.salt.gumtree.ast.ClassifiedASTNode;
import ca.ubc.ece.salt.pangor.analysis.SourceCodeFileAnalysis;
import ca.ubc.ece.salt.pangor.batch.Commit;
import ca.ubc.ece.salt.pangor.cfg.CFG;

/**
 * Performs a simple analysis of a source code file. The analysis lists all
 * member variables and methods in the source code file.
 *
 * NOTE: this analysis only works with Java.
 */
public class SimpleSourceCodeFileAnalysis extends SourceCodeFileAnalysis {

	public SimpleSourceCodeFileAnalysis(Commit commit) {
		super(commit);
	}

	@Override
	public void analyze(ClassifiedASTNode root, List<CFG> cfgs) throws Exception {
		root.getTypeName();
		this.addPattern(new SimplePattern(root.getTypeName()));
	}

}
