package ca.ubc.ece.salt.pangor.classify;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintStream;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import ca.ubc.ece.salt.pangor.analysis.DataSet;
import ca.ubc.ece.salt.pangor.classify.alert.ClassifierAlert;
import ca.ubc.ece.salt.pangor.classify.alert.DeserializedClassifierAlert;

/**
 * The {@code DataSet} manages the alerts that were generated during the
 * analysis.
 */
public class ClassifierDataSet implements DataSet<ClassifierAlert> {

	/**
	 * The path to the file where the data set will be cached. This allows us
	 * to limit our memory use and cache results for the future by storing the
	 * keyword extraction results on the disk.
	 */
	private String dataSetPath;

	/**
	 * The path to the folder where any supplementary files should be stored.
	 * These files contain the function source code from the alerts.
	 */
	private String supplementaryPath;

	/** The alerts generated by the analysis. **/
	private Set<ClassifierAlert> alerts;

	/**
	 * Used to produce a data set of the analysis results.
	 * @param dataSetPath The file path to store the data set.
	 * @param supplementaryPath The directory path to store the supplementary
	 * 		  files.
	 * @throws Exception Throws an exception when the {@code dataSetPath}
	 * 					 cannot be read.
	 */
	public ClassifierDataSet(String dataSetPath, String supplementaryPath) {
		this.alerts = new HashSet<ClassifierAlert>();
		this.dataSetPath = dataSetPath;
		this.supplementaryPath = supplementaryPath;
	}

	/**
	 * Used to read a data set from a file on disk.
	 * @param dataSetPath The file path to read the data set from.
	 * @throws Exception
	 */
	public ClassifierDataSet(String dataSetPath) throws Exception {
		this.alerts = new HashSet<ClassifierAlert>();
		this.dataSetPath = dataSetPath;

		/* Read the data set file and de-serialize the feature vectors. */
		this.importDataSet(dataSetPath);
	}

	/**
	 * Import a data set from a file to this {@code ClassifierDataSet}.
	 * @param dataSetPath The file path where the data set is stored.
	 * @throws Exception Occurs when the data set file cannot be read.
	 */
	public void importDataSet(String dataSetPath) throws Exception {

		try(BufferedReader reader = new BufferedReader(new FileReader(dataSetPath))) {

			for (String serialAlert = reader.readLine();
					serialAlert != null;
					serialAlert = reader.readLine()) {

				ClassifierAlert alert = DeserializedClassifierAlert.deSerialize(serialAlert);

				this.alerts.add(alert);

			}

		}
		catch(Exception e) {
			throw e;
		}

	}

	/**
	 * Adds a alert to the data set. If a data set file exists
	 * ({@code dataSetPath}), serializes the alert and writes it to
	 * the file. Otherwise, the alert is stored in memory in
	 * {@code LearningDataSet}.
	 * @param alert The alert to be managed by this class.
	 */
	@Override
	public void registerAlert(ClassifierAlert alert) throws Exception {

		if(this.dataSetPath != null) {
			this.storeAlert(alert);
		}
		else {
			this.alerts.add(alert);
		}

	}

	/**
	 * Stores the alert in the file specified by {@code dataSetPath}.
	 * This method is synchronized because it may be used by several
	 * GitProjectAnalysis thread at the same time, which may cause race
	 * conditions when writing to the output file.
	 *
	 * @param alert The alert to be managed by this class.
	 */
	private synchronized void storeAlert(ClassifierAlert alert) throws Exception {

		/* The path to the file may not exist. Create it if needed. */
		File path = new File(this.dataSetPath);
		path.getParentFile().mkdirs();
		path.createNewFile();

		/* May throw IOException if the path does not exist. */
		PrintStream stream = new PrintStream(new FileOutputStream(path, true));

		/* Write the data set. */
		stream.println(alert.serialize());

		/* Finished writing the alert. */
		stream.close();

		/* Write the source code to a folder so we can excommitne it later. */
		this.printSupplementaryFiles(alert);

	}

	/**
	 * Builds the alert header by filtering out features (columns)
	 * that are not used or hardly used.
	 * @return The alert header as a CSV list.
	 */
	public String getAlertHeader() {

		String header = String.join(",", "ID", "ProjectID", "ProjectHomepage", "BuggyFile",
				"RepairedFile", "BuggyCommitID", "RepairedCommitID",
				"FunctionName");

		return header;

	}

	/**
	 * @return The list of alerts in this data set.
	 */
	public List<ClassifierAlert> getAlerts() {
		return new LinkedList<ClassifierAlert>(this.alerts);
	}

	/**
	 * Perform operations that prepare the data set for analysis.
	 */
	public void preProcess() { }

	/**
	 * Print the data set to a file. The filtered data set will be in a CSV
	 * format that can be imported directly into Weka.
	 * @param outFile The file to write the filtered data set to.
	 */
	public void writeFilteredDataSet(String outFile) {

		/* Open the file stream for writing if a file has been given. */
		PrintStream stream = System.out;

		if(outFile != null) {
			try {
				/*
				 * The path to the output folder may not exist. Create it if
				 * needed.
				 */
				File path = new File(outFile);
				path.getParentFile().mkdirs();

				stream = new PrintStream(new FileOutputStream(outFile));
			}
			catch (IOException e) {
				System.err.println(e.getMessage());
			}
		}

		/* Write the header for the feature vector. */
		stream.println(this.getAlertHeader());

		/* Write the data set. */
		stream.println(this.getAlertVector());

	}

	/**
	 * Computes metrics about the data set:
	 *
	 *	Type Count: A ranked list of the classifer alert types.
	 *
	 *	Subtype Count: A ranked list of the classifer alert subtypes.
	 *
	 * @return The metrics for the data set (in the {@code ClassifierMetrics}
	 * 		   object).
	 */
	public ClassifierMetrics getMetrics() {

		/* The metrics object. */
		ClassifierMetrics metrics = new ClassifierMetrics();

		/* Compute the frequency of keywords. */
		Map<String, Integer> typeCount = new HashMap<String, Integer>();
		Map<String, Integer> subtypeCount = new HashMap<String, Integer>();
		for(ClassifierAlert alert : this.alerts) {

			/* Increment the type/subtype that appears in this alert. */
			Integer tc = typeCount.get(alert.getType());
			tc = tc == null ? 1 : tc + 1;
			typeCount.put(alert.getType(), tc);

			Integer sc = subtypeCount.get(alert.getType() + "_" + alert.getSubType());
			sc = sc == null ? 1 : sc + 1;
			subtypeCount.put(alert.getType() + "_" + alert.getSubType(), sc);

		}

		/* Create the ordered set of alert types. */
		for(String typeName : typeCount.keySet()) {
			metrics.addTypeCount(typeName, typeCount.get(typeName));
		}

		for(String subtypeName : subtypeCount.keySet()) {
			metrics.addSubTypeCount(subtypeName, subtypeCount.get(subtypeName));
		}


		return metrics;

	}

	/**
	 * Builds the data set by writing all alerts to a string.
	 * @return The data set as a CSV file.
	 */
	public String getAlertVector() {

		String dataSet = "";

		for(ClassifierAlert alert : this.alerts) {
			dataSet += alert.serialize() + "\n";
		}

		return dataSet;

	}

	/**
	 * Writes the source code from each of the inspected functions to a file.
	 * @param supplementaryFolder The folder to place the files in.
	 */
	private void printSupplementaryFiles(ClassifierAlert alert) {

		/* The path to the supplementary folder may not exist. Create
		 * it if needed. */
		File path = new File(this.supplementaryPath);
		path.mkdirs();

		File src = new File(this.supplementaryPath, alert.id + "_src.js");
		File dst = new File(this.supplementaryPath, alert.id + "_dst.js");

		try (PrintStream srcStream = new PrintStream(new FileOutputStream(src));
			 PrintStream dstStream = new PrintStream(new FileOutputStream(dst));) {

			srcStream.print(alert.sourceCodeFileChange.buggyCode);
			dstStream.print(alert.sourceCodeFileChange.repairedCode);

			srcStream.close();
			dstStream.close();

		} catch (FileNotFoundException e) {
			System.err.println(e.getMessage());
		}

	}

}