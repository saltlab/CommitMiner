package ca.ubc.ece.salt.pangor.learn.analysis;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.tuple.Pair;
import org.deri.iris.EvaluationException;
import org.deri.iris.ProgramNotStratifiedException;
import org.deri.iris.RuleUnsafeException;
import org.deri.iris.api.IKnowledgeBase;
import org.deri.iris.api.basics.IQuery;
import org.deri.iris.api.basics.IRule;
import org.deri.iris.api.basics.ITuple;
import org.deri.iris.factory.Factory;
import org.deri.iris.storage.IRelation;

import weka.clusterers.DBSCAN;
import weka.core.Attribute;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.ManhattanDistance;
import weka.core.converters.ArffSaver;
import weka.filters.Filter;
import weka.filters.unsupervised.attribute.Remove;
import weka.filters.unsupervised.attribute.RemoveByName;
import ca.ubc.ece.salt.gumtree.ast.ClassifiedASTNode.ChangeType;
import ca.ubc.ece.salt.pangor.analysis.Commit;
import ca.ubc.ece.salt.pangor.analysis.Commit.Type;
import ca.ubc.ece.salt.pangor.analysis.DataSet;
import ca.ubc.ece.salt.pangor.api.KeywordDefinition;
import ca.ubc.ece.salt.pangor.api.KeywordDefinition.KeywordType;
import ca.ubc.ece.salt.pangor.api.KeywordUse;
import ca.ubc.ece.salt.pangor.api.KeywordUse.KeywordContext;
import ca.ubc.ece.salt.pangor.learn.ClusterMetrics;
import ca.ubc.ece.salt.pangor.learn.analysis.KeywordFilter.FilterType;

/**
 * The {@code LearningDataSet} manages the data set for data mining
 * and machine learning.
 *
 * Once all feature vectors have been built, they will contain some meta info
 * (commit {@link #clone()}, file, project, etc.) and zero or more
 * {@code Keyword}s (where a {@code Keyword} = name + context + package.
 *
 * The {@code LearningDataSet} is also responsible for creating feature vectors
 * by running datalog queries on the facts produced during the analysis.
 */
public class LearningDataSet extends DataSet {

	public static int ctr = 0;

	/**
	 * The packages we want to investigate. LearningFeatureVectorManager
	 * filters out any LearningFeatureVector which does not contain one of these
	 * packages.
	 */
	private List<KeywordFilter> rowFilters;

	/**
	 * The keywords we want to ommit as features from the data set during
	 * clustering.
	 */
	private List<KeywordUse> columnFilters;

	/**
	 * The path to the file where the data set will be cached. This allows us
	 * to limit our memory use and cache results for the future by storing the
	 * keyword extraction results on the disk.
	 */
	private String dataSetPath;

	/**
	 * The path to the file where the expected feature vector classes are
	 * stored. This file is produced by manual inspection and classification.
	 */
	private String oraclePath;

	/** The maximum number of modified statements for a feature vector. **/
	private int maxModifiedStatements;

	/** An ordered list of the keywords to print in the feature vector. **/
	private Set<KeywordDefinition> keywords;

	/** The feature vectors generated by the AST analysis. **/
	private List<LearningFeatureVector> featureVectors;

	/** The oracle containing expected feature vector classes. **/
	private Map<Integer, String> oracle;

	/** Weka-format data */
	private Instances wekaData;

	/**
	 * Used to produce a Weka data set. Create a {@code LearningDataSet} from
	 * a file on disk. This {@code LearningDataSet} can pre-process the data
	 * set and create a data set file for Weka.
	 * @param dataSetPath The file path to read the data set.
	 * @param oraclePath The file path to read the oracle (expected FV classes).
	 * @param rowFilters Filters out rows by requiring keywords to be present.
	 * @param maxModifiedStatements The maximum number of modified statements
	 * 								for a feature vector.
	 * @throws Exception Throws an exception when the {@code dataSetPath}
	 * 					 cannot be read.
	 */
	private LearningDataSet(String dataSetPath, String oraclePath,
							List<KeywordFilter> rowFilters,
							List<KeywordUse> columnFilters,
							int maxModifiedStatements) throws Exception {
		super(null, null);
		this.rowFilters = rowFilters;
		this.columnFilters = columnFilters;
		this.keywords = new HashSet<KeywordDefinition>();
		this.featureVectors = new LinkedList<LearningFeatureVector>();
		this.dataSetPath = dataSetPath;
		this.oraclePath = oraclePath;
		this.maxModifiedStatements = maxModifiedStatements;
		this.oracle = null;
		this.wekaData = null;

		/* Read the data set file and de-serialize the feature vectors. */
		this.importDataSet(dataSetPath);

		/* Read the oracle file and store the expected feature vector classes. */
		if(this.oraclePath != null) {
			this.oracle = new HashMap<Integer, String>();
			this.importOracle(oraclePath);
		}
	}

	/**
	 * Used for keyword analysis. Create a {@code LearningDataSet} to write
	 * the analysis results to disk.
	 * @param dataSetPath The file path to store the data set.
	 */
	private LearningDataSet(String dataSetPath, List<IRule> rules, List<IQuery> queries) {
		super(rules, queries);
		this.rowFilters = null;
		this.keywords = new HashSet<KeywordDefinition>();
		this.featureVectors = new LinkedList<LearningFeatureVector>();
		this.dataSetPath = dataSetPath;
		this.oraclePath = null;
		this.oracle = null;
		this.wekaData = null;
	}

	/**
	 * Used for testing. Creates a {@code LearningDataSet} that will add
	 * features directly to the data set (instead of writing them to a file).
	 * @param filters Filters out rows by requiring keywords to be present.
	 */
	private LearningDataSet(List<KeywordFilter> filters, List<IRule> rules, List<IQuery> queries) {
		super(rules, queries);
		this.rowFilters = filters;
		this.keywords = new HashSet<KeywordDefinition>();
		this.featureVectors = new LinkedList<LearningFeatureVector>();
		this.dataSetPath = null;
		this.oraclePath = null;
		this.oracle = null;
		this.wekaData = null;
	}

	/**
	 * Used to produce a Weka data set. Create a {@code LearningDataSet} from
	 * a file on disk. This {@code LearningDataSet} can pre-process the data
	 * set and create a data set file for Weka.
	 * @param dataSetPath The file path to read the data set.
	 * @param oraclePath The file path to read the oracle (expected FV classes).
	 * @param rowFilters Filters out rows by requiring keywords to be present.
	 * @param columnFilters The keywords we want to omit from the dataset
	 * 						during clustering.
	 * @param maxModifiedStatements The maximum number of modified statements
	 * 								for a feature vector.
	 * @throws Exception Throws an exception when the {@code dataSetPath}
	 * 					 cannot be read.
	 */
	public static LearningDataSet createLearningDataSet(String dataSetPath,
														String oraclePath,
														List<KeywordFilter> rowFilters,
														List<KeywordUse> columnFilters,
														int maxModifiedStatements) throws Exception {
		return new LearningDataSet(dataSetPath, oraclePath, rowFilters, columnFilters, maxModifiedStatements);
	}

	/**
	 * Creates a {@code LearningDataSet} performing the static analysis.
	 * @param dataSetPath The file path to store the data set.
	 * @return A new data set to store the analysis results.
	 */
	public static LearningDataSet createLearningDataSet(String dataSetPath) {
		return new LearningDataSet(dataSetPath, new LinkedList<IRule>(), getQueries());
	}

	/**
	 * Used for testing. Creates a {@code LearningDataSet} that will add
	 * features directly to the data set (instead of writing them to a file).
	 * @param filters Filters out rows by requiring keywords to be present.
	 */
	public static LearningDataSet createLearningDataSet(List<KeywordFilter> filters) {
		return new LearningDataSet(filters, new LinkedList<IRule>(), getQueries());
	}

	/**
	 * @return The list of queries for the test.
	 */
	private static List<IQuery> getQueries() {

		List<IQuery> queries = new LinkedList<IQuery>();
		queries.add(
			Factory.BASIC.createQuery(
				Factory.BASIC.createLiteral(true,
					Factory.BASIC.createPredicate("KeywordChange", 8),
					Factory.BASIC.createTuple(
						Factory.TERM.createVariable("Class"),
						Factory.TERM.createVariable("Method"),
						Factory.TERM.createVariable("KeywordType"),
						Factory.TERM.createVariable("KeywordContext"),
						Factory.TERM.createVariable("Package"),
						Factory.TERM.createVariable("ChangeType"),
						Factory.TERM.createVariable("Keyword"),
						Factory.TERM.createVariable("ID")))));

		queries.add(
			Factory.BASIC.createQuery(
				Factory.BASIC.createLiteral(true,
					Factory.BASIC.createPredicate("ModifiedStatementCount", 3),
					Factory.BASIC.createTuple(
						Factory.TERM.createVariable("Class"),
						Factory.TERM.createVariable("Method"),
						Factory.TERM.createVariable("Count")))));

		return queries;

	}

	/**
	 * Adds a feature vector to the data set. If a data set file exists
	 * ({@code dataSetPath}), serializes the feature vector and writes it to
	 * the file. Otherwise, the feature vector is stored in memory in
	 * {@code LearningDataSet}.
	 * @param featureVector The feature vector to be managed by this class.
	 * @throws EvaluationException
	 * @throws RuleUnsafeException
	 * @throws ProgramNotStratifiedException
	 */
	@Override
	protected void registerAlerts(Commit commit, IKnowledgeBase knowledgeBase) throws ProgramNotStratifiedException, RuleUnsafeException, EvaluationException {

		Map<String, LearningFeatureVector> featureVectors = new HashMap<String, LearningFeatureVector>();

		for(IQuery query : this.queries) {

			IRelation results = knowledgeBase.execute(query);

			/* Iterate through the tuples that are members of the relation and add
			 * them as alerts. */
			for(int i = 0; i < results.size(); i++) {

				ITuple tuple = results.get(i);

				/* Lookup or create the LearningFeatureVector. */
				String key = commit.projectID + "_" + commit.repairedCommitID 	// Identifies the commit
							 + "_" + tuple.get(0) + "_" + tuple.get(1); 		// Identifies the class/method
				LearningFeatureVector featureVector = featureVectors.get(key);

				/* Add the feature vector if it is not yet in the map. */
				if(featureVector == null) {
					featureVector = new LearningFeatureVector(commit,
							tuple.get(0).toString(),
							tuple.get(1).toString());
					featureVectors.put(key, featureVector);
				}

				/* Add the keyword or statement change to the bag of words. */
				if(query.toString().contains("KeywordChange")) {
					KeywordUse ku = new KeywordUse(
							KeywordType.valueOf(tuple.get(2).getValue().toString()),	// KeywordType
							KeywordContext.valueOf(tuple.get(3).getValue().toString()),	// KeywordContext
							tuple.get(6).getValue().toString(),							// Keyword
							ChangeType.valueOf(tuple.get(5).getValue().toString()),		// ChangeType
							tuple.get(4).getValue().toString());						// API String
					Integer count = featureVector.keywordMap.get(ku);
					count = count == null ? 1 : count + 1;
					featureVector.keywordMap.put(ku, count);
				}
				/* Update the number of modified statements in the feature vector. */
				if(query.toString().contains("ModifiedStatementCount")) {
					featureVector.modifiedStatementCount += Integer.parseInt(tuple.get(2).getValue().toString());
				}

			}

		}

		/* Store the feature vectors. */
		for(LearningFeatureVector featureVector : featureVectors.values()) {
			if(this.dataSetPath != null) {
				try {
					this.storeLearningFeatureVector(featureVector);
				} catch (Exception e) {
					System.err.println("Error while writing feature vector: " + e.getMessage());
				}
			}
			else {
				this.featureVectors.add(featureVector);
			}
		}

	}

	/**
	 * Import a data set from a file to this {@code LearningDataSet}.
	 * @param dataSetPath The file path where the data set is stored.
	 * @throws Exception Occurs when the data set file cannot be read.
	 */
	public void importDataSet(String dataSetPath) throws Exception {

		try(BufferedReader reader = new BufferedReader(new FileReader(dataSetPath))) {

			for (String serialLearningFeatureVector = reader.readLine();
					serialLearningFeatureVector != null;
					serialLearningFeatureVector = reader.readLine()) {

				LearningFeatureVector featureVector = LearningFeatureVector.deSerialize(serialLearningFeatureVector);

				this.featureVectors.add(featureVector);

			}

		}
		catch(Exception e) {
			throw e;
		}

	}

	/**
	 * Import the expected feature vector classes from a file.
	 * @param oraclePath The file path where the oracle is stored.
	 * @throws Exception Occurs when the oracle cannot be read.
	 */
	public void importOracle(String oraclePath) throws Exception {

		try(BufferedReader reader = new BufferedReader(new FileReader(oraclePath))) {

			for(String line = reader.readLine();
					line != null;
					line = reader.readLine()) {

				String[] values = line.split(",");

				if(values.length != 2) throw new Exception("Incorrect oracle format.");

				Integer id = Integer.parseInt(values[1]);
				String expected = values[0];

				this.oracle.put(id, expected);

			}

		}
		catch(Exception e) {
			throw e;
		}

	}

	/**
	 * Stores the feature vector in the file specified by {@code dataSetPath}.
	 * This method is synchronized because it may be used by several
	 * GitProjectAnalysis thread at the same time, which may cause race
	 * conditions when writing to the output file.
	 *
	 * @param featureVector The feature vector to be managed by this class.
	 */
	private synchronized void storeLearningFeatureVector(LearningFeatureVector featureVector) throws Exception {

		/* The path to the file may not exist. Create it if needed. */
		File path = new File(this.dataSetPath);
		path.getParentFile().mkdirs();
		path.createNewFile();

		/* May throw IOException if the path does not exist. */
		PrintStream stream = new PrintStream(new FileOutputStream(path, true));

		/* Write the data set. */
		stream.println(featureVector.serialize());

		/* Finished writing the feature vector. */
		stream.close();

	}

	/**
	 * Converts the feature vector header into a list of Weka attributes.
	 * @return The feature vector header as a list of Weka attributes.
	 */
	public ArrayList<Attribute> getWekaAttributes() {

		ArrayList<Attribute> attributes = new ArrayList<Attribute>();

		attributes.add(new Attribute("ID", 0));
		attributes.add(new Attribute("ProjectID", (ArrayList<String>)null, 1));
		attributes.add(new Attribute("BugFixingCommit", (ArrayList<String>)null, 2));
		attributes.add(new Attribute("CommitURL", (ArrayList<String>)null, 3));
		attributes.add(new Attribute("BuggyCommitID", (ArrayList<String>)null, 4));
		attributes.add(new Attribute("RepairedCommitID", (ArrayList<String>)null, 5));
		attributes.add(new Attribute("Class", (ArrayList<String>)null, 6));
		attributes.add(new Attribute("Method", (ArrayList<String>)null, 7));
		attributes.add(new Attribute("Cluster", (ArrayList<String>) null, 8));
		attributes.add(new Attribute("ModifiedStatementCount", 9));

		int i = 9;
		for(KeywordDefinition keyword : this.keywords) {
			attributes.add(new Attribute(keyword.toString(), i));
			i++;
		}

		return attributes;

	}

	/**
	 * Builds the feature vector header by filtering out features (columns)
	 * that are not used or hardly used.
	 * @return The feature vector header as a CSV list.
	 */
	public String getLearningFeatureVectorHeader() {

		String header = String.join(",", "ID", "ProjectID", "CommitURL",
				"BuggyCommitID", "RepairedCommitID",
				"Class", "Method", "ModifiedStatements");

		for(KeywordDefinition keyword : this.keywords) {
			header += "," + keyword.toString();
		}

		return header;

	}

	/**
	 * Builds the feature vector by filtering out feature vectors (rows)
	 * that do not contain the packages specified in {@code packagesToExtract}.
	 * @return The data set as a CSV file.
	 */
	public String getLearningFeatureVector() {

		String dataSet = "";

		for(LearningFeatureVector featureVector : this.featureVectors) {
			dataSet += featureVector.getFeatureVector(keywords) + "\n";
		}

		return dataSet;

	}

	/**
	 * @return The list of feature vectors in this data set.
	 */
	public List<LearningFeatureVector> getLearningFeatureVectors() {
		return this.featureVectors;
	}

	/**
	 * Computes metrics about the data set:
	 *
	 *	Common keywords: A ranked list of the most common keywords sorted by
	 *					 their change type and the number of occurrences.
	 *
	 * @return The metrics for the data set (in the {@code LearningMetrics}
	 * 		   object).
	 */
	public LearningMetrics getMetrics() {

		/* The metrics object. */
		LearningMetrics metrics = new LearningMetrics();

		/* Compute the frequency of keywords. */
		Map<KeywordUse, Integer> counts = new HashMap<KeywordUse, Integer>();
		for(LearningFeatureVector featureVector : this.featureVectors) {

			/* Increment all the keywords that appear in this feature vector. */
			for(KeywordUse keyword : featureVector.keywordMap.keySet()) {
				Integer count = counts.get(keyword);
				count = count == null ?  1 : count + 1;
				counts.put(keyword, count);
			}

		}

		/* Create the ordered set of keywords. */
		for(KeywordUse keyword : counts.keySet()) {
			metrics.addKeywordFrequency(keyword, counts.get(keyword));
		}


		return metrics;

	}

	/**
	 * Performs pre-processing operations for data-mining. Specifically,
	 * filters out rows which do not use the specified packages and filters
	 * out columns which do not contain any data.
	 */
	public void preProcess() {

		/* Remove rows that do not reference the packages we are interested in. */

		List<LearningFeatureVector> toRemove = new LinkedList<LearningFeatureVector>();
		for(LearningFeatureVector featureVector : this.featureVectors) {

			/* Ignore merge commits. */
			if(featureVector.commit.commitMessageType == Type.MERGE) {
				toRemove.add(featureVector);
			}
			/* Check if the feature vector references the any of the interesting packages. */
			else if(!includeRow(featureVector.keywordMap.keySet(), featureVector.modifiedStatementCount)) {

				/* Schedule this LearningFeatureVector for removal. */
				toRemove.add(featureVector);

			}

		}

		for(LearningFeatureVector featureVector : toRemove) {
			this.featureVectors.remove(featureVector);
		}

		/* Remove rows that do not fall within the desired change score. */

		toRemove = new LinkedList<LearningFeatureVector>();
		for(LearningFeatureVector featureVector : this.featureVectors) {

			/* Check if the feature vector references the any of the interesting packages. */
			if(getChangeScore(featureVector.keywordMap.keySet()) == 0) {

				/* Schedule this LearningFeatureVector for removal. */
				toRemove.add(featureVector);

			}

		}

		for(LearningFeatureVector featureVector : toRemove) {
			this.featureVectors.remove(featureVector);
		}

		/* Get the set of keywords from all the feature vectors. */

		for(LearningFeatureVector featureVector : this.featureVectors) {
			for(KeywordDefinition keyword : featureVector.keywordMap.keySet()) keywords.add(keyword);
		}

	}

	/**
	 * Converts this data set to a set of Weka Instances.
	 * @return The Weka data set.
	 */
	public Instances getWekaDataSet() {

		ArrayList<Attribute> attributes = this.getWekaAttributes();

		Instances dataSet = new Instances("DataSet", attributes, 0);
		dataSet.setClassIndex(-1);

		for(LearningFeatureVector featureVector : this.featureVectors) {
			dataSet.add(featureVector.getWekaInstance(dataSet, attributes, this.keywords));
		}

		return dataSet;
	}

	/**
	 * Generates the clusters for this data set using DBScan, epsilon = 0.01 and
	 * min = 25.
	 * @param clusterMetrics Structure to store the clusters and their metrics.
	 *
	 * @return The number of instances in each cluster. The array index is the
	 *         cluster number.
	 * @throws Exception
	 */
	public void getWekaClusters(ClusterMetrics clusterMetrics) throws Exception {

		/* Convert the data set to a Weka-usable format. */
		wekaData = this.getWekaDataSet();

		/* Filter out the columns we don't want. */
		String[] removeOptions = new String[2];
		removeOptions[0] = "-R";
		removeOptions[1] = "1-9";
		Remove remove = new Remove();
		remove.setOptions(removeOptions);
		remove.setInputFormat(wekaData);
		Instances filteredData = Filter.useFilter(wekaData, remove);

		/* Filter out the UNCHANGED columns. */
		String[] removeByNameOptions = new String[2];
		removeByNameOptions[0] = "-E";
		removeByNameOptions[1] = ".*UNCHANGED.*";
		RemoveByName removeByName = new RemoveByName();
		removeByName.setOptions(removeByNameOptions);
		removeByName.setInputFormat(filteredData);
		filteredData = Filter.useFilter(filteredData, removeByName);

		/* Create the keyword (column) filter. */
		String filter = "(.*_global_test)";
		for(KeywordUse keywordUse : this.columnFilters) {
			filter += "|(" + keywordUse.type.toString();
			filter += "_" + keywordUse.context.toString();
			filter += "_" + keywordUse.getPackageName();
			filter += "_" + keywordUse.keyword + ")";
		}

		/* Filter out the statement columns. */
		String[] removeKeywordOptions = new String[2];
		removeKeywordOptions[0] = "-E";
		removeKeywordOptions[1] = filter;
		RemoveByName removeKeyword = new RemoveByName();
		/* TODO: May get "NO ATTRIBUTE" error when small." */
		removeKeyword.setOptions(removeKeywordOptions);
		removeKeyword.setInputFormat(filteredData);
		filteredData = Filter.useFilter(filteredData, removeKeyword);

		/* Set up the distance function. We want Manhattan Distance. */
		ManhattanDistance distanceFunction = new ManhattanDistance();
		// -R specifies list of column to use, -D turns off normalization of attribute values
		String[] distanceFunctionOptions = "-R first-last -D".split("\\s");
		distanceFunction.setOptions(distanceFunctionOptions);

		/* DBScan Clusterer. */
		DBSCAN dbScan = new DBSCAN();
		String[] dbScanClustererOptions = "-E 0.2 -M 3".split("\\s");
		dbScan.setOptions(dbScanClustererOptions);
		dbScan.setDistanceFunction(distanceFunction);
		dbScan.buildClusterer(filteredData);

		/* Compute the metrics for the clustering. */
		for (Instance instance : wekaData) {
			try {
				Integer cluster = dbScan.clusterInstance(instance);
				instance.setValue(8, "cluster" + cluster.toString());

				/* Update the cluster with the set of keywords and complexity. */
				List<String> keywords = new LinkedList<String>();
				for(int i = 10; i < instance.numAttributes(); i++) {
					if(instance.value(i) > 0) {
						keywords.add(instance.attribute(i).name()
										+ ":" + (int)instance.value(i));
					}
				}

				/* Get the expected class for the instance. */
				String expected = this.oracle != null ?
						this.oracle.get((int)instance.value(0)) : "?";

				if(expected == null) throw new Error("A feature vector was not classified in the oracle: " + (int)instance.value(0));

				clusterMetrics.addInstance(cluster, (int)instance.value(0),
											expected, (int)instance.value(8),
											keywords);

			} catch (Exception ignore) { } // Instance is not part of any cluster

		}

	}

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
		stream.println(this.getLearningFeatureVectorHeader());

		/* Write the data set. */
		stream.println(this.getLearningFeatureVector());

	}

	/**
	 * @param keywords The keywords from a feature vector.
	 * @param maxModifiedStatements The maximum number of modified statements.
	 * @return True if the keyword set matches an include filter.
	 */
	private boolean includeRow(Set<KeywordUse> keywords, int modifiedStatementCount) {
		if(modifiedStatementCount > this.maxModifiedStatements) return false;
		return includeRow(keywords, this.rowFilters);
	}

	/**
	 * @param keywords The keywords from a feature vector.
	 * @param filers The filters to apply to the row.
	 * @return True if the keyword set matches an include filter.
	 */
	private static boolean includeRow(Set<KeywordUse> keywords, List<KeywordFilter> filters) {
		for(KeywordUse keyword : keywords) {
			for(KeywordFilter filter : filters) {

				/* This keyword must match the filter to be included. */
				if(filter.type != KeywordType.UNKNOWN && filter.type != keyword.type) continue;
				if(filter.context != KeywordContext.UNKNOWN && filter.context != keyword.context) continue;
				if(filter.changeType != ChangeType.UNKNOWN && filter.changeType != keyword.changeType) continue;
				if(!filter.pack.isEmpty() && !filter.pack.equals(keyword.getPackageName())) continue;
				if(!filter.keyword.isEmpty() && !filter.keyword.equals(keyword.keyword)) continue;

				/* The keyword matches the filter. */
				if(filter.filterType == FilterType.INCLUDE) return true;
				if(filter.filterType == FilterType.EXCLUDE) return false;

			}
		}
		return false;
	}

	/**
	 * Computes a score to determine how much a function has changed (using the
	 * number of inserted/removed/updated keywords).
	 * @param keywords The keywords from a feature vector.
	 * @return A score that represents how much the function has changed with
	 * 		   respect to its keywords. A score of zero means no keywords
	 * 		   changed.
	 */
	private int getChangeScore(Set<KeywordUse> keywords) {

		int score = 0;

		for(KeywordUse keyword : keywords) {
			if(keyword.changeType != ChangeType.UNCHANGED &&
					keyword.changeType != ChangeType.UNKNOWN) {
				score++;
			}
		}

		return score;

	}

	/**
	 * Writes the result of a clustering on an ARFF file
	 *
	 * @param outputFolder The folder were the ARFF files will be stored
	 * @param filename The filename (usually keyword toString representation)
	 */
	public void writeArffFile(String outputFolder, String filename) {
		/* The path may not exist. Create it if needed. */
		File path = new File(outputFolder, filename);
		path.mkdirs();

		ArffSaver saver = new ArffSaver();
		saver.setInstances(wekaData);

		try {
			saver.setFile(path);
			saver.writeBatch();
		} catch (IOException e) {
			System.err.println("Not possible to write Arff file.");

			e.printStackTrace();
		}

	}

	/**
	 * Compute the evaluation metrics if an oracle was provided.
	 */
	public void evaluate(ClusterMetrics clusterMetrics) {

		if(this.oracle != null) {
			clusterMetrics.evaluate(this.oracle);
		}

	}

	/**
	 * Checks if the feature vector manager contains the keyword inside a
	 * feature vector. Used for testing.
	 * @param function The name of the function.
	 * @param keywords The keywords to look for.
	 * @return True if the list of keywords matches the list of keywords form
	 * 		   one or more functions.
	 */
	public boolean contains(String function, List<Pair<KeywordUse, Integer>> keywords) {
		outer:
		for(LearningFeatureVector featureVector : this.featureVectors) {
			for(Pair<KeywordUse, Integer> keyword : keywords) {
				if(keyword.getRight() > 0 && !featureVector.keywordMap.containsKey(keyword.getLeft())) continue outer;
				if(keyword.getRight() > 0 && !featureVector.keywordMap.get(keyword.getLeft()).equals(keyword.getRight())) continue outer;
			}
			return true;
		}
		return false;
	}

}