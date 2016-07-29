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
		System.out.println("--Source Metrics--");
		printMetrics(sourceComparisons);
		System.out.println("--Destination Metrics--");
		printMetrics(destinationComparisons);

	}

	private void printMetrics(List<SourceFileDiffComparison> sourceComparisons) {

		double astLineAvg = 0;
		double conAstAvg = 0;
		double envAstAvg = 0;
		double valAstAvg = 0;

		int conCnt = 0;
		int envCnt = 0;
		int valCnt = 0;
		int anyCnt = 0;

		for(SourceFileDiffComparison source : sourceComparisons) {
			astLineAvg += source.astLineSubtraction;
			conAstAvg += source.conAstSubtraction;
			envAstAvg += source.envAstSubtraction;
			valAstAvg += source.valAstSubtraction;

			if(source.conAstSubtraction > 0) conCnt++;
			if(source.envAstSubtraction > 0) envCnt++;
			if(source.valAstSubtraction > 0) valCnt++;
			if(source.conAstSubtraction > 0
					|| source.envAstSubtraction > 0
					|| source.valAstSubtraction > 0)
				anyCnt++;

//			System.out.println(source.astChanges + "," + source.conAstSubtraction);
		}

		if(sourceComparisons.size() > 0) {
			astLineAvg /= sourceComparisons.size();
			conAstAvg /= sourceComparisons.size();
			envAstAvg /= sourceComparisons.size();
			valAstAvg /= sourceComparisons.size();
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

	}

	private int subtract(Set<Integer> left, Set<Integer> right) {
		Set<Integer> tmp = new HashSet<Integer>(left);
		tmp.removeAll(right);
		return tmp.size();
	}

}
