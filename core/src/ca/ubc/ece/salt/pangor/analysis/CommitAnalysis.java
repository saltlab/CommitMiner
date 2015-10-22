package ca.ubc.ece.salt.pangor.analysis;

import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;

import org.mozilla.javascript.EvaluatorException;

import ca.ubc.ece.salt.pangor.batch.SourceCodeFileChange;
import ca.ubc.ece.salt.pangor.cfd.CFDContext;
import ca.ubc.ece.salt.pangor.cfd.ControlFlowDifferencing;
import ca.ubc.ece.salt.pangor.cfg.CFGFactory;

/**
 * Gathers facts about one commit and synthesizes alerts based on those facts.
 *
 * @param <A> The type of alert the data set stores.
 * @param <DS> The type of data set that stores the analysis results.
 * @param <S> The type of the source file analysis.
 * @param <D> Type type of the destination file analysis.
 */
public class CommitAnalysis<A extends Alert, DS extends DataSet<A>,
	S extends SourceCodeFileAnalysis<A>, D extends SourceCodeFileAnalysis<A>> {

	/**
	 * The data set manages the alerts by storing and loading alerts to and
	 * from the disk, performing pre-processing tasks and calculating metrics.
	 */
	private DS dataSet;

	/** The source file analysis to use. **/
	private S srcAnalysis;

	/** The destination file analysis to use. **/
	private D dstAnalysis;

	/**
	 * A map of file extensions to CFGFactories (used for control flow differencing).
	 */
	private CFGFactory cfgFactory;

	/** Set to true to enable AST pre-processing. **/
	private boolean preProcess;

	/**
	 * @param srcAnalysis The analysis to run on the source (or buggy) file.
	 * @param dstAnalysis The analysis to run on the destination (or repaired) file.
	 */
	public CommitAnalysis(DS dataSet, Commit commit, S srcAnalysis, D dstAnalysis,
			CFGFactory cfgFactory, boolean preProcess) {
		this.dataSet = dataSet;
		this.srcAnalysis = srcAnalysis;
		this.dstAnalysis = dstAnalysis;
		this.cfgFactory = cfgFactory;
		this.preProcess = preProcess;
	}

	/**
	 * Analyze the commit. Each file in the commit is analyzed separately, and
	 * facts are gathered from each analysis. Once all the files are analyzed,
	 * alerts are synthesized by checking that pre-conditions and post-conditions
	 * are all met.
	 * @param fai
	 * @throws Exception
	 */
	public void analyze(Commit commit) throws Exception {

		/* Store the facts from the analysis in this object. */
		Facts<A> facts = new Facts<A>();

		/* Iterate through the files in the commit and call the SourceCodeFileAnalysis on them. */
		for(SourceCodeFileChange sourceCodeFileChange : commit.sourceCodeFileChanges) {
			this.analyzeFile(facts, sourceCodeFileChange);
		}

		/* Synthesize the alerts from the analysis facts. */
		this.synthesizeAlerts(commit, facts);

	}

	/**
	 * Registers alerts based on the patterns found by the analysis.
	 *
	 * For each pattern, checks that pre-conditions are met and that there
	 * are no anti-patterns.
	 *
	 * @throws Exception
	 */
	protected void synthesizeAlerts(Commit commit, Facts<A> facts) throws Exception {

		/* Get the facts (patterns, anti-patterns and pre-conditions) from the
		 * source and destination analysis. */

		List<Pattern<A>> patterns = facts.getPatterns();
		Set<AntiPattern> antiPatterns = facts.getAntiPatterns();
		Set<PreCondition> preConditions = facts.getPreConditions();

		/* Compute the set of (P - A) n C ... that is patters minus
		 * anti-patterns intersecting pre-conditions. */

		List<A> alerts = new LinkedList<A>();
		for(Pattern<A> pattern : patterns) {
			if(pattern.accept(antiPatterns, preConditions)) {
				alerts.add(pattern.getAlert(commit));
			}
		}

		/* Register the alerts. */

		for(A alert : alerts) {
			this.dataSet.registerAlert(alert);
		}

	}

	/**
	 * Performs AST-differencing and launches the analysis of the pre-commit/post-commit
	 * source code file pair.
	 *
	 * @param facts Stores the facts from this analysis.
	 * @param preProcess Set to true to enable AST pre-processing.
	 */
	private void analyzeFile(Facts<A> facts, SourceCodeFileChange sourceCodeFileChange) throws Exception {

		/* Get the file extension. */
		String fileExtension = getSourceCodeFileExtension(sourceCodeFileChange.buggyFile, sourceCodeFileChange.repairedFile);

		/* Difference the files and analyze if they are an extension we handle. */
		if(fileExtension != null && cfgFactory.acceptsExtension(fileExtension)) {

			/* Control flow difference the files. */
			ControlFlowDifferencing cfd = null;
			try {
				String[] args = preProcess ? new String[] {"", "", "-pp"} : new String[] {"", ""};
				cfd = new ControlFlowDifferencing(cfgFactory, args, sourceCodeFileChange.buggyCode, sourceCodeFileChange.repairedCode);
			}
			catch(ArrayIndexOutOfBoundsException e) {
				System.err.println("ArrayIndexOutOfBoundsException: possibly caused by empty file.");
				return;
			}
			catch(EvaluatorException e) {
				System.err.println("Evaluator exception: " + e.getMessage());
				return;
			}
			catch(Exception e) {
				throw e;
			}

			/* Get the results of the control flow differencing. The results
			 * include an analysis context: the source and destination ASTs
			 * and CFGs. */
			CFDContext cfdContext = cfd.getContext();

			/* Run the analysis. */
			this.srcAnalysis.analyze(facts, cfdContext.srcScript, cfdContext.srcCFGs);
			this.dstAnalysis.analyze(facts, cfdContext.dstScript, cfdContext.dstCFGs);

		}

	}

	/**
	 * @param preCommitPath The path of the file before the commit.
	 * @param postCommitPath The path of the file after the commit.
	 * @return The extension of the source code file or null if none is found
	 * 	or the extensions of the pre and post paths do not match.
	 */
	private static String getSourceCodeFileExtension(String preCommitPath, String postCommitPath) {
		java.util.regex.Pattern pattern = java.util.regex.Pattern.compile("\\.[a-z]+$");
		Matcher preMatcher = pattern.matcher(preCommitPath);
		Matcher postMatcher = pattern.matcher(postCommitPath);

		String preExtension = null;
		String postExtension = null;

		if(preMatcher.find() && postMatcher.find()) {
			preExtension = preMatcher.group();
			postExtension = postMatcher.group();
			if(preExtension.equals(postExtension)) return preExtension.substring(1);
		}

		return null;

	}


}
