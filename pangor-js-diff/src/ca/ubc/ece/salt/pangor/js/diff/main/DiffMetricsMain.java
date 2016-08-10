package ca.ubc.ece.salt.pangor.js.diff.main;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import ca.ubc.ece.salt.pangor.js.diff.main.SourceFile.DiffType;

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
					sfp = new SourceFilePair(fv.project, fv.bfc, fv.file, fv.url);
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
		List<SourceFileDiffComparison> pairComparisons = new LinkedList<SourceFileDiffComparison>();
		List<SourceFileDiffComparison> sourceComparisons = new LinkedList<SourceFileDiffComparison>();
		List<SourceFileDiffComparison> destinationComparisons = new LinkedList<SourceFileDiffComparison>();
		for(SourceFilePair sfp : sourceFilePairs.values()) {

			/* Pair */
			SourceFileDiffComparison pair = new SourceFileDiffComparison();
			pair.project = sfp.project;
			pair.commit = sfp.commit;
			pair.file = sfp.file;
			pair.url = sfp.url;
			pair.totalLines = sfp.getAvgTotalLines();
			pair.lineChanges = sfp.getSize(DiffType.LINE);
			pair.astChanges = sfp.getSize(DiffType.AST);
			pair.conChanges = sfp.getSize(DiffType.CONTROL);
			pair.envChanges = sfp.getSize(DiffType.ENVIRONMENT);
			pair.valChanges = sfp.getSize(DiffType.VALUE);
			pair.multiChanges = sfp.getSize(DiffType.MULTI);
			pair.conAstSubtraction = sfp.subtract(DiffType.CONTROL, DiffType.AST);
			pair.envAstSubtraction = sfp.subtract(DiffType.ENVIRONMENT, DiffType.AST);
			pair.valAstSubtraction = sfp.subtract(DiffType.VALUE, DiffType.AST);
			pair.multiAstSubtraction = sfp.subtract(DiffType.MULTI, DiffType.AST);
			pairComparisons.add(pair);

			/* Source */
			SourceFileDiffComparison source = new SourceFileDiffComparison();
			source.project = sfp.project;
			source.commit = sfp.commit;
			source.file = sfp.file;
			source.url = sfp.url;
			source.totalLines = sfp.source.getTotalLines();
			source.lineChanges = sfp.source.getSize(DiffType.LINE);
			source.astChanges = sfp.source.getSize(DiffType.AST);
			source.conChanges = sfp.source.getSize(DiffType.CONTROL);
			source.envChanges = sfp.source.getSize(DiffType.ENVIRONMENT);
			source.valChanges = sfp.source.getSize(DiffType.VALUE);
			source.multiChanges = sfp.source.getSize(DiffType.MULTI);
			source.conAstSubtraction = sfp.source.subtract(DiffType.CONTROL, DiffType.AST);
			source.envAstSubtraction = sfp.source.subtract(DiffType.ENVIRONMENT, DiffType.AST);
			source.valAstSubtraction = sfp.source.subtract(DiffType.VALUE, DiffType.AST);
			source.multiAstSubtraction = sfp.source.subtract(DiffType.MULTI, DiffType.AST);
			sourceComparisons.add(source);

			/* Destination */
			SourceFileDiffComparison destination = new SourceFileDiffComparison();
			destination.project = sfp.project;
			destination.commit = sfp.commit;
			destination.file = sfp.file;
			destination.url = sfp.url;
			destination.totalLines = sfp.destination.getTotalLines();
			destination.lineChanges = sfp.destination.getSize(DiffType.LINE);
			destination.astChanges = sfp.destination.getSize(DiffType.AST);
			destination.conChanges = sfp.destination.getSize(DiffType.CONTROL);
			destination.envChanges = sfp.destination.getSize(DiffType.ENVIRONMENT);
			destination.valChanges = sfp.destination.getSize(DiffType.VALUE);
			destination.multiChanges = sfp.destination.getSize(DiffType.MULTI);
			destination.conAstSubtraction = sfp.destination.subtract(DiffType.CONTROL, DiffType.AST);
			destination.envAstSubtraction = sfp.destination.subtract(DiffType.ENVIRONMENT, DiffType.AST);
			destination.valAstSubtraction = sfp.destination.subtract(DiffType.VALUE, DiffType.AST);
			destination.multiAstSubtraction = sfp.destination.subtract(DiffType.MULTI, DiffType.AST);
			destinationComparisons.add(destination);

		}

		/* Print the metrics. */
		System.out.println("--Source Metrics--");
		printMetrics(sourceComparisons);
		System.out.println("\n--Destination Metrics--");
		printMetrics(destinationComparisons);
		System.out.println("\n--Pair Metrics--");
		printMetrics(pairComparisons);

	}


	private void printMetrics(List<SourceFileDiffComparison> sourceComparisons) {

		Set<String> totalCommits = new HashSet<String>();
		Set<String> totalASTModifiedCommits = new HashSet<String>();
		int totalASTModifiedFiles = 0;

		/* Count the number of commits with at least one multi-diff fact. */
		Set<String> multiContextImprovedCommits = new HashSet<String>();
		Set<String> conContextImprovedCommits = new HashSet<String>();
		Set<String> envContextImprovedCommits = new HashSet<String>();
		Set<String> valContextImprovedCommits = new HashSet<String>();

		/* Count the number of files with at least one multi-diff fact. */
		int multiContextImprovedFilePairs = 0;
		int conContextImprovedFilePairs = 0;
		int envContextImprovedFilePairs = 0;
		int valContextImprovedFilePairs = 0;

		/* Count the number of commits with at least one line improvement. */
		Set<String> multiLineImprovedCommits = new HashSet<String>();
		Set<String> conLineImprovedCommits = new HashSet<String>();
		Set<String> envLineImprovedCommits = new HashSet<String>();
		Set<String> valLineImprovedCommits = new HashSet<String>();

		/* Count the number of files with at least one line improvement. */
		int multiLineImprovedFilePairs = 0;
		int conLineImprovedFilePairs = 0;
		int envLineImprovedFilePairs = 0;
		int valLineImprovedFilePairs = 0;

		/* Count the total number of line-improvements. */
		int multiLinesAdded = 0;
		int conLinesAdded = 0;
		int envLinesAdded = 0;
		int valLinesAdded = 0;

		/* Count the total number of multi-diff facts added. */
		int multiFactsAdded = 0;
		int conFactsAdded = 0;
		int envFactsAdded = 0;
		int valFactsAdded = 0;

		/* Compute the totals. */
		for(SourceFileDiffComparison source : sourceComparisons) {

			/* For computing the total number of commits. */
			totalCommits.add(source.commit);

			/* For computing the total number of commits with at least one
			 * AST change. */
			if(source.astChanges > 0) {
				totalASTModifiedCommits.add(source.commit);
				totalASTModifiedFiles++;
			}

			/* Check if the file is line-improved. */
			if(source.multiAstSubtraction > 0) {
				multiLineImprovedCommits.add(source.commit);
				multiLineImprovedFilePairs++;
				multiLinesAdded += source.multiAstSubtraction;
			}
			if(source.conAstSubtraction > 0) {
				conLineImprovedCommits.add(source.commit);
				conLineImprovedFilePairs++;
				conLinesAdded += source.conAstSubtraction;
			}
			if(source.envAstSubtraction > 0) {
				envLineImprovedCommits.add(source.commit);
				envLineImprovedFilePairs++;
				envLinesAdded += source.envAstSubtraction;
			}
			if(source.valAstSubtraction > 0) {
				valLineImprovedCommits.add(source.commit);
				valLineImprovedFilePairs++;
				valLinesAdded += source.valAstSubtraction;
			}

			/* Check if the file is context-improved. */
			if(source.multiChanges > 0) {
				multiContextImprovedCommits.add(source.commit);
				multiContextImprovedFilePairs++;
				multiFactsAdded += source.multiChanges;
			}
			if(source.conChanges > 0) {
				conContextImprovedCommits.add(source.commit);
				conContextImprovedFilePairs++;
				conFactsAdded += source.conChanges;
			}
			if(source.envChanges > 0) {
				envContextImprovedCommits.add(source.commit);
				envContextImprovedFilePairs++;
				envFactsAdded += source.envChanges;
			}
			if(source.valChanges > 0) {
				valContextImprovedCommits.add(source.commit);
				valContextImprovedFilePairs++;
				valFactsAdded += source.valChanges;
			}

		}

		System.out.println("Total Commits Analyzed = " + totalCommits.size());
		System.out.println("Total Files Analyzed = " + sourceComparisons.size());
		System.out.println("");
		System.out.println("Total Commits With AST Change = " + totalASTModifiedCommits.size());
		System.out.println("Total File With AST Change = " + totalASTModifiedFiles);
		System.out.println("");
		System.out.println("Multi-Context-Commit = " + multiContextImprovedCommits.size());
		System.out.println("Control-Context-Commit = " + conContextImprovedCommits.size());
		System.out.println("Environment-Context-Commit = " + envContextImprovedCommits.size());
		System.out.println("Value-Context-Commit = " + valContextImprovedCommits.size());
		System.out.println("");
		System.out.println("Multi-AvgFactsAdded-Commit = " + multiFactsAdded/multiContextImprovedCommits.size());
		System.out.println("Control-AvgFactsAdded-Commit = " + conFactsAdded/conContextImprovedCommits.size());
		System.out.println("Environment-AvgFactsAdded-Commit = " + envFactsAdded/envContextImprovedCommits.size());
		System.out.println("Value-AvgFactsAdded-Commit = " + envFactsAdded/valContextImprovedCommits.size());
		System.out.println("");
		System.out.println("Multi-Context-FilePair = " + multiContextImprovedFilePairs);
		System.out.println("Control-Context-FilePair = " + conContextImprovedFilePairs);
		System.out.println("Environment-Context-FilePair = " + envContextImprovedFilePairs);
		System.out.println("Value-Context-FilePair = " + valContextImprovedFilePairs);
		System.out.println("");
		System.out.println("Multi-AvgFactsAdded-FilePair = " + multiFactsAdded/multiContextImprovedFilePairs);
		System.out.println("Control-AvgFactsAdded-FilePair = " + conFactsAdded/conContextImprovedFilePairs);
		System.out.println("Environment-AvgFactsAdded-FilePair = " + envFactsAdded/envContextImprovedFilePairs);
		System.out.println("Value-AvgFactsAdded-FilePair = " + valFactsAdded/valContextImprovedFilePairs);
		System.out.println("");
		System.out.println("Multi-Line-Commit = " + multiLineImprovedCommits.size());
		System.out.println("Control-Line-Commit = " + conLineImprovedCommits.size());
		System.out.println("Environment-Line-Commit = " + envLineImprovedCommits.size());
		System.out.println("Value-Line-Commit = " + valLineImprovedCommits.size());
		System.out.println("");
		System.out.println("Multi-AvgLinesAdded-Commit = " + multiLinesAdded/multiLineImprovedCommits.size());
		System.out.println("Control-AvgLinesAdded-Commit = " + conLinesAdded/conLineImprovedCommits.size());
		System.out.println("Environment-AvgLinesAdded-Commit = " + envLinesAdded/envLineImprovedCommits.size());
		System.out.println("Value-AvgLinesAdded-Commit = " + valLinesAdded/valLineImprovedCommits.size());
		System.out.println("");
		System.out.println("Multi-Line-FilePair = " + multiLineImprovedFilePairs);
		System.out.println("Control-Line-FilePair = " + conLineImprovedFilePairs);
		System.out.println("Environment-Line-FilePair = " + envLineImprovedFilePairs);
		System.out.println("Value-Line-FilePair = " + valLineImprovedFilePairs);
		System.out.println("");
		System.out.println("Multi-AvgLinesAdded-FilePair = " + multiLinesAdded/multiLineImprovedFilePairs);
		System.out.println("Control-AvgLinesAdded-FilePair = " + conLinesAdded/conLineImprovedFilePairs);
		System.out.println("Environment-AvgLinesAdded-FilePair = " + envLinesAdded/envLineImprovedFilePairs);
		System.out.println("Value-AvgLinesAdded-FilePair = " + valLinesAdded/valLineImprovedFilePairs);

	}

}
