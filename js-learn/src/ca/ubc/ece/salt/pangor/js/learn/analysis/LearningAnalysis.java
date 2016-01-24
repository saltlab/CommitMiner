package ca.ubc.ece.salt.pangor.js.learn.analysis;

import java.util.Map;

import org.deri.iris.api.basics.IPredicate;
import org.deri.iris.storage.IRelation;

import ca.ubc.ece.salt.pangor.analysis.Commit;
import ca.ubc.ece.salt.pangor.analysis.DomainAnalysis;
import ca.ubc.ece.salt.pangor.analysis.SourceCodeFileChange;
import ca.ubc.ece.salt.pangor.js.cfg.JavaScriptCFGFactory;

/**
 * Creates a data set for learning bug and repair patterns related to the use
 * of Node.js package APIs. This class will produce one feature vector for each
 * function in the analyzed script with a source and destination function.
 */
public class LearningAnalysis extends DomainAnalysis {

	/**
	 * The maximum change complexity for the file. If the change complexity for
	 * a file is greater than the maximum change complexity, the file is not
	 * analyzed and no feature vectors are generated.
	 */
	private int maxChangeComplexity;

	/**
	 * @param dataSet the manager that stores the feature vectors produced by this analysis.
	 * @param domainAnalyses The domains to extract facts from.
	 */
	public LearningAnalysis(int maxChangeComplexity) {
		super(new LearningSourceCodeFileAnalysis(),
			  new LearningSourceCodeFileAnalysis(),
			  new JavaScriptCFGFactory(), false);
		this.maxChangeComplexity = maxChangeComplexity;
	}

	@Override
	protected boolean preAnalysis(Commit commit, Map<IPredicate, IRelation> facts) throws Exception {
		/* TODO: Measure the number of modified statements in each source code file.
		 * If there are more than `maxChangeComplexity` modified statements,
		 * abort the analysis. */

		/* Iterate through the files in the commit and run the
		 * SourceCodeFileAnalysis on each of them. */
		for(SourceCodeFileChange sourceCodeFileChange : commit.sourceCodeFileChanges) {
			ChangeComplexitySCFA srcComplexity = new ChangeComplexitySCFA();
			ChangeComplexitySCFA dstComplexity = new ChangeComplexitySCFA();
			this.analyzeFile(sourceCodeFileChange, facts, srcComplexity, dstComplexity);
		}

		/* TODO: Check the complexity. */
		return true;
	}

}