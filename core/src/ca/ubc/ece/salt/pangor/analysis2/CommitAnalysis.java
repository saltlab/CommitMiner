package ca.ubc.ece.salt.pangor.analysis2;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.mozilla.javascript.EvaluatorException;

import ca.ubc.ece.salt.pangor.analysis.Alert;
import ca.ubc.ece.salt.pangor.analysis.DataSet;
import ca.ubc.ece.salt.pangor.batch.Commit;
import ca.ubc.ece.salt.pangor.batch.SourceCodeFileChange;
import ca.ubc.ece.salt.pangor.cfd.CFDContext;
import ca.ubc.ece.salt.pangor.cfd.ControlFlowDifferencing;
import ca.ubc.ece.salt.pangor.cfg.CFGFactory;

/**
 * Gathers facts about one commit and synthesizes alerts based on those facts.
 *
 * @param <U> The type of alert the data set stores.
 * @param <T> The type of data set that stores the analysis results.
 * @param <S> The type of the source file analysis.
 * @param <D> Type type of the destination file analysis.
 *
 * TODO: Here is where we should create pre and post conditions. When both conditions are met, we register
 * some alert... which also needs to be defined by the subclass.
 */
public abstract class CommitAnalysis<U extends Alert, T extends DataSet<U>,
	S extends SourceCodeFileAnalysis, D extends SourceCodeFileAnalysis> {

	/**
	 * The data set manages the alerts by storing and loading alerts to and
	 * from the disk, performing pre-processing tasks and calculating metrics.
	 */
	private T dataSet;

	/** The source file analysis to use. **/
	private S srcAnalysis;

	/** The destination file analysis to use. **/
	private D dstAnalysis;

	/**
	 * A map of file extensions to CFGFactories (used for control flow differencing).
	 */
	private Map<String, CFGFactory> cfgFactories;

	/** Set to true to enable AST pre-processing. **/
	private boolean preProcess;

	/**
	 * @param srcAnalysis The analysis to run on the source (or buggy) file.
	 * @param dstAnalysis The analysis to run on the destination (or repaired) file.
	 */
	public CommitAnalysis(T dataSet, Commit ami, S srcAnalysis, D dstAnalysis,
			Map<String, CFGFactory> cfgFactories, boolean preProcess) {
		this.srcAnalysis = srcAnalysis;
		this.dstAnalysis = dstAnalysis;
		this.cfgFactories = cfgFactories;
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

		/* Iterate through the files in the commit and call the SourceCodeFileAnalysis on them. */
		for(SourceCodeFileChange sourceCodeFileChange : commit.sourceCodeFileChanges) {
			this.analyzeFile(sourceCodeFileChange);
		}

		/* Synthesize the alerts from the analysis facts. */
		this.synthesizeAlerts();

	}

	/**
	 * Registers alerts based on the patterns found by the analysis.
	 *
	 * For each pattern, checks that pre-conditions are met and that there
	 * are no anti-patterns.
	 *
	 * @throws Exception
	 */
	protected void synthesizeAlerts() throws Exception {

		/* TODO: Compute the set of (P - A) n C ... that is patters minus antipatterns intersecting preconditions. */

	}

	/**
	 * Performs AST-differencing and launches the analysis of the pre-commit/post-commit
	 * source code file pair.
	 *
	 * @param commit The meta info for the analysis (i.e., project id, file paths,
	 * 			  commit IDs, etc.)
	 * @param preProcess Set to true to enable AST pre-processing.
	 */
	private void analyzeFile(SourceCodeFileChange sourceCodeFileChange) throws Exception {

		/* Get the file extension. */
		String fileExtension = getSourceCodeFileExtension(sourceCodeFileChange.buggyFile, sourceCodeFileChange.repairedFile);

		/* Look up the CFGFactory. */
		CFGFactory cfgFactory = null;
		if(fileExtension != null) cfgFactory = this.cfgFactories.get(fileExtension);

		/* Difference the files and analyze if they are an extension we handle. */
		if(cfgFactory != null) {

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
			this.srcAnalysis.analyze(cfdContext.srcScript, cfdContext.srcCFGs);
			this.dstAnalysis.analyze(cfdContext.dstScript, cfdContext.dstCFGs);

		}

	}

	/**
	 * @param preCommitPath The path of the file before the commit.
	 * @param postCommitPath The path of the file after the commit.
	 * @return The extension of the source code file or null if none is found
	 * 	or the extensions of the pre and post paths do not match.
	 */
	private static String getSourceCodeFileExtension(String preCommitPath, String postCommitPath) {
		Pattern pattern = Pattern.compile("\\.[a-z]+$");
		Matcher preMatcher = pattern.matcher(preCommitPath);
		Matcher postMatcher = pattern.matcher(postCommitPath);

		String preExtension = null;
		String postExtension = null;

		if(preMatcher.matches() && postMatcher.matches()) {
			preExtension = preMatcher.group();
			postExtension = postMatcher.group();
			if(preExtension.equals(postExtension)) return preExtension.substring(1);
		}

		return null;

	}


}
