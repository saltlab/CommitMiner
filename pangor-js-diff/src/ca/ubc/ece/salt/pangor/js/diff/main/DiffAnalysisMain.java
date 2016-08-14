package ca.ubc.ece.salt.pangor.js.diff.main;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.commons.lang3.tuple.Pair;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.deri.iris.api.basics.IQuery;
import org.deri.iris.api.basics.IRule;
import org.deri.iris.compiler.Parser;
import org.deri.iris.compiler.ParserException;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;

import ca.ubc.ece.salt.pangor.analysis.factories.ICommitAnalysisFactory;
import ca.ubc.ece.salt.pangor.batch.GitProjectAnalysis;
import ca.ubc.ece.salt.pangor.batch.GitProjectAnalysisTask;
import ca.ubc.ece.salt.pangor.classify.analysis.ClassifierDataSet;
import ca.ubc.ece.salt.pangor.classify.analysis.ClassifierFeatureVector;
import ca.ubc.ece.salt.pangor.classify.analysis.Transformer;
import ca.ubc.ece.salt.pangor.js.diff.DiffCommitAnalysisFactory;

public class DiffAnalysisMain {

	protected static final Logger logger = LogManager.getLogger(DiffAnalysisMain.class);

	/** The directory where repositories are checked out. **/
	public static final String CHECKOUT_DIR =  new String("repositories");

	/**
	 * Creates the learning data set for extracting repair patterns.
	 * @param args
	 * @throws ParserException when a Datalog query is incorrect.
	 * @throws Exception
	 */
	public static void main(String[] args) throws ParserException {
		PropertyConfigurator.configure("log4j.properties");

		DiffAnalysisOptions options = new DiffAnalysisOptions();
		CmdLineParser parser = new CmdLineParser(options);

		try {
			parser.parseArgument(args);
		} catch (CmdLineException e) {
			DiffAnalysisMain.printUsage(e.getMessage(), parser);
			return;
		}

		/* Print the help page. */
		if(options.getHelp()) {
			DiffAnalysisMain.printHelp(parser);
			return;
		}


		/* Builds the data set with our custom queries. */
		ClassifierDataSet dataSet = new ClassifierDataSet(options.getDataSetPath(),
				new LinkedList<IRule>(), getUseQueries());

		/* Set up the analysis factory. */
		ICommitAnalysisFactory commitFactory = new DiffCommitAnalysisFactory(dataSet);

		/* Set up the project analysis. */
        GitProjectAnalysis gitProjectAnalysis;

		/* A URI was given. */
		if(options.getURI() != null) {

			try {
                gitProjectAnalysis = GitProjectAnalysis.fromURI(options.getURI(),
                		CHECKOUT_DIR, options.getRegex(), commitFactory);
				gitProjectAnalysis.analyze();

			} catch (Exception e) {
				e.printStackTrace(System.err);
				return;
			}

		}
		/* A list of URIs was given. */
		else if(options.getRepoFile() != null) {

			/* Parse the file into a list of URIs. */
			List<String> uris = new LinkedList<String>();

			try(BufferedReader br = new BufferedReader(new FileReader(options.getRepoFile()))) {
			    for(String line; (line = br.readLine()) != null; ) {
			    	uris.add(line);
			    }
			}
			catch(Exception e) {
				System.err.println("Error while reading URI file: " + e.getMessage());
				return;
			}

			/*
			 * Create a pool of threads and use a CountDownLatch to check when
			 * all threads are done.
			 * http://stackoverflow.com/questions/1250643/how-to-wait-for-all-
			 * threads-to-finish-using-executorservice
			 *
			 * I was going to create a list of Callable objects and use
			 * executor.invokeAll, but this would remove the start of the
			 * execution of the tasks from the loop to outside the loop, which
			 * would mean all git project initializations would have to happen
			 * before starting the analysis.
			 */
			ExecutorService executor = Executors.newFixedThreadPool(options.getNThreads());
			CountDownLatch latch = new CountDownLatch(uris.size());

			/* Analyze all projects. */
			for(String uri : uris) {

				try {
					/* Build git repository object */
					gitProjectAnalysis = GitProjectAnalysis.fromURI(uri,
							DiffAnalysisMain.CHECKOUT_DIR, options.getRegex(), commitFactory);

					/* Perform the analysis (this may take some time) */
					executor.submit(new GitProjectAnalysisTask(gitProjectAnalysis, latch));
				} catch (Exception e) {
					e.printStackTrace(System.err);
					logger.error("[IMPORTANT] Project " + uri + " threw an exception");
					logger.error(e);
					continue;
				}
			}

			/* Wait for all threads to finish their work */
			try {
				latch.await();
			} catch (InterruptedException e) {
				e.printStackTrace();
				return;
			}

		}
		else {
			System.out.println("No repository given.");
			DiffAnalysisMain.printUsage("No repository given.", parser);
			return;
		}

	}

	/**
	 * Prints the help file for main.
	 * @param parser The args4j parser.
	 */
	private static void printHelp(CmdLineParser parser) {
        System.out.print("Usage: DataSetMain ");
        parser.printSingleLineUsage(System.out);
        System.out.println("\n");
        parser.printUsage(System.out);
        System.out.println("");
        return;
	}

	/**
	 * Prints the usage of main.
	 * @param error The error message that triggered the usage message.
	 * @param parser The args4j parser.
	 */
	private static void printUsage(String error, CmdLineParser parser) {
        System.out.println(error);
        System.out.print("Usage: DataSetMain ");
        parser.printSingleLineUsage(System.out);
        System.out.println("");
        return;
	}

	/**
	 * @return The Datalog query that selects identifier uses.
	 * @throws ParserException when iris fails to parse the query string.
	 */
	public static Map<IQuery,Transformer> getUseQueries() throws ParserException {

		Map<IQuery, Transformer> queries = new HashMap<IQuery, Transformer>();

		Pair<IQuery, Transformer> valueQuery = getValueQuery();
		queries.put(valueQuery.getLeft(), valueQuery.getRight());

		Pair<IQuery, Transformer> environmentQuery = getEnvironmentQuery();
		queries.put(environmentQuery.getLeft(), environmentQuery.getRight());

		Pair<IQuery, Transformer> controlQuery = getControlQuery();
		queries.put(controlQuery.getLeft(), controlQuery.getRight());

		Pair<IQuery, Transformer> astQuery = getAstQuery();
		queries.put(astQuery.getLeft(), astQuery.getRight());

		Pair<IQuery, Transformer> lineQuery = getLineQuery();
		queries.put(lineQuery.getLeft(), lineQuery.getRight());

		Pair<IQuery, Transformer> totalLinesQuery = getTotalLinesQuery();
		queries.put(totalLinesQuery.getLeft(), totalLinesQuery.getRight());

		return queries;

	}

	/**
	 * @return The query for extracting value-diff alerts.
	 * @throws ParserException for incorrect query strings.
	 */
	private static Pair<IQuery, Transformer> getValueQuery() throws ParserException {

		String qs = "";
		qs += "?- Value(?Version,?File,?Line,?StatementID,?Identifier,?ValChange)";
		qs += ", EQUAL(?ValChange, 'Change:CHANGED').";

		/* The query that produces the results. */
		Parser parser = new Parser();
		parser.parse(qs);
		IQuery query = parser.getQueries().get(0);

		/* Transforms the query results to a ClassifierFeatureVector. */
		Transformer transformer = (commit, tuple) -> {
				return new ClassifierFeatureVector(commit,
						tuple.get(0).toString().replace("\'", ""),			// Version
						tuple.get(1).toString().replace("\'", ""), 			// Class
						tuple.get(3).toString().replace("\'",  ""),			// AST Node ID
						tuple.get(2).toString().replace("\'", ""),			// Line
						"DIFF",												// Type
						"VAL",											// Subtype
						tuple.get(4).toString().replace("\'", "")
							+ "_" + tuple.get(5).toString().replace("\'", ""));	// Description
		};

		return Pair.of(query, transformer);

	}

	/**
	 * @return The query for extracting environment-diff alerts.
	 * @throws ParserException for incorrect query strings.
	 */
	private static Pair<IQuery, Transformer> getEnvironmentQuery() throws ParserException {

		String qs = "";
		qs += "?- Environment(?Version,?File,?Line,?StatementID,?Identifier,?Type,?EnvChange)";
		qs += ", EQUAL(?EnvChange, 'Change:CHANGED').";

		/* The query that produces the results. */
		Parser parser = new Parser();
		parser.parse(qs);
		IQuery query = parser.getQueries().get(0);

		/* Transforms the query results to a ClassifierFeatureVector. */
		Transformer transformer = (commit, tuple) -> {
				return new ClassifierFeatureVector(commit,
						tuple.get(0).toString().replace("\'", ""),			// Version
						tuple.get(1).toString().replace("\'", ""), 			// Class
						tuple.get(3).toString().replace("\'",  ""),			// AST Node ID
						tuple.get(2).toString().replace("\'", ""),			// Line
						"DIFF",												// Type
						"ENV",												// Subtype
						tuple.get(4).toString().replace("\'", "")
							+ "_" + tuple.get(5).toString().replace("\'", "")
							+ "_" + tuple.get(6).toString().replace("\'", ""));	// Description
		};

		return Pair.of(query, transformer);

	}

	/**
	 * @return The query for extracting control-flow-diff alerts.
	 * @throws ParserException for incorrect query strings.
	 */
	private static Pair<IQuery, Transformer> getControlQuery() throws ParserException {

		String qs = "";
		qs += "?- Control(?Version,?File,?Line,?StatementID,?Type,?Change)";
		qs += ", EQUAL(?Change, 'Change:CHANGED').";

		/* The query that produces the results. */
		Parser parser = new Parser();
		parser.parse(qs);
		IQuery query = parser.getQueries().get(0);

		/* Transforms the query results to a ClassifierFeatureVector. */
		Transformer transformer = (commit, tuple) -> {
				return new ClassifierFeatureVector(commit,
						tuple.get(0).toString().replace("\'", ""),			// Version
						tuple.get(1).toString().replace("\'", ""), 			// Class
						tuple.get(3).toString().replace("\'",  ""),			// AST Node ID
						tuple.get(2).toString().replace("\'", ""),			// Line
						"DIFF",												// Type
						"CONTROL",											// Subtype
						tuple.get(5).toString().replace("\'", ""));			// Description
		};

		return Pair.of(query, transformer);

	}

	/**
	 * @return The query for extracting AST-diff alerts.
	 * @throws ParserException for incorrect query strings.
	 */
	private static Pair<IQuery, Transformer> getAstQuery() throws ParserException {

		String qs = "";
		qs += "?- AST(?Version,?File,?Line,?StatementID,?Change)";
		qs += ", NOT_EQUAL(?Change, 'UNCHANGED').";

		/* The query that produces the results. */
		Parser parser = new Parser();
		parser.parse(qs);
		IQuery query = parser.getQueries().get(0);

		/* Transforms the query results to a ClassifierFeatureVector. */
		Transformer transformer = (commit, tuple) -> {
				return new ClassifierFeatureVector(commit,
						tuple.get(0).toString().replace("\'", ""),			// Version
						tuple.get(1).toString().replace("\'", ""), 			// Class
						tuple.get(3).toString().replace("\'",  ""),			// AST Node ID
						tuple.get(2).toString().replace("\'", ""),			// Line
						"DIFF",												// Type
						"AST",												// Subtype
						tuple.get(4).toString().replace("\'", ""));			// Description
		};

		return Pair.of(query, transformer);
	}

	/**
	 * @return The query for extracting total number of lines in a file.
	 * @throws ParserException for incorrect query strings.
	 */
	private static Pair<IQuery, Transformer> getTotalLinesQuery() throws ParserException {

		String qs = "";
		qs += "?- TotalLines(?Version,?File,?TotalLines).";

		/* The query that produces the results. */
		Parser parser = new Parser();
		parser.parse(qs);
		IQuery query = parser.getQueries().get(0);

		/* Transforms the query results to a ClassifierFeatureVector. */
		Transformer transformer = (commit, tuple) -> {
				return new ClassifierFeatureVector(commit,
						tuple.get(0).toString().replace("\'", ""),			// Version
						tuple.get(1).toString().replace("\'", ""), 			// Class
						"NA",												// Method
						"NA",												// Line
						"DIFF",												// Type
						"TOTAL_LINES",										// Subtype
						tuple.get(2).toString().replace("\'", ""));			// Description
		};

		return Pair.of(query, transformer);

	}

	/**
	 * @return The query for extracting line-diff alerts.
	 * @throws ParserException for incorrect query strings.
	 */
	private static Pair<IQuery, Transformer> getLineQuery() throws ParserException {

		String qs = "";
		qs += "?- Line(?Version,?File,?Line,?Change)";
		qs += ", NOT_EQUAL(?Change, 'UNCHANGED').";

		/* The query that produces the results. */
		Parser parser = new Parser();
		parser.parse(qs);
		IQuery query = parser.getQueries().get(0);

		/* Transforms the query results to a ClassifierFeatureVector. */
		Transformer transformer = (commit, tuple) -> {
				return new ClassifierFeatureVector(commit,
						tuple.get(0).toString().replace("\'", ""),			// Version
						tuple.get(1).toString().replace("\'", ""), 			// Class
						"NA",												// AST Node ID
						tuple.get(2).toString().replace("\'", ""),			// Line
						"DIFF",												// Type
						"LINE",												// Subtype
						tuple.get(3).toString().replace("\'", ""));			// Description
		};

		return Pair.of(query, transformer);

	}

}