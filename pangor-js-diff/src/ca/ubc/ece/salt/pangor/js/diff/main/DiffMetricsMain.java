package ca.ubc.ece.salt.pangor.js.diff.main;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.HashSet;
import java.util.Set;

public class DiffMetricsMain {

	public static void main(String[] args) {
		DiffMetricsMain metrics = new DiffMetricsMain("./output/dataset_2016-07-28.csv");
		metrics.analyze();
	}


	private String datasetPath;
	private Set<SourceFilePair> sourceFilePairs;

	public DiffMetricsMain(String datasetPath) {
		this.datasetPath = datasetPath;
		this.sourceFilePairs = new HashSet<SourceFilePair>();
	}

	private void analyze() {

		try(BufferedReader reader = new BufferedReader(new FileReader(datasetPath))) {

			for (String line = reader.readLine();
					line != null;
					line = reader.readLine()) {

				String[] tokens = line.split(",");

				FV fv = new FV(tokens);
				System.out.println(fv.line);

			}

		}
		catch(Exception e) {
			System.err.println(e);
		}

	}

	/**
	 * Adds the fact to the correct SourceFilePair
	 * @param fv the feature vector read from the dataset.
	 */
	public void interpretFV(FV fv) {

	}

}
