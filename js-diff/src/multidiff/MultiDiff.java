package multidiff;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.deri.iris.api.basics.IQuery;
import org.deri.iris.api.basics.IRule;
import org.deri.iris.compiler.Parser;
import org.deri.iris.compiler.ParserException;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;

import commitminer.analysis.Commit;
import commitminer.analysis.CommitAnalysis;
import commitminer.analysis.SourceCodeFileChange;
import commitminer.analysis.Commit.Type;
import commitminer.analysis.factories.ICommitAnalysisFactory;
import commitminer.classify.ClassifierDataSet;
import commitminer.classify.ClassifierFeatureVector;
import commitminer.classify.Transformer;
import commitminer.js.diff.DiffCommitAnalysisFactory;
import commitminer.js.diff.view.HTMLMultiDiffViewer;
import commitminer.js.diff.view.HTMLUnixDiffViewer;

public class MultiDiff {

	public static void main(String[] args) {

		/* The test files. */
		MultiDiffOptions options = new MultiDiffOptions();
		CmdLineParser parser = new CmdLineParser(options);

		try {
			parser.parseArgument(args);
		} catch (CmdLineException e) {
			MultiDiff.printUsage(e.getMessage(), parser);
			return;
		}

		/* Print the help page. */
		if(options.getHelp()) {
			MultiDiff.printHelp(parser);
			return;
		}

		/* Read the source files. */
		List<SourceCodeFileChange> sourceCodeFileChanges = new LinkedList<SourceCodeFileChange>();
		
		try { 
			sourceCodeFileChanges.add(getSourceCodeFileChange(options.getOriginal(), options.getModified()));
		} catch(IOException e) {
			System.err.println("An IOException occurred while reading the source code files. Check that the paths are correct.");
			return;
		}

		/* Build the expected feature vectors. */
		try {
			MultiDiff multiDiff = new MultiDiff(options);
			multiDiff.diff(sourceCodeFileChanges);
		} catch (Exception e) {
			System.err.println("An Exception occurred while generating the diff... aborting.");
		}

	}
	
	/** The analysis options. **/
	private MultiDiffOptions options;
	
	public MultiDiff(MultiDiffOptions options) {
		this.options = options;
	}

	/**
	 * Generate the diff in a partial html file.
	 */
	protected void diff(List<SourceCodeFileChange> sourceFileChanges) throws Exception {

		/* Read the source files. */
		String srcCode = new String(Files.readAllBytes(Paths.get(options.getOriginal())));
		String dstCode = new String(Files.readAllBytes(Paths.get(options.getModified())));

		/* Set up a 'fake' commit since this is not a mining task. */
		Commit commit = getCommit();
		for(SourceCodeFileChange sourceFileChange : sourceFileChanges) {
			commit.addSourceCodeFileChange(sourceFileChange);
		}

		/* Builds the data set with our custom queries. */
		ClassifierDataSet dataSet = new ClassifierDataSet(null,
				new LinkedList<IRule>(), getUseQueries());

		/* Set up the analysis. */
		ICommitAnalysisFactory commitFactory = new DiffCommitAnalysisFactory(dataSet);
		CommitAnalysis commitAnalysis = commitFactory.newInstance();

		/* Run the analysis. */
		commitAnalysis.analyze(commit);

        /* Print the data set. */
		dataSet.printDataSet();

        /* Return the alerts. */
		List<ClassifierFeatureVector> alerts = dataSet.getFeatureVectors();

		/* Only annotate the destination file. The source file isn't especially useful. */
		String annotatedDst = HTMLMultiDiffViewer.annotate(dstCode, alerts, "DESTINATION");

		/* Combine the annotated file with the UnixDiff. */
		String annotatedCombined = HTMLUnixDiffViewer.annotate(srcCode, dstCode, annotatedDst);
		Files.write(Paths.get(options.getOutputFile()), annotatedCombined.getBytes(), StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);

	}

	/**
	 * Prints the help file for main.
	 * @param parser The args4j parser.
	 */
	private static void printHelp(CmdLineParser parser) {
        System.out.print("Usage: MultiDiff ");
        parser.printSingleLineUsage(System.out);
        System.out.println("\n");
        parser.printUsage(System.out);
        System.out.println("");
	}

	/**
	 * Prints the usage of main.
	 * @param error The error message that triggered the usage message.
	 * @param parser The args4j parser.
	 */
	private static void printUsage(String error, CmdLineParser parser) {
        System.out.println(error);
        System.out.print("Usage: MultiDiff ");
        parser.printSingleLineUsage(System.out);
        System.out.println("");
	}


	/**
	 * @return a dummy commit. 
	 */
	public static Commit getCommit() {
		return new Commit("test", "http://github.com/saltlab/Pangor", "c0", "c1", Type.BUG_FIX);
	}

	/**
	 * @return A dummy source code file change for testing.
	 * @throws IOException
	 */
	public static SourceCodeFileChange getSourceCodeFileChange(String srcFile, String dstFile) throws IOException {
		String buggyCode = readFile(srcFile);
		String repairedCode = readFile(dstFile);
		return new SourceCodeFileChange(srcFile, dstFile, buggyCode, repairedCode);
	}

	/**
	 * Reads the contents of a source code file into a string.
	 * @param path The path to the source code file.
	 * @return A string containing the source code.
	 * @throws IOException
	 */
	private static String readFile(String path) throws IOException {
		return FileUtils.readFileToString(new File(path));
	}

	/**
	 * @return The Datalog query that selects identifier uses.
	 * @throws ParserException when iris fails to parse the query string.
	 */
	public Map<IQuery,Transformer> getUseQueries() throws ParserException {

		Map<IQuery, Transformer> queries = new HashMap<IQuery, Transformer>();

		if(options.defUse()) {
			Pair<IQuery, Transformer> defQuery = getDefQuery();
			queries.put(defQuery.getLeft(), defQuery.getRight());

			Pair<IQuery, Transformer> useQuery = getUseQuery();
			queries.put(useQuery.getLeft(), useQuery.getRight());
		}

		if(options.valDiff()) {
			Pair<IQuery, Transformer> valueQuery = getValueQuery();
			queries.put(valueQuery.getLeft(), valueQuery.getRight());
		}

		if(options.varDiff()) {
			Pair<IQuery, Transformer> environmentQuery = getEnvironmentQuery();
			queries.put(environmentQuery.getLeft(), environmentQuery.getRight());
		}

		if(options.conDiff()) {
			Pair<IQuery, Transformer> controlQuery = getControlQuery();
			queries.put(controlQuery.getLeft(), controlQuery.getRight());
		}

		return queries;

	}

	/**
	 * @return The query for extracting DEF alerts.
	 * @throws ParserException for incorrect query strings.
	 */
	private static Pair<IQuery, Transformer> getDefQuery() throws ParserException {

		String qs = "";
		qs += "?- Def(?Version,?Address,?Position,?Length).";

		/* The query that produces the results. */
		Parser parser = new Parser();
		parser.parse(qs);
		IQuery query = parser.getQueries().get(0);

		/* Transforms the query results to a ClassifierFeatureVector. */
		Transformer transformer = (commit, tuple) -> {
				return new ClassifierFeatureVector(commit,
						tuple.get(0).toString().replace("\'", ""),			// Version
						"NA", 												// Class
						"NA",												// AST Node ID
						tuple.get(1).toString().replace("\'", ""),			// Address
						tuple.get(2).toString().replace("\'", ""),			// Position
						tuple.get(3).toString().replace("\'", ""),			// Length
						"POINTS-TO",										// Type
						"DEF",												// Subtype
						"NA");												// Description
		};

		return Pair.of(query, transformer);

	}

	/**
	 * @return The query for extracting USE alerts.
	 * @throws ParserException for incorrect query strings.
	 */
	private static Pair<IQuery, Transformer> getUseQuery() throws ParserException {

		String qs = "";
		qs += "?- Use(?Version,?File,?Address,?Position,?Length).";

		/* The query that produces the results. */
		Parser parser = new Parser();
		parser.parse(qs);
		IQuery query = parser.getQueries().get(0);

		/* Transforms the query results to a ClassifierFeatureVector. */
		Transformer transformer = (commit, tuple) -> {
				return new ClassifierFeatureVector(commit,
						tuple.get(0).toString().replace("\'", ""),			// Version
						tuple.get(1).toString().replace("\'", ""), 												// Class
						"NA",												// AST Node ID
						tuple.get(2).toString().replace("\'", ""),			// Address
						tuple.get(3).toString().replace("\'", ""),			// Position
						tuple.get(4).toString().replace("\'", ""),			// Length
						"POINTS-TO",										// Type
						"USE",												// Subtype
						"NA");												// Description
		};

		return Pair.of(query, transformer);

	}

	/**
	 * @return The query for extracting value-diff alerts.
	 * @throws ParserException for incorrect query strings.
	 */
	private static Pair<IQuery, Transformer> getValueQuery() throws ParserException {

		String qs = "";
		qs += "?- Value(?Version,?File,?Line,?Position,?Length,?StatementID,?Identifier,?ValChange)";
		qs += ", NOT_EQUAL(?ValChange, 'Change:UNCHANGED')";
		qs += ", NOT_EQUAL(?ValChange, 'Change:BOTTOM').";

		/* The query that produces the results. */
		Parser parser = new Parser();
		parser.parse(qs);
		IQuery query = parser.getQueries().get(0);

		/* Transforms the query results to a ClassifierFeatureVector. */
		Transformer transformer = (commit, tuple) -> {
				return new ClassifierFeatureVector(commit,
						tuple.get(0).toString().replace("\'", ""),			// Version
						tuple.get(1).toString().replace("\'", ""), 			// Class
						tuple.get(5).toString().replace("\'",  ""),			// AST Node ID
						tuple.get(2).toString().replace("\'", ""),			// Line
						tuple.get(3).toString().replace("\'", ""),			// Position
						tuple.get(4).toString().replace("\'", ""),			// Length
						"DIFF",												// Type
						"VAL",												// Subtype
						tuple.get(6).toString().replace("\'", "")
							+ "_" + tuple.get(7).toString().replace("\'", ""));	// Description
		};

		return Pair.of(query, transformer);

	}

	/**
	 * @return The query for extracting environment-diff alerts.
	 * @throws ParserException for incorrect query strings.
	 */
	private static Pair<IQuery, Transformer> getEnvironmentQuery() throws ParserException {

		String qs = "";
		qs += "?- Environment(?Version,?File,?Line,?Position,?Length,?StatementID,?Identifier,?Type,?EnvChange)";
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
						tuple.get(5).toString().replace("\'",  ""),			// AST Node ID
						tuple.get(2).toString().replace("\'", ""),			// Line
						tuple.get(3).toString().replace("\'", ""),			// Position
						tuple.get(4).toString().replace("\'", ""),			// Length
						"DIFF",												// Type
						"ENV",												// Subtype
						tuple.get(6).toString().replace("\'", "")
							+ "_" + tuple.get(7).toString().replace("\'", "")
							+ "_" + tuple.get(8).toString().replace("\'", ""));	// Description
		};

		return Pair.of(query, transformer);

	}

	/**
	 * @return The query for extracting control-flow-diff alerts.
	 * @throws ParserException for incorrect query strings.
	 */
	private static Pair<IQuery, Transformer> getControlQuery() throws ParserException {

		String qs = "";
		qs += "?- Control(?Version,?File,?Line,?Position,?Length,?StatementID,?Type,?Change)";
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
						tuple.get(5).toString().replace("\'",  ""),			// AST Node ID
						tuple.get(2).toString().replace("\'", ""),			// Line
						tuple.get(3).toString().replace("\'", ""),			// Position
						tuple.get(4).toString().replace("\'", ""),			// Length
						"DIFF",												// Type
						tuple.get(6).toString().replace("\'", ""),			// Subtype
						tuple.get(7).toString().replace("\'", ""));			// Description
		};

		return Pair.of(query, transformer);

	}

}