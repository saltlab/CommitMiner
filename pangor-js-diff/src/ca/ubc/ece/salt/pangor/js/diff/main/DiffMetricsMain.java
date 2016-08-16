package ca.ubc.ece.salt.pangor.js.diff.main;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.PrintStream;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import ca.ubc.ece.salt.pangor.js.diff.main.SourceFile.DiffType;

public class DiffMetricsMain {

	public static void main(String[] args) throws Exception{
		DiffMetricsMain metrics = new DiffMetricsMain("./output/dataset_2016-08-14.csv");
		metrics.analyze();
	}


	private String datasetPath;
	private Map<FV, SourceFilePair> sourceFilePairs;

	public DiffMetricsMain(String datasetPath) {
		this.datasetPath = datasetPath;
		this.sourceFilePairs = new HashMap<FV, SourceFilePair>();
	}

	private void analyze() throws Exception{

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
			pair.lineChanges = sfp.getFactSize(DiffType.LINE);
			pair.astChanges = sfp.getFactSize(DiffType.AST);
			pair.conChanges = sfp.getFactSize(DiffType.CONTROL);
			pair.envChanges = sfp.getFactSize(DiffType.ENVIRONMENT);
			pair.valChanges = sfp.getFactSize(DiffType.VALUE);
			pair.multiChanges = sfp.getFactSize(DiffType.MULTI);
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
			source.lineChanges = sfp.source.getFactSize(DiffType.LINE);
			source.astChanges = sfp.source.getFactSize(DiffType.AST);
			source.conChanges = sfp.source.getFactSize(DiffType.CONTROL);
			source.envChanges = sfp.source.getFactSize(DiffType.ENVIRONMENT);
			source.valChanges = sfp.source.getFactSize(DiffType.VALUE);
			source.multiChanges = sfp.source.getFactSize(DiffType.MULTI);
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
			destination.lineChanges = sfp.destination.getFactSize(DiffType.LINE);
			destination.astChanges = sfp.destination.getFactSize(DiffType.AST);
			destination.conChanges = sfp.destination.getFactSize(DiffType.CONTROL);
			destination.envChanges = sfp.destination.getFactSize(DiffType.ENVIRONMENT);
			destination.valChanges = sfp.destination.getFactSize(DiffType.VALUE);
			destination.multiChanges = sfp.destination.getFactSize(DiffType.MULTI);
			destination.multiChanges = sfp.destination.getFactSize(DiffType.MULTI);
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


	private void printMetrics(List<SourceFileDiffComparison> sourceComparisons) throws Exception {

		List<MetricsRow> fileFactMetrics = new LinkedList<MetricsRow>();
		List<MetricsRow> fileLineMetrics = new LinkedList<MetricsRow>();

		Map<String, MetricsRow> commitFactMetrics = new HashMap<String, MetricsRow>();
		Map<String, MetricsRow> commitLineMetrics = new HashMap<String, MetricsRow>();

		Set<String> repeatedFiles = new HashSet<String>();

		Set<String> totalCommits = new HashSet<String>();
		Set<String> totalASTModifiedCommits = new HashSet<String>();
		int totalASTModifiedFiles = 0;

		/* Count the ast facts each time there is a multi-diff fact added. */
		int astFactsAdded = 0;
		int multiASTFactsAdded = 0;
		int conASTFactsAdded = 0;
		int envASTFactsAdded = 0;
		int valASTFactsAdded = 0;

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

			String commit = source.project + "~" + source.commit;

			/* For computing the total number of commits. */
			totalCommits.add(source.commit);

			/* For computing the total number of commits with at least one
			 * AST change. */
			if(source.astChanges > 0) {
				totalASTModifiedCommits.add(source.commit);
				totalASTModifiedFiles++;
				astFactsAdded += source.astChanges;
			}

			/* Check if the file is line-improved. */
			if(source.multiAstSubtraction > 0) {
				multiLineImprovedCommits.add(source.project + "~" + source.commit);
				multiLineImprovedFilePairs++;
				multiLinesAdded += source.multiAstSubtraction;

				addFileFactsToCommit(commitLineMetrics, commit,
									 MetricsRow.Type.ALL,
									 source.multiAstSubtraction,
									 source.astChanges);

				fileLineMetrics.add(new MetricsRow(MetricsRow.Type.ALL,
													source.multiAstSubtraction,
													source.astChanges));
			}
			if(source.conAstSubtraction > 0) {
				conLineImprovedCommits.add(source.project + "~" + source.commit);
				conLineImprovedFilePairs++;
				conLinesAdded += source.conAstSubtraction;

				addFileFactsToCommit(commitLineMetrics, commit,
									 MetricsRow.Type.CONTROL,
									 source.conAstSubtraction,
									 source.astChanges);

				fileLineMetrics.add(new MetricsRow(MetricsRow.Type.CONTROL,
													source.conAstSubtraction,
													source.astChanges));
			}
			if(source.envAstSubtraction > 0) {
				envLineImprovedCommits.add(source.project + "~" + source.commit);
				envLineImprovedFilePairs++;
				envLinesAdded += source.envAstSubtraction;

				addFileFactsToCommit(commitLineMetrics, commit,
									 MetricsRow.Type.ENVIRONMENT,
									 source.envAstSubtraction,
									 source.astChanges);

				fileLineMetrics.add(new MetricsRow(MetricsRow.Type.ENVIRONMENT,
													source.envAstSubtraction,
													source.astChanges));
			}
			if(source.valAstSubtraction > 0) {
				valLineImprovedCommits.add(source.project + "~" + source.commit);
				valLineImprovedFilePairs++;
				valLinesAdded += source.valAstSubtraction;

				addFileFactsToCommit(commitLineMetrics, commit,
									 MetricsRow.Type.VALUE,
									 source.valAstSubtraction,
									 source.astChanges);

				fileLineMetrics.add(new MetricsRow(MetricsRow.Type.VALUE,
													source.valAstSubtraction,
													source.astChanges));
			}

			/* Check if the file is context-improved. */
			if(source.multiChanges > 0) {
				multiContextImprovedCommits.add(source.project + "~" + source.commit);
				multiContextImprovedFilePairs++;
				multiFactsAdded += source.multiChanges;
				multiASTFactsAdded += source.astChanges;

				addFileFactsToCommit(commitFactMetrics, commit,
									 MetricsRow.Type.ALL,
									 source.multiChanges,
									 source.astChanges);

				fileFactMetrics.add(new MetricsRow(MetricsRow.Type.ALL,
													source.multiChanges,
													source.astChanges));
			}
			if(source.conChanges > 0) {
				conContextImprovedCommits.add(source.project + "~" + source.commit);
				conContextImprovedFilePairs++;
				conFactsAdded += source.conChanges;
				conASTFactsAdded += source.astChanges;

				addFileFactsToCommit(commitFactMetrics, commit,
									 MetricsRow.Type.CONTROL,
									 source.conChanges,
									 source.astChanges);

				fileFactMetrics.add(new MetricsRow(MetricsRow.Type.CONTROL,
													source.conChanges,
													source.astChanges));
			}
			if(source.envChanges > 0) {
				envContextImprovedCommits.add(source.project + "~" + source.commit);
				envContextImprovedFilePairs++;
				envFactsAdded += source.envChanges;
				envASTFactsAdded += source.astChanges;

				addFileFactsToCommit(commitFactMetrics, commit,
									 MetricsRow.Type.ENVIRONMENT,
									 source.envChanges,
									 source.astChanges);

				fileFactMetrics.add(new MetricsRow(MetricsRow.Type.ENVIRONMENT,
													source.envChanges,
													source.astChanges));
			}
			if(source.valChanges > 0) {
				valContextImprovedCommits.add(source.project + "~" + source.commit);
				valContextImprovedFilePairs++;
				valFactsAdded += source.valChanges;
				valASTFactsAdded += source.astChanges;

				addFileFactsToCommit(commitFactMetrics, commit,
									 MetricsRow.Type.VALUE,
									 source.valChanges,
									 source.astChanges);

				fileFactMetrics.add(new MetricsRow(MetricsRow.Type.VALUE,
													source.valChanges,
													source.astChanges));
			}

		}

		/* Write the data to a file. */
		this.writeMetrics("chart_file_line_metrics.csv", fileLineMetrics);
		this.writeMetrics("chart_file_fact_metrics.csv", fileFactMetrics);
		this.writeMetrics("chart_commit_line_metrics.csv", commitLineMetrics.values());
		this.writeMetrics("chart_commit_fact_metrics.csv", commitFactMetrics.values());

		double multiContextCommit = (double)multiContextImprovedCommits.size()/(double)totalASTModifiedCommits.size();
		double controlContextCommit = (double)conContextImprovedCommits.size()/(double)totalASTModifiedCommits.size();
		double environmentContextCommit = (double)envContextImprovedCommits.size()/(double)totalASTModifiedCommits.size();
		double valueContextCommit = (double)valContextImprovedCommits.size()/(double)totalASTModifiedCommits.size();

		double multiAvgFactsAddedCommit = (double)multiFactsAdded/(double)multiContextImprovedCommits.size();
		double controlAvgFactsAddedCommit = (double)conFactsAdded/(double)conContextImprovedCommits.size();
		double environmentAvgFactsAddedCommit = (double)envFactsAdded/(double)envContextImprovedCommits.size();
		double valueAvgFactsAddedCommit = (double)valFactsAdded/(double)valContextImprovedCommits.size();

		double multiAvgASTFactsAddedCommit = (double)multiASTFactsAdded/(double)multiContextImprovedCommits.size();
		double controlAvgASTFactsAddedCommit = (double)conASTFactsAdded/(double)conContextImprovedCommits.size();
		double environmentAvgASTFactsAddedCommit = (double)envASTFactsAdded/(double)envContextImprovedCommits.size();
		double valueAvgASTFactsAddedCommit = (double)valASTFactsAdded/(double)valContextImprovedCommits.size();

		double multiContextFilePair = (double)multiContextImprovedFilePairs/(double)totalASTModifiedFiles;
		double controlContextFilePair = (double)conContextImprovedFilePairs/(double)totalASTModifiedFiles;
		double environmentContextFilePair = (double)envContextImprovedFilePairs/(double)totalASTModifiedFiles;
		double valueContextFilePair = (double)valContextImprovedFilePairs/(double)totalASTModifiedFiles;

		double multiAvgFactsAddedFilePair = (double)multiFactsAdded/(double)multiContextImprovedFilePairs;
		double controlAvgFactsAddedFilePair = (double)conFactsAdded/(double)conContextImprovedFilePairs;
		double environmentAvgFactsAddedFilePair = (double)envFactsAdded/(double)envContextImprovedFilePairs;
		double valueAvgFactsAddedFilePair = (double)valFactsAdded/(double)valContextImprovedFilePairs;

		double astAvgASTFactsAddedFilePair = (double)astFactsAdded/(double)totalASTModifiedFiles;
		double multiAvgASTFactsAddedFilePair = (double)multiASTFactsAdded/(double)multiContextImprovedFilePairs;
		double controlAvgASTFactsAddedFilePair = (double)conASTFactsAdded/(double)conContextImprovedFilePairs;
		double environmentAvgASTFactsAddedFilePair = (double)envASTFactsAdded/(double)envContextImprovedFilePairs;
		double valueAvgASTFactsAddedFilePair = (double)valASTFactsAdded/(double)valContextImprovedFilePairs;

		double multiLineCommit = (double)multiLineImprovedCommits.size()/(double)totalASTModifiedCommits.size();
		double controlLineCommit = (double)conLineImprovedCommits.size()/(double)totalASTModifiedCommits.size();
		double environmentLineCommit = (double)envLineImprovedCommits.size()/(double)totalASTModifiedCommits.size();
		double valueLineCommit = (double)valLineImprovedCommits.size()/(double)totalASTModifiedCommits.size();

		double multiAvgLinesAddedCommit = (double)multiLinesAdded/(double)multiLineImprovedCommits.size();
		double controlAvgLinesAddedCommit = (double)conLinesAdded/(double)conLineImprovedCommits.size();
		double environmentAvgLinesAddedCommit = (double)envLinesAdded/(double)envLineImprovedCommits.size();
		double valueAvgLinesAddedCommit = (double)valLinesAdded/(double)valLineImprovedCommits.size();

		double multiLineFilePair = (double)multiLineImprovedFilePairs/(double)totalASTModifiedFiles;
		double controlLineFilePair = (double)conLineImprovedFilePairs/(double)totalASTModifiedFiles;
		double environmentLineFilePair = (double)envLineImprovedFilePairs/(double)totalASTModifiedFiles;
		double valueLineFilePair = (double)valLineImprovedFilePairs/(double)totalASTModifiedFiles;

		double multiAvgLinesAddedFilePair = (double)multiLinesAdded/(double)multiLineImprovedFilePairs;
		double controlAvgLinesAddedFilePair = (double)conLinesAdded/(double)conLineImprovedFilePairs;
		double environmentAvgLinesAddedFilePair = (double)envLinesAdded/(double)envLineImprovedFilePairs;
		double valueAvgLinesAddedFilePair = (double)valLinesAdded/(double)valLineImprovedFilePairs;

		System.out.println("Total Commits Analyzed = " + totalCommits.size());
		System.out.println("Total Files Analyzed = " + sourceComparisons.size());
		System.out.println("");
		System.out.println("Total Commits With AST Change = " + totalASTModifiedCommits.size());
		System.out.println("Total File With AST Change = " + totalASTModifiedFiles);
		System.out.println("");
		System.out.println("Multi-Context-Commit = " + multiContextCommit);
		System.out.println("Control-Context-Commit = " + controlContextCommit);
		System.out.println("Environment-Context-Commit = " + environmentContextCommit);
		System.out.println("Value-Context-Commit = " + valueContextCommit);
		System.out.println("");
		System.out.println("Multi-AvgFactsAdded-Commit = " + multiAvgFactsAddedCommit);
		System.out.println("Control-AvgFactsAdded-Commit = " + controlAvgFactsAddedCommit);
		System.out.println("Environment-AvgFactsAdded-Commit = " + environmentAvgFactsAddedCommit);
		System.out.println("Value-AvgFactsAdded-Commit = " + valueAvgFactsAddedCommit);
		System.out.println("");
		System.out.println("Multi-AvgASTFactsAdded-Commit = " + multiAvgASTFactsAddedCommit);
		System.out.println("Control-AvgASTFactsAdded-Commit = " + controlAvgASTFactsAddedCommit);
		System.out.println("Environment-AvgASTFactsAdded-Commit = " + environmentAvgASTFactsAddedCommit);
		System.out.println("Value-AvgASTFactsAdded-Commit = " + valueAvgASTFactsAddedCommit);
		System.out.println("");
		System.out.println("Multi-Context-FilePair = " + multiContextFilePair);
		System.out.println("Control-Context-FilePair = " + controlContextFilePair);
		System.out.println("Environment-Context-FilePair = " + environmentContextFilePair);
		System.out.println("Value-Context-FilePair = " + valueContextFilePair);
		System.out.println("");
		System.out.println("Multi-AvgFactsAdded-FilePair = " + multiAvgFactsAddedFilePair);
		System.out.println("Control-AvgFactsAdded-FilePair = " + controlAvgFactsAddedFilePair);
		System.out.println("Environment-AvgFactsAdded-FilePair = " + environmentAvgFactsAddedFilePair);
		System.out.println("Value-AvgFactsAdded-FilePair = " + valueAvgFactsAddedFilePair);
		System.out.println("");
		System.out.println("AST-AvgASTFactsAdded-FilePair = " + astAvgASTFactsAddedFilePair);
		System.out.println("Multi-AvgASTFactsAdded-FilePair = " + multiAvgASTFactsAddedFilePair);
		System.out.println("Control-AvgASTFactsAdded-FilePair = " + controlAvgASTFactsAddedFilePair);
		System.out.println("Environment-AvgASTFactsAdded-FilePair = " + environmentAvgASTFactsAddedFilePair);
		System.out.println("Value-AvgASTFactsAdded-FilePair = " + valueAvgASTFactsAddedFilePair);
		System.out.println("");
		System.out.println("Multi-Line-Commit = " + multiLineCommit);
		System.out.println("Control-Line-Commit = " + controlLineCommit);
		System.out.println("Environment-Line-Commit = " + environmentLineCommit);
		System.out.println("Value-Line-Commit = " + valueLineCommit);
		System.out.println("");
		System.out.println("Multi-AvgLinesAdded-Commit = " + multiAvgLinesAddedCommit);
		System.out.println("Control-AvgLinesAdded-Commit = " + controlAvgLinesAddedCommit);
		System.out.println("Environment-AvgLinesAdded-Commit = " + environmentAvgLinesAddedCommit);
		System.out.println("Value-AvgLinesAdded-Commit = " + valueAvgLinesAddedCommit);
		System.out.println("");
		System.out.println("Multi-Line-FilePair = " + multiLineFilePair);
		System.out.println("Control-Line-FilePair = " + controlLineFilePair);
		System.out.println("Environment-Line-FilePair = " + environmentLineFilePair);
		System.out.println("Value-Line-FilePair = " + valueLineFilePair);
		System.out.println("");
		System.out.println("Multi-AvgLinesAdded-FilePair = " + multiAvgLinesAddedFilePair);
		System.out.println("Control-AvgLinesAdded-FilePair = " + controlAvgLinesAddedFilePair);
		System.out.println("Environment-AvgLinesAdded-FilePair = " + environmentAvgLinesAddedFilePair);
		System.out.println("Value-AvgLinesAdded-FilePair = " + valueAvgLinesAddedFilePair);
		System.out.println("");

		System.out.println("chart_context_lines.csv");
		System.out.println("Diff, ContextCommit, ContextFile, LineCommit, LineFile");
		System.out.println("All," + multiContextCommit + "," + multiContextFilePair + "," + multiLineCommit + "," + multiLineFilePair);
		System.out.println("Ctrl," + controlContextCommit + "," + controlContextFilePair + "," + controlLineCommit + "," + controlLineFilePair);
		System.out.println("Env," + environmentContextCommit + "," + environmentContextFilePair + "," + environmentLineCommit + "," + environmentLineFilePair);
		System.out.println("Val," + valueContextCommit + "," + valueContextFilePair + "," + valueLineCommit + "," + valueLineFilePair);

		System.out.println("chart_facts_lines.csv");
		System.out.println("Diff, ContextCommit, ContextFile, LineCommit, LineFile, ASTCommit, ASTFile");
		System.out.println("All," + multiAvgFactsAddedCommit + "," + multiAvgFactsAddedFilePair + "," + multiAvgLinesAddedCommit + "," + multiAvgLinesAddedFilePair + "," + multiAvgASTFactsAddedCommit + "," + astAvgASTFactsAddedFilePair);
		System.out.println("Ctrl," + controlAvgFactsAddedCommit + "," + controlAvgFactsAddedFilePair + "," + controlAvgLinesAddedCommit + "," + controlAvgLinesAddedFilePair + "," + controlAvgASTFactsAddedCommit + "," + controlAvgASTFactsAddedFilePair);
		System.out.println("Env," + environmentAvgFactsAddedCommit + "," + environmentAvgFactsAddedFilePair + "," + environmentAvgLinesAddedCommit + "," + environmentAvgLinesAddedFilePair + "," + environmentAvgASTFactsAddedCommit + "," + environmentAvgASTFactsAddedFilePair);
		System.out.println("Val," + valueAvgFactsAddedCommit + "," + valueAvgFactsAddedFilePair + "," + valueAvgLinesAddedCommit + "," + valueAvgLinesAddedFilePair + "," + valueAvgASTFactsAddedCommit + "," + valueAvgASTFactsAddedFilePair);

	}

	private void addFileFactsToCommit(Map<String, MetricsRow> map,
									  String commit,
									  MetricsRow.Type type,
									  int added, int ast) {

		String key = commit + "~" + type.toString();
		MetricsRow row = map.get(key);

		if(row == null) {
			row = new MetricsRow(type, added, ast);
			map.put(key, row);
		}
		else {
			if(row.type != type) throw new Error("Map type does not match input type.");
			row.added += added;
			row.ast += ast;
		}

	}

	/**
	 * Creates a csv file for building boxplots in R
	 */
	private void writeMetrics(String fileName, Collection<MetricsRow> rows) throws Exception {

		/* The path to the file may not exist. Create it if needed. */
		File path = new File("/Users/qhanam/Repositories/quinn-papers/multi-diff/" + fileName);
		path.getParentFile().mkdirs();
		path.delete();
		path.createNewFile();

		/* May throw IOException if the path does not exist. */
		PrintStream stream = new PrintStream(new FileOutputStream(path, true));

		stream.println("Type,Added,AST");

		/* Write the data set. */
		for(MetricsRow row : rows) {
			stream.println(row.serialize());
		}

		/* Finished writing the feature vector. */
		stream.close();

	}

}
