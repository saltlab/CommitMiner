package ca.ubc.ece.salt.pangor.js.diff.line;

import java.util.LinkedList;
import java.util.Map;

import org.deri.iris.api.basics.IPredicate;
import org.deri.iris.api.basics.ITuple;
import org.deri.iris.factory.Factory;
import org.deri.iris.storage.IRelation;
import org.deri.iris.storage.IRelationFactory;
import org.deri.iris.storage.simple.SimpleRelationFactory;

import ca.ubc.ece.salt.gumtree.ast.ClassifiedASTNode.ChangeType;
import ca.ubc.ece.salt.gumtree.ast.ClassifiedASTNode.Version;
import ca.ubc.ece.salt.pangor.analysis.DomainAnalysis;
import ca.ubc.ece.salt.pangor.analysis.SourceCodeFileChange;


/**
 * An analysis of a JavaScript file for extracting line-level-diff change facts.
 *
 * Uses the Myers diff algorithm.
 */
public class LineDomainAnalysis extends DomainAnalysis {

	public LineDomainAnalysis() {
		super(null, null, null, false);
	}

	@Override
	protected void analyzeFile(SourceCodeFileChange sourceCodeFileChange,
							   Map<IPredicate, IRelation> facts) throws Exception {

		DiffMatchPatch dmp = new DiffMatchPatch();
		LinkedList<DiffMatchPatch.Diff> diffs =
				dmp.diff_main_line_mode(sourceCodeFileChange.buggyCode,
							  sourceCodeFileChange.repairedCode);

		int i = 0; // Track the line number in the source file.
		int j = 0; // Track the line number in the destination file.

		for (DiffMatchPatch.Diff diff : diffs) {
		  for (int y = 0; y < diff.text.length(); y++) {
			  switch(diff.operation) {
			  case EQUAL:
				  i++;
				  j++;
				  registerChangeFact(Version.SOURCE, i, ChangeType.UNCHANGED,
						  			 facts, sourceCodeFileChange);
				  registerChangeFact(Version.DESTINATION, j, ChangeType.UNCHANGED,
						  			 facts, sourceCodeFileChange);
				  break;
			  case DELETE:
				  i++;
				  registerChangeFact(Version.SOURCE, i, ChangeType.REMOVED,
						  			 facts, sourceCodeFileChange);
				  break;
			  case INSERT:
				  j++;
				  registerChangeFact(Version.DESTINATION, j, ChangeType.INSERTED,
						  			 facts, sourceCodeFileChange);
				  break;
			  }
		  }
		}

		registerTotalLinesFact(Version.SOURCE, i, facts, sourceCodeFileChange);
		registerTotalLinesFact(Version.DESTINATION, j, facts, sourceCodeFileChange);

	}

	/**
	 * Registers a fact that stores the total number of lines in the file.
	 * @param version The version of the file: SOURCE or DESTINATION
	 * @param total_lines The total number of lines in the file.
	 * @param tag The type of modification: CHANGE, DELETE or INSERT
	 * @param changeType How the statement was modified.
	 */
	private static void registerTotalLinesFact(Version version, int total_lines,
									  Map<IPredicate, IRelation> facts,
									  SourceCodeFileChange sourceCodeFileChange) {

		/* Get the relation for this predicate from the fact base. */
		IPredicate predicate = Factory.BASIC.createPredicate("TotalLines", 3);
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
				Factory.TERM.createString(version.toString()),						// Version
				Factory.TERM.createString(sourceCodeFileChange.repairedFile), 		// File
				Factory.TERM.createString(String.valueOf(total_lines)));			// Line #
		relation.add(tuple);

		facts.put(predicate, relation);

	}

	/**
	 * Registers a line change fact.
	 * @param version The version of the file: SOURCE or DESTINATION
	 * @param line The line number of the changed line.
	 * @param tag The type of modification: CHANGE, DELETE or INSERT
	 * @param changeType How the statement was modified.
	 */
	private static void registerChangeFact(Version version, int line,
									  ChangeType type,
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
				Factory.TERM.createString(version.toString()),						// Version
				Factory.TERM.createString(sourceCodeFileChange.repairedFile), 		// File
				Factory.TERM.createString("{" + String.valueOf(line) + "}"),		// Line #
				Factory.TERM.createString(type.toString()));						// Change Type
		relation.add(tuple);

		facts.put(predicate, relation);

	}

}
