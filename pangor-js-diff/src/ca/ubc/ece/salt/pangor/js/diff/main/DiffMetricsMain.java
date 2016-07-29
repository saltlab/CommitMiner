package ca.ubc.ece.salt.pangor.js.diff.main;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class DiffMetricsMain {

	public static void main(String[] args) {
		DiffMetricsMain metrics = new DiffMetricsMain("./output/dataset_2016-07-28.csv");
		metrics.analyze();
	}


	private String datasetPath;
	private Map<FV, SourceFilePair> sourceFilePairs;

	public DiffMetricsMain(String datasetPath) {
		this.datasetPath = datasetPath;
		this.sourceFilePairs = new HashMap<FV, SourceFilePair>();
	}

	private void analyze() {

		try(BufferedReader reader = new BufferedReader(new FileReader(datasetPath))) {

			for (String line = reader.readLine();
					line != null;
					line = reader.readLine()) {

				/* Read the feature vector. */
				String[] tokens = line.split(",");
				FV fv = new FV(tokens);

				/* Get the source file pair for the feture vector. */
				SourceFilePair sfp = sourceFilePairs.get(fv);
				if(sfp == null) {
					sfp = new SourceFilePair(fv.bfc, fv.file);
					sourceFilePairs.put(fv, sfp);
				}

				/* Store the feature vector. */
				if(fv.version.equals("SOURCE")) sfp.source.interpretFeatureVector(fv);
				else sfp.destination.interpretFeatureVector(fv);

			}

		}
		catch(Exception e) {
			System.err.println(e);
		}

		/* Compute the metrics for each file. */
		List<SourceFileDiffComparison> sourceComparisons = new LinkedList<SourceFileDiffComparison>();
		List<SourceFileDiffComparison> destinationComparisons = new LinkedList<SourceFileDiffComparison>();
		for(SourceFilePair sfp : sourceFilePairs.values()) {


			/* Source */
			SourceFileDiffComparison source = new SourceFileDiffComparison();
			source.commit = sfp.commit;
			source.file = sfp.file;
			source.totalLines = sfp.source.totalLines;
			source.lineChanges = sfp.source.line.size();
			source.astChanges = sfp.source.ast.size();
			source.conChanges = sfp.source.control.size();
			source.envChanges = sfp.source.environment.size();
			source.astLineSubtraction = subtract(sfp.source.ast, sfp.source.line);
			source.conLineSubtraction = subtract(sfp.source.control, sfp.source.line);
			source.envLineSubtraction = subtract(sfp.source.environment, sfp.source.line);
			source.valLineSubtraction = subtract(sfp.source.value, sfp.source.line);
			source.conAstSubtraction = subtract(sfp.source.control, sfp.source.ast);
			source.envAstSubtraction = subtract(sfp.source.environment, sfp.source.environment);
			source.valAstSubtraction = subtract(sfp.source.value, sfp.source.value);
			sourceComparisons.add(source);

			/* Destination */
			SourceFileDiffComparison destination = new SourceFileDiffComparison();
			destination.commit = sfp.commit;
			destination.file = sfp.file;
			destination.totalLines = sfp.destination.totalLines;
			destination.lineChanges = sfp.destination.line.size();
			destination.astChanges = sfp.destination.ast.size();
			destination.conChanges = sfp.destination.control.size();
			destination.envChanges = sfp.destination.environment.size();
			destination.astLineSubtraction = subtract(sfp.destination.ast, sfp.destination.line);
			destination.conLineSubtraction = subtract(sfp.destination.control, sfp.destination.line);
			destination.envLineSubtraction = subtract(sfp.destination.environment, sfp.destination.line);
			destination.valLineSubtraction = subtract(sfp.destination.value, sfp.destination.line);
			destination.conAstSubtraction = subtract(sfp.destination.control, sfp.destination.ast);
			destination.envAstSubtraction = subtract(sfp.destination.environment, sfp.destination.ast);
			destination.valAstSubtraction = subtract(sfp.destination.value, sfp.destination.ast);
			destinationComparisons.add(destination);

		}

		/* Print the mean and median. */
		Set<String> pairLineImprovedCommits = new HashSet<String>();
		Set<String> pairLineImprovedFiles = new HashSet<String>();
		Set<String> pairImprovedCommits = new HashSet<String>();
		Set<String> pairImprovedFiles = new HashSet<String>();
		Set<String> pairASTCommit = new HashSet<String>();
		Set<String> pairASTFile = new HashSet<String>();
		System.out.println("--Source Metrics--");
		printMetrics(sourceComparisons, pairLineImprovedCommits, pairLineImprovedFiles, pairImprovedCommits, pairImprovedFiles, pairASTCommit, pairASTFile);
		System.out.println("\n--Destination Metrics--");
		printMetrics(destinationComparisons, pairLineImprovedCommits, pairLineImprovedFiles, pairImprovedCommits, pairImprovedFiles, pairASTCommit, pairASTFile);
		System.out.println("\n--Pair Metrics--");
		System.out.println("Total Files Line-Improved = " + pairLineImprovedFiles.size());
		System.out.println("Total Commits Line-Improved = " + pairLineImprovedCommits.size());
		System.out.println("Total Files Improved = " + pairImprovedFiles.size());
		System.out.println("Total Commits Improved = " + pairImprovedCommits.size());
		System.out.println("Total Files With AST Changes = " + pairASTFile.size());
		System.out.println("Total Commits With AST Changes = " + pairASTCommit.size());

	}

	private void printMetrics(List<SourceFileDiffComparison> sourceComparisons,
			Set<String> pairLineImprovedCommits, Set<String> pairLineImprovedFiles,
			Set<String> pairImprovedCommits, Set<String> pairImprovedFiles,
			Set<String> pairASTCommit, Set<String> pairASTFile) {

		Set<String> totalCommits = new HashSet<String>();
		Set<String> improvedCommits = new HashSet<String>();

		double astLineAvg = 0;
		double conAstAvg = 0;
		double envAstAvg = 0;
		double valAstAvg = 0;

		int astCnt = 0;
		int conCnt = 0;
		int envCnt = 0;
		int valCnt = 0;
		int anyCnt = 0;

		for(SourceFileDiffComparison source : sourceComparisons) {

			if(source.astChanges > 0) {
				pairASTCommit.add(source.commit);
				pairASTFile.add(source.commit + "~" + source.file);
			}

			if(source.conChanges > 0
					|| source.envChanges > 0
					|| source.valChanges > 0) {
				pairImprovedCommits.add(source.commit);
				pairImprovedFiles.add(source.commit + "~" + source.file);
			}

			if(source.astLineSubtraction > 0) {
				astLineAvg += source.astLineSubtraction;
				astCnt++;
			}
			if(source.conAstSubtraction > 0) {
				conAstAvg += source.conAstSubtraction;
				conCnt++;
			}
			if(source.envAstSubtraction > 0) {
				envAstAvg += source.envAstSubtraction;
				envCnt++;
			}
			if(source.valAstSubtraction > 0) {
				valAstAvg += source.valAstSubtraction;
				valCnt++;
			}
			if(source.conAstSubtraction > 0
					|| source.envAstSubtraction > 0
					|| source.valAstSubtraction > 0) {
				anyCnt++;
				improvedCommits.add(source.commit);
				pairLineImprovedCommits.add(source.commit);
				pairLineImprovedFiles.add(source.commit + "~" + source.file);
			}

			totalCommits.add(source.commit);

//			System.out.println(source.astChanges + "," + source.conAstSubtraction);
		}

		if(sourceComparisons.size() > 0) {
			astLineAvg /= astCnt;
			conAstAvg /= conCnt;
			envAstAvg /= envCnt;
			valAstAvg /= valCnt;
		}

		System.out.println("AST - Line Avg = " + astLineAvg);
		System.out.println("Control - AST Avg = " + conAstAvg);
		System.out.println("Environment - AST Avg = " + envAstAvg);
		System.out.println("Value - AST Avg = " + valAstAvg);
		System.out.println("Control Improves AST Count = " + conCnt);
		System.out.println("Environment Improves AST Count = " + envCnt);
		System.out.println("Value Improves AST Count = " + valCnt);
		System.out.println("Any Improves AST Count = " + anyCnt);
		System.out.println("Total Files Analyzed = " + sourceComparisons.size());
		System.out.println("Any Improves AST Count in Commit = " + improvedCommits.size());
		System.out.println("Total Commits Analyzed = " + totalCommits.size());

	}

	private int subtract(Set<Integer> left, Set<Integer> right) {
		Set<Integer> tmp = new HashSet<Integer>(left);
		tmp.removeAll(right);
		return tmp.size();
	}

}
