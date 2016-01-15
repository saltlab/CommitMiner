package ca.ubc.ece.salt.pangor.analysis;

import java.util.Map;
import java.util.regex.Matcher;

import org.deri.iris.api.basics.IPredicate;
import org.deri.iris.storage.IRelation;
import org.mozilla.javascript.EvaluatorException;

import ca.ubc.ece.salt.pangor.cfd.CFDContext;
import ca.ubc.ece.salt.pangor.cfd.ControlFlowDifferencing;
import ca.ubc.ece.salt.pangor.cfg.CFGFactory;

/**
 * Gathers facts about one commit that fall within a domain.
 */
public class DomainAnalysis {

	/** The source file analysis to use. **/
	private SourceCodeFileAnalysis srcAnalysis;

	/** The destination file analysis to use. **/
	private SourceCodeFileAnalysis dstAnalysis;

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
	public DomainAnalysis(SourceCodeFileAnalysis srcAnalysis,
						  SourceCodeFileAnalysis dstAnalysis,
						  CFGFactory cfgFactory, boolean preProcess) {
		this.srcAnalysis = srcAnalysis;
		this.dstAnalysis = dstAnalysis;
		this.cfgFactory = cfgFactory;
		this.preProcess = preProcess;
	}

	/**
	 * Analyze the commit. Each file in the commit is analyzed separately, and
	 * facts are gathered from each analysis. By extending this class, the
	 * {@code DomainAnalysis} itself may also do an analysis (e.g., to extract
	 * facts about the structural changes between files).
	 *
	 * @param commit The commit being analyzed.
	 * @param facts The database of facts the domain analysis will add to.
	 * @throws Exception when an error occurs during domain analysis.
	 */
	public void analyze(Commit commit, Map<IPredicate, IRelation> facts) throws Exception {

		/* Iterate through the files in the commit and run the
		 * SourceCodeFileAnalysis on each of them. */
		for(SourceCodeFileChange sourceCodeFileChange : commit.sourceCodeFileChanges) {
			this.analyzeFile(commit, sourceCodeFileChange, facts);
		}

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
