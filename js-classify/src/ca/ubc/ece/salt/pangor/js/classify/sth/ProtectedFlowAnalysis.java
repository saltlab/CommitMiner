package ca.ubc.ece.salt.pangor.js.classify.sth;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.tuple.Pair;
import org.deri.iris.api.basics.IPredicate;
import org.deri.iris.api.basics.ITuple;
import org.deri.iris.factory.Factory;
import org.deri.iris.storage.IRelation;
import org.deri.iris.storage.IRelationFactory;
import org.deri.iris.storage.simple.SimpleRelationFactory;
import org.mozilla.javascript.ast.AstNode;
import org.mozilla.javascript.ast.ScriptNode;

import ca.ubc.ece.salt.gumtree.ast.ClassifiedASTNode.ChangeType;
import ca.ubc.ece.salt.pangor.analysis.SourceCodeFileChange;
import ca.ubc.ece.salt.pangor.analysis.flow.PathSensitiveFlowAnalysis;
import ca.ubc.ece.salt.pangor.cfg.CFGEdge;
import ca.ubc.ece.salt.pangor.cfg.CFGNode;
import ca.ubc.ece.salt.pangor.js.analysis.scope.Scope;
import ca.ubc.ece.salt.pangor.js.analysis.utilities.AnalysisUtilities;
import ca.ubc.ece.salt.pangor.js.analysis.utilities.SpecialTypeAnalysisUtilities;
import ca.ubc.ece.salt.pangor.js.analysis.utilities.SpecialTypeAnalysisUtilities.SpecialType;
import ca.ubc.ece.salt.pangor.js.analysis.utilities.SpecialTypeCheck;

/**
 * A change-sensitive analysis that finds where special types are protected in JavaScript.
 *
 * Protected (not special type) path:
 * 	1. An edge conditions that check that an identifier is not a special type.
 *  2. Condition is propagated to all nodes along the path until merged with the
 *     opposite condition.
 *
 * Special type path:
 * 	1. New edge conditions that check that an identifier is a special type.
 *  2. Condition is propagated to all nodes along the path until merged with the
 *     opposite condition.
 *
 */
public class ProtectedFlowAnalysis extends PathSensitiveFlowAnalysis<ProtectedLatticeElement> {

//	/** Stores the possible special type check repairs. */
//	private Map<String, List<SpecialTypeCheckResult>> specialTypeCheckResults;

	public ProtectedFlowAnalysis() {
//		this.specialTypeCheckResults = new HashMap<String, List<SpecialTypeCheckResult>>();
	}

//	/**
//	 * @return The set of possible special type check repairs (or
//	 * anti-patterns if this is the source file analysis.
//	 */
////	public Map<String, List<SpecialTypeCheckResult>> getSpecialTypeCheckResults() {
////		return this.specialtypecheckresults;
////	}

	@Override
	public ProtectedLatticeElement entryValue(ScriptNode function) {
		return new ProtectedLatticeElement();
	}

	@Override
	public void transfer(CFGEdge edge, ProtectedLatticeElement sourceLE, Scope<AstNode> scope,
						 Map<IPredicate, IRelation> facts,
						 SourceCodeFileChange sourceCodeFileChange) {

		AstNode condition = (AstNode)edge.getCondition();
		if(condition == null) return;

		/* Check if condition has an inserted special type check and whether
		 * the check evaluates to true or false. */
		ProtectedVisitor visitor = new ProtectedVisitor(condition);
		condition.visit(visitor);

		/* Add any special type checks to the lattice element. */
		for(SpecialTypeCheck specialTypeCheck : visitor.getSpecialTypeChecks()) {

			/* Is the identifier definitely a special type on this path or
			 * definitely not a special type on this path?
			 */
			if(specialTypeCheck.isSpecialType) {

				/* Make sure this identifier wasn't introduced as a variable. */
                AstNode declaration = scope.getVariableDeclaration(specialTypeCheck.identifier);
				if(declaration != null && declaration.getChangeType() == ChangeType.INSERTED) return;

				/* Is the identifier already in the map? */
				if(sourceLE.specialTypeIdentifiers.containsKey(specialTypeCheck.identifier)) {

					/* Add the special type to the list of types the
					 * identifier could possibly be (based on the type check). */
					List<SpecialType> couldBe = sourceLE.specialTypeIdentifiers.get(specialTypeCheck.identifier);
					if(!couldBe.contains(specialTypeCheck.specialType)) couldBe.add(specialTypeCheck.specialType);

				}
				else {

					/* Add the special type to the list of types the
					 * identifier could possibly be (based on the type check). */
					LinkedList<SpecialType> couldBe = new LinkedList<SpecialType>();
					couldBe.add(specialTypeCheck.specialType);
                    sourceLE.specialTypeIdentifiers.put(specialTypeCheck.identifier, couldBe);

				}

			}
			else {

				/* Make sure this identifier wasn't introduced as a variable. */
                AstNode declaration = scope.getVariableDeclaration(specialTypeCheck.identifier);
				if(declaration != null && declaration.getChangeType() == ChangeType.INSERTED) return;

				/* Is the identifier already in the map? */
				if(sourceLE.protectedIdentifiers.containsKey(specialTypeCheck.identifier)) {

					/* Add the special type to the list of types the
					 * identifier could not possibly be (based on the type check). */
					List<SpecialType> couldBe = sourceLE.protectedIdentifiers.get(specialTypeCheck.identifier);
					if(!couldBe.contains(specialTypeCheck.specialType)) couldBe.add(specialTypeCheck.specialType);

				}
				else {

					/* Add the special type to the list of types the
					 * identifier could not possibly be (based on the type check). */
					LinkedList<SpecialType> couldNotBe = new LinkedList<SpecialType>();
					couldNotBe.add(specialTypeCheck.specialType);
                    sourceLE.protectedIdentifiers.put(specialTypeCheck.identifier, couldNotBe);
				}
			}
		}

	}

	@Override
	public void transfer(CFGNode node, ProtectedLatticeElement sourceLE, Scope<AstNode> scope,
						 Map<IPredicate, IRelation> facts,
						 SourceCodeFileChange sourceCodeFileChange) {

		AstNode statement = (AstNode)node.getStatement();

		/* Loop through the moved or unchanged identifiers that are used in
		 * this statement. */
        Set<String> usedIdentifiers = AnalysisUtilities.getUsedIdentifiers(statement);
        for(String identifier : sourceLE.protectedIdentifiers.keySet()) {

        	/* Make sure this is a valid path... */
        	if(sourceLE.specialTypeIdentifiers.containsKey(identifier)) continue;

        	/* Is the identifier in our "definitely not a special type" list? */
        	if(usedIdentifiers.contains(identifier)) {

        		/* Check that this identifier hasn't been newly assigned to
        		 * the special type we are checking. */
        		SpecialType assignedTo = sourceLE.assignments.get(identifier);
        		if(assignedTo != SpecialType.FALSEY) {

        			List<SpecialType> specialTypes = sourceLE.protectedIdentifiers.get(identifier);
        			for(SpecialType specialType : specialTypes) {

        				if(assignedTo != specialType) {

                            /* Trigger an alert! */
//        					List<SpecialTypeCheckResult> identifierResults = null;
//        					if(this.specialTypeCheckResults.containsKey(identifier)) {
//        						identifierResults = this.specialTypeCheckResults.get(identifier);
//        					}
//        					else {
//        						identifierResults = new LinkedList<SpecialTypeCheckResult>();
//								this.specialTypeCheckResults.put(identifier, identifierResults);
//        					}
//							identifierResults.add(new SpecialTypeCheckResult(identifier, specialType));

							/* Register the ProtectedSpecialType fact. */
							this.protectedSpecialType(identifier, specialType, facts, sourceCodeFileChange);


        				}

        			}

        		}

        	}
        }

        /* Check if the statement has an assignment. */
        List<Pair<String, AstNode>> assignments = SpecialTypeAnalysisUtilities.getIdentifierAssignments(statement);

        for(Pair<String, AstNode> assignment : assignments) {

        	SpecialType specialType = SpecialTypeAnalysisUtilities.getSpecialType(assignment.getValue());

        	/* Store the assignment if it is a new special type assignment. */
        	if(specialType != null && (assignment.getValue().getChangeType() == ChangeType.INSERTED
        			|| assignment.getValue().getChangeType() == ChangeType.REMOVED
        			|| assignment.getValue().getChangeType() == ChangeType.UPDATED)) {
        		sourceLE.assignments.put(assignment.getKey(), specialType);
        	}

        	/* Remove the assignment (if it exists) if it is not (any old
        	 * assignments are no longer relevant). */
        	else {
        		sourceLE.assignments.remove(assignment.getKey());
        	}

        	/* Remove the identifier from the special type set (if it exists). */
        	if(sourceLE.protectedIdentifiers.containsKey(assignment.getKey())) {

        		/* Remove the identifier. */
        		sourceLE.protectedIdentifiers.remove(assignment.getKey());

        	}

        }

	}

	@Override
	public ProtectedLatticeElement copy(ProtectedLatticeElement le) {
		return ProtectedLatticeElement.copy(le);
	}

	/**
	 * Stores an identifier that was used in a new special type check, and then
	 * used on the 'guaranteed not a special type' path.
	 */
	public class SpecialTypeCheckResult {

		public String identifier;
		public SpecialType specialType;

		public SpecialTypeCheckResult(String identifier, SpecialType specialType) {
			this.identifier = identifier;
			this.specialType = specialType;
		}

		@Override
		public boolean equals(Object o) {

			if(!(o instanceof SpecialTypeCheckResult)) return false;

			SpecialTypeCheckResult cec = (SpecialTypeCheckResult) o;

			if(this.identifier.equals(cec.identifier) && this.specialType.equals(cec.specialType)) return true;

			return false;

		}

		@Override
		public int hashCode() {
			return (this.identifier + "-" + this.specialType).hashCode();
		}
	}

	/**
	 * Registers a ProtectedSpecialType fact.
	 * @param identifyer The identifier that was protected.
	 * @param type The type being protected against.
	 */
	private void protectedSpecialType(String identifier, SpecialType type,
									  Map<IPredicate, IRelation> facts,
									  SourceCodeFileChange sourceCodeFileChange) {

		/* Get the relation for this predicate from the fact base. */
		IPredicate predicate = Factory.BASIC.createPredicate("ProtectedSpecialType", 4);
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
				Factory.TERM.createString(sourceCodeFileChange.repairedFile),
				Factory.TERM.createString("MethodNA"),
				Factory.TERM.createString(identifier),
				Factory.TERM.createString(type.toString()));
		relation.add(tuple);

		facts.put(predicate, relation);

	}

}