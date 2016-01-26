package ca.ubc.ece.salt.pangor.js.learn.analysis;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.deri.iris.api.basics.IPredicate;
import org.deri.iris.storage.IRelation;

import ca.ubc.ece.salt.pangor.analysis.Commit;
import ca.ubc.ece.salt.pangor.analysis.DomainAnalysis;
import ca.ubc.ece.salt.pangor.analysis.SourceCodeFileAnalysis;
import ca.ubc.ece.salt.pangor.analysis.SourceCodeFileChange;
import ca.ubc.ece.salt.pangor.js.analysis.FunctionAnalysis;
import ca.ubc.ece.salt.pangor.js.analysis.ScriptAnalysis;
import ca.ubc.ece.salt.pangor.js.cfg.JavaScriptCFGFactory;

/**
 * An analysis for extracting facts in the learning domain.
 *
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
	private LearningAnalysis(SourceCodeFileAnalysis srcSCFA,
							SourceCodeFileAnalysis dstSCFA,
							int maxChangeComplexity) {
		super(srcSCFA, dstSCFA, new JavaScriptCFGFactory(), false);
		this.maxChangeComplexity = maxChangeComplexity;
	}

	@Override
	protected boolean preAnalysis(Commit commit, Map<IPredicate, IRelation> facts) throws Exception {
		/* Measure the number of modified statements in each source code file.
		 * If there are more than `maxChangeComplexity` modified statements,
		 * abort the analysis. */

		/* Iterate through the files in the commit and run the
		 * SourceCodeFileAnalysis on each of them. */
		int complexity = 0;
		for(SourceCodeFileChange sourceCodeFileChange : commit.sourceCodeFileChanges) {

			/* Get the file extension. */
			String fileExtension = getSourceCodeFileExtension(sourceCodeFileChange.buggyFile, sourceCodeFileChange.repairedFile);

			/* Check the file extension. */
			if(fileExtension != null && cfgFactory.acceptsExtension(fileExtension)) {

				ChangeComplexitySCFA srcComplexity = new ChangeComplexitySCFA();
				ChangeComplexitySCFA dstComplexity = new ChangeComplexitySCFA();

				/* Compute the change complexity for this file.
				 * NOTE: If an exception occurs while analyzing the file, no results
				 * 		 will be returned and the complexity will not be correct. */
				this.analyzeFile(sourceCodeFileChange, facts, srcComplexity, dstComplexity);

				/* Update the total change complexity. */
				if(srcComplexity.getChangeComplexity() != null) {
					complexity += srcComplexity.getChangeComplexity().removedStatements;
				}
				if(dstComplexity.getChangeComplexity() != null) {
					complexity += dstComplexity.getChangeComplexity().insertedStatements;
					complexity += dstComplexity.getChangeComplexity().updatedStatements;
				}

			}

		}

		/* The commit is small enough to be repetitive. Analyze the commit. */
		if(complexity <= this.maxChangeComplexity) return true;

		/* The change is too complex. Do not analyze the file. */
		return false;
	}

	/**
	 * Builds a new {@code LearningAnalysis}
	 * @param maxChangeComplexity The maximum number of statements that can change in a commit.
	 * @return an analysis for extracting facts in the learning domain.
	 */
	public static LearningAnalysis createLearningAnalysis(int maxChangeComplexity) {

		List<FunctionAnalysis> srcFunctionAnalyses = new LinkedList<FunctionAnalysis>();
		List<FunctionAnalysis> dstFunctionAnalyses = new LinkedList<FunctionAnalysis>();

		srcFunctionAnalyses.add(new LearningFunctionAnalysis(false));
		dstFunctionAnalyses.add(new LearningFunctionAnalysis(true));

		SourceCodeFileAnalysis srcSCFA = new ScriptAnalysis(srcFunctionAnalyses);
		SourceCodeFileAnalysis dstSCFA = new ScriptAnalysis(dstFunctionAnalyses);

		LearningAnalysis analysis = new LearningAnalysis(srcSCFA, dstSCFA,
														 maxChangeComplexity);

		return analysis;
	}

}