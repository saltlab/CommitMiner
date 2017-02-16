package ca.ubc.ece.salt.pangor.js.diff.main;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import ca.ubc.ece.salt.pangor.js.diff.main.SourceFile.DiffType;

public class DiffSearchMain {

	public static void main(String[] args) throws Exception{
		DiffSearchMain metrics = new DiffSearchMain("./output/dataset_2016-08-26.csv");
		metrics.analyze();
	}


	private String datasetPath;
	private Map<FV, SourceFilePair> sourceFilePairs;

	public DiffSearchMain(String datasetPath) {
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

		Map<SourceFileDiffComparison, Integer> sem = new HashMap<SourceFileDiffComparison, Integer>();

		/* Compute the metrics for each file. */
		List<SourceFileDiffComparison> destinationComparisons = new LinkedList<SourceFileDiffComparison>();
		for(SourceFilePair sfp : sourceFilePairs.values()) {

			/* Destination */
			SourceFileDiffComparison destination = new SourceFileDiffComparison();
			destination.project = sfp.project;
			destination.commit = sfp.commit;
			destination.file = sfp.file;
			destination.url = sfp.url;
			destination.totalLines = sfp.destination.getTotalLines();
			destination.lineChanges = sfp.destination.getFactSize(DiffType.LINE);
			destination.astChanges = sfp.destination.getFactSize(DiffType.AST);
			destinationComparisons.add(destination);

			Integer cnt = sem.get(destination);
			if(cnt == null) cnt = 1;
			else cnt = cnt + 1;
			sem.put(destination, cnt);

//			if(destination.project.equals("pm2")) {
//				System.out.println(destination.url + " " + destination.project + " " + destination.file + " " + destination.commit + " " + destination.lineChanges);
//			}

		}

		System.out.println("START");

		for(Map.Entry<SourceFileDiffComparison, Integer> entry : sem.entrySet()) {
			if(entry.getValue() == 1 && entry.getKey().lineChanges > 10 && entry.getKey().lineChanges < 30) {
				System.out.println(entry.getKey().url + " " + entry.getKey().lineChanges);
			}
		}

//		/* Print the metrics. */
//		System.out.println("\n--Destination Metrics--");
//		printMetrics(destinationComparisons);

	}

}
