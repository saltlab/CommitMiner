package ca.ubc.ece.salt.pangor.analysis;

import java.util.Map;
import java.util.regex.Matcher;

import org.apache.commons.lang3.time.StopWatch;
import org.deri.iris.api.basics.IPredicate;
import org.deri.iris.storage.IRelation;
import org.mozilla.javascript.EvaluatorException;

import ca.ubc.ece.salt.pangor.analysis.factories.ISourceCodeFileAnalysisFactory;
import ca.ubc.ece.salt.pangor.cfd.CFDContext;
import ca.ubc.ece.salt.pangor.cfd.ControlFlowDifferencing;
import ca.ubc.ece.salt.pangor.cfg.CFGFactory;

/**
 * Gathers facts about one commit that fall within a domain.
 */
public class DomainAnalysis {

	/** The source file analysis to use. **/
	protected ISourceCodeFileAnalysisFactory srcAnalysisFactory;

	/** The destination file analysis to use. **/
	protected ISourceCodeFileAnalysisFactory dstAnalysisFactory;

	/**
	 * A map of file extensions to CFGFactories (used for control flow differencing).
	 */
	protected CFGFactory cfgFactory;

	/** Set to true to enable AST pre-processing. **/
	private boolean preProcess;

	/**
	 * @param srcAnalysis The analysis to run on the source (or buggy) file.
	 * @param dstAnalysis The analysis to run on the destination (or repaired) file.
	 */
	public DomainAnalysis(ISourceCodeFileAnalysisFactory srcAnalysisClass,
						  ISourceCodeFileAnalysisFactory dstAnalysisClass,
						  CFGFactory cfgFactory, boolean preProcess) {
		this.srcAnalysisFactory = srcAnalysisClass;
		this.dstAnalysisFactory = dstAnalysisClass;
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
		
//		System.out.println("Starting analysis of commit" + commit.url);
		StopWatch commitTimer = new StopWatch();
		commitTimer.start();

		/* Analyze the commit before the files are analyzed. */
		if(!preAnalysis(commit, facts)) return;

		/* Iterate through the files in the commit and run the
		 * SourceCodeFileAnalysis on each of them. */
		for(SourceCodeFileChange sourceCodeFileChange : commit.sourceCodeFileChanges) {
//			System.out.println("Starting analysis of file " + commit.url + " " + sourceCodeFileChange.repairedFile);
			StopWatch fileTimer = new StopWatch();
			fileTimer.start();

			this.analyzeFile(sourceCodeFileChange, facts);

			fileTimer.stop();
			System.out.println("File:" + fileTimer.getNanoTime());
		}

		/* Analyze the commit after the files are analyzed. */
		postAnalysis(commit, facts);
		
		/* Stop the commit analysis timer. */
		commitTimer.stop();
		System.out.println("Commit:" + commitTimer.getNanoTime());

	}

	/**
	 * Override to run a custom pre-file analysis.
	 * @return {@code true} to continue the analysis, {@code false} to abort.
	 * @param commit The commit being analyzed.
	 * @param facts The database of facts the domain analysis will add to.
	 * @throws Exception when an error occurs during domain analysis.
	 */
	protected boolean preAnalysis(Commit commit, Map<IPredicate, IRelation> facts) throws Exception {
		return true;
	}


	/**
	 * Override to run a custom post-file analysis.
	 * @param commit The commit being analyzed.
	 * @param facts The database of facts the domain analysis will add to.
	 * @throws Exception when an error occurs during domain analysis.
	 */
	protected void postAnalysis(Commit commit, Map<IPredicate, IRelation> facts) throws Exception { }

	/**
	 * Performs AST-differencing and launches the analysis of the pre-commit/post-commit
	 * source code file pair.
	 *
	 * @param sourceCodeFileChange The source code file change information.
	 * @param facts Stores the facts from this analysis.
	 * @param preProcess Set to true to enable AST pre-processing.
	 * @param srcAnalysisFactory The analysis to run on the buggy file.
	 * @param dstAnalysisClass The analysis to run on the repaired file.
	 */
	protected void analyzeFile(SourceCodeFileChange sourceCodeFileChange,
							   Map<IPredicate, IRelation> facts) throws Exception {

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

			/* Build the analyzers with reflection. */
			SourceCodeFileAnalysis srcAnalysis = this.srcAnalysisFactory.newInstance();
			SourceCodeFileAnalysis dstAnalysis = this.srcAnalysisFactory.newInstance();

			/* Run the analysis. */
			srcAnalysis.analyze(sourceCodeFileChange, facts, cfdContext.srcScript, cfdContext.srcCFGs);
			dstAnalysis.analyze(sourceCodeFileChange, facts, cfdContext.dstScript, cfdContext.dstCFGs);

		}

	}

	/**
	 * @param preCommitPath The path of the file before the commit.
	 * @param postCommitPath The path of the file after the commit.
	 * @return The extension of the source code file or null if none is found
	 * 	or the extensions of the pre and post paths do not match.
	 */
	protected static String getSourceCodeFileExtension(String preCommitPath, String postCommitPath) {

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
