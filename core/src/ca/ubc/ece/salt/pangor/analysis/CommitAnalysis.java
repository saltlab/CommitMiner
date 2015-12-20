package ca.ubc.ece.salt.pangor.analysis;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;

import org.deri.iris.api.basics.IPredicate;
import org.deri.iris.api.basics.IQuery;
import org.deri.iris.api.basics.IRule;
import org.deri.iris.storage.IRelation;
import org.mozilla.javascript.EvaluatorException;

import ca.ubc.ece.salt.pangor.cfd.CFDContext;
import ca.ubc.ece.salt.pangor.cfd.ControlFlowDifferencing;
import ca.ubc.ece.salt.pangor.cfg.CFGFactory;

/**
 * Gathers facts about one commit and runs queries in prolog against those
 * facts to synthesize alerts.
 *
 * @param <A> The type of alert the data set stores.
 * @param <DS> The type of data set that stores the analysis results.
 * @param <S> The type of the source file analysis.
 * @param <D> Type type of the destination file analysis.
 */
public class CommitAnalysis<DS extends DataSet,
	S extends SourceCodeFileAnalysis, D extends SourceCodeFileAnalysis> {

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
	 * @param rules The datalog rules that are part of the IRIS KnowledgeBase.
	 * @param queries The datalog queries that will produce alerts.
	 * @param srcAnalysis The analysis to run on the source (or buggy) file.
	 * @param dstAnalysis The analysis to run on the destination (or repaired) file.
	 */
	public CommitAnalysis(List<IRule> rules, List<IQuery> queries,
			DS dataSet,
			S srcAnalysis, D dstAnalysis,
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

		/* Initialize the fact base that will be filled by:
		 * 	a) This CommitAnalysis.
		 * 	b) The SourceCodeFileAnalysis implementations. */
		Map<IPredicate, IRelation> facts = new HashMap<IPredicate, IRelation>();

		/* Iterate through the files in the commit and run the
		 * SourceCodeFileAnalysis on each of them. */
		for(SourceCodeFileChange sourceCodeFileChange : commit.sourceCodeFileChanges) {
			this.analyzeFile(commit, sourceCodeFileChange, facts);
		}

		/* Synthesize the alerts from the analysis facts. */
		this.synthesizeAlerts(commit, facts);

	}

	/**
	 * Registers alerts by applying rules to the facts found during the analysis.
	 *
	 * This method is effective for patterns that are contained within one
	 * file. For analyses that need knowledge about multiple files, this method
	 * should be overridden.
	 *
	 * @param commit The details for the commit.
	 * @param facts The facts derived from the source code file analyses.
	 * @throws Exception
	 */
	protected void synthesizeAlerts(Commit commit, Map<IPredicate, IRelation> facts) throws Exception {

		/* Query the knowledge base and create alerts. */
		this.dataSet.addCommitAnalysisResults(commit, facts);

	}

	/**
	 * Performs AST-differencing and launches the analysis of the pre-commit/post-commit
	 * source code file pair.
	 *
	 * @param commit The commit information.
	 * @param sourceCodeFileChange The source code file change information.
	 * @param facts Stores the facts from this analysis.
	 * @param preProcess Set to true to enable AST pre-processing.
	 */
	private void analyzeFile(Commit commit, SourceCodeFileChange sourceCodeFileChange, Map<IPredicate, IRelation> facts) throws Exception {

		/* Get the file extension. */
		String fileExtension = getSourceCodeFileExtension(sourceCodeFileChange.buggyFile, sourceCodeFileChange.repairedFile);

		/* Difference the files and analyze if they are an extension we handle. */
		if(fileExtension != null && cfgFactory.acceptsExtension(fileExtension)) {

			/* Control flow difference the files. */
			ControlFlowDifferencing cfd = null;
			try {
				String[] args = preProcess ? new String[] {sourceCodeFileChange.buggyFile, sourceCodeFileChange.repairedFile, "-pp"}
									: new String[] {sourceCodeFileChange.buggyFile, sourceCodeFileChange.repairedFile};
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
			this.srcAnalysis.analyze(commit, sourceCodeFileChange, facts, cfdContext.srcScript, cfdContext.srcCFGs);
			this.dstAnalysis.analyze(commit, sourceCodeFileChange, facts, cfdContext.dstScript, cfdContext.dstCFGs);

		}

	}

	/**
	 * @param preCommitPath The path of the file before the commit.
	 * @param postCommitPath The path of the file after the commit.
	 * @return The extension of the source code file or null if none is found
	 * 	or the extensions of the pre and post paths do not match.
	 */
	private static String getSourceCodeFileExtension(String preCommitPath, String postCommitPath) {

		java.util.regex.Pattern pattern = java.util.regex.Pattern.compile("\\.([a-z]+)$");
		Matcher preMatcher = pattern.matcher(preCommitPath);
		Matcher postMatcher = pattern.matcher(postCommitPath);

		String preExtension = null;
		String postExtension = null;

		if(preMatcher.find() && postMatcher.find()) {
			preExtension = preMatcher.group(1);
			postExtension = postMatcher.group(1);
			if(preExtension.equals(postExtension)) return preExtension;
		}

		return null;

	}


}
