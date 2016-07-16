package ca.ubc.ece.salt.pangor.js.diff.line;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.deri.iris.api.basics.IPredicate;
import org.deri.iris.api.basics.ITuple;
import org.deri.iris.factory.Factory;
import org.deri.iris.storage.IRelation;
import org.deri.iris.storage.IRelationFactory;
import org.deri.iris.storage.simple.SimpleRelationFactory;

import ca.ubc.ece.salt.gumtree.ast.ClassifiedASTNode.Version;
import ca.ubc.ece.salt.pangor.analysis.DomainAnalysis;
import ca.ubc.ece.salt.pangor.analysis.SourceCodeFileChange;
import difflib.Chunk;
import difflib.Delta;
import difflib.DiffUtils;
import difflib.Patch;

/**
 * An analysis of a JavaScript file for extracting line-level-diff change facts.
 *
 * Currently uses the Myers diff algorithm.
 */
public class LineDomainAnalysis extends DomainAnalysis {

	public LineDomainAnalysis() {
		super(null, null, null, false);
	}

	@Override
	protected void analyzeFile(SourceCodeFileChange sourceCodeFileChange,
							   Map<IPredicate, IRelation> facts) throws Exception {

		/* First transform the source code files into a list of lines. */
		String[] buggyArray = sourceCodeFileChange.buggyCode.split("\n");
		ArrayList<String> buggyLines = new ArrayList<String>(buggyArray.length);
		for(int i = 0; i < buggyArray.length; i++) buggyLines.add(i, buggyArray[i]);

		String[] repairedArray = sourceCodeFileChange.repairedCode.split("\n");
		ArrayList<String> repairedLines = new ArrayList<String>(repairedArray.length);
		for(int i = 0; i < repairedArray.length; i++) repairedLines.add(i, repairedArray[i]);

		/* Compute the line-level diff between the source and destination files. */
		Patch patch = DiffUtils.diff(buggyLines, repairedLines);

		/* Figure out which line numbers have changed. */
		List<Delta> deltas = patch.getDeltas();
		for(Delta delta : deltas) {

			/* Register a fact for each modified line in the source file. */
			Chunk buggyChunk = delta.getOriginal();
			for(int i = buggyChunk.getPosition() + 1;
					i <= buggyChunk.getPosition() + buggyChunk.size();
					i++) {
				registerChangeFact(Version.SOURCE, i, delta.getType(), facts,
								   sourceCodeFileChange);
			}

			/* Register a fact for each modified line in the destination file. */
			Chunk repairedChunk = delta.getRevised();
			for(int i = repairedChunk.getPosition() + 1;
					i <= repairedChunk.getPosition() + repairedChunk.size();
					i++) {
				registerChangeFact(Version.DESTINATION, i, delta.getType(), facts,
								   sourceCodeFileChange);
			}

		}

	}

	/**
	 * Registers a line change fact.
	 * @param version The version of the file: SOURCE or DESTINATION
	 * @param line The line number of the changed line.
	 * @param type The type of modification: CHANGE, DELETE or INSERT
	 * @param changeType How the statement was modified.
	 */
	private static void registerChangeFact(Version version, int line,
									  Delta.TYPE type,
									  Map<IPredicate, IRelation> facts,
									  SourceCodeFileChange sourceCodeFileChange) {

		/* Get the relation for this predicate from the fact base. */
		IPredicate predicate = Factory.BASIC.createPredicate("Line", 4);
		IRelation relation = facts.get(predicate);
		if(relation == null) {

			/* The predicate does not yet exist in the fact base. Create a
			 * relation for the predicate and add it to the fact base. */
			IRelationFactory relationFactory = new SimpleRelationFactory();
			relation = relationFactory.createRelation();
			facts.put(predicate, relation);

		}

		/* Add the new tuple to the relation. */
		ITuple tuple = Factory.BASIC.createTuple(
				Factory.TERM.createString(version.toString()),							// Version
				Factory.TERM.createString(sourceCodeFileChange.repairedFile), 		// File
				Factory.TERM.createString(String.valueOf(line)),					// Line #
				Factory.TERM.createString(type.toString()));						// Change Type
		relation.add(tuple);

		facts.put(predicate, relation);

	}

}
