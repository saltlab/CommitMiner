package ca.ubc.ece.salt.pangor.js.classify.sth;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.deri.iris.api.basics.IPredicate;
import org.deri.iris.storage.IRelation;
import org.mozilla.javascript.ast.AstNode;
import org.mozilla.javascript.ast.ScriptNode;

import ca.ubc.ece.salt.pangor.analysis.SourceCodeFileChange;
import ca.ubc.ece.salt.pangor.analysis.flow.PathInsensitiveFlowAnalysis;
import ca.ubc.ece.salt.pangor.cfg.CFGEdge;
import ca.ubc.ece.salt.pangor.cfg.CFGNode;
import ca.ubc.ece.salt.pangor.js.analysis.scope.Scope;
import ca.ubc.ece.salt.pangor.js.analysis.utilities.SpecialTypeAnalysisUtilities.SpecialType;

public class ProtectedFlowAnalysisPI extends PathInsensitiveFlowAnalysis<ProtectedLatticeElement> {

	public ProtectedFlowAnalysisPI() {
		// TODO Auto-generated constructor stub
	}

	@Override
	public ProtectedLatticeElement entryValue(ScriptNode function) {
		return new ProtectedLatticeElement();
	}

	@Override
	protected ProtectedLatticeElement join(ProtectedLatticeElement left,
			ProtectedLatticeElement right) {

		Map<Long, Integer> visitedEdges = new HashMap<Long, Integer>();
		visitedEdges.putAll(left.visitedEdges);
		visitedEdges.putAll(right.visitedEdges);

		Map<String, List<SpecialType>> specialTypeIdentifiers = new HashMap<String, List<SpecialType>>();
		Map<String, List<SpecialType>> protectedIdentifiers = new HashMap<String, List<SpecialType>>();
		Map<String, SpecialType> assignments = new HashMap<String, SpecialType>();

		mergeProtected(protectedIdentifiers, left.protectedIdentifiers);
		mergeProtected(protectedIdentifiers, right.protectedIdentifiers);

		mergeProtected(specialTypeIdentifiers, left.specialTypeIdentifiers);
		mergeProtected(specialTypeIdentifiers, right.specialTypeIdentifiers);

		eliminateContradictions(protectedIdentifiers, specialTypeIdentifiers);


		return new ProtectedLatticeElement(specialTypeIdentifiers, protectedIdentifiers, assignments, visitedEdges);
	}

	/**
	 * Eliminates contradictions from the left set. Values in the sets should be mutually exclusive.
	 */
	private void eliminateContradictions(Map<String, List<SpecialType>> left, Map<String, List<SpecialType>> right) {

		/* Get all the identifiers. */
		Set<String> identifiers = new HashSet<String>();
		identifiers.addAll(left.keySet());
		identifiers.addAll(right.keySet());

		for(String identifier : identifiers) {

			List<SpecialType> contradictions = new LinkedList<SpecialType>();

			/* Nothing to be done. */
			if(!left.containsKey(identifier) || !right.containsKey(identifier)) continue;

			Set<SpecialType> leftTypes = new HashSet<SpecialType>(left.get(identifier));
			Set<SpecialType> rightTypes = new HashSet<SpecialType>(right.get(identifier));

			for(SpecialType leftType : leftTypes) {
				if(rightTypes.contains(leftType)) {
					contradictions.add(leftType);
				}
			}

			for(SpecialType type : contradictions) {
				leftTypes.remove(type);
				rightTypes.remove(type);
			}

			left.put(identifier, new LinkedList<SpecialType>(leftTypes));
			right.put(identifier, new LinkedList<SpecialType>(rightTypes));

		}

	}

	/**
	 * Merges right into left.
	 * @param left The structure to copy right's vales into.
	 * @param right The values to copy into left.
	 */
	private void mergeAssignment(Map<String, SpecialType> left, Map<String, SpecialType> right) {

	}

	/**
	 * Merges right into left.
	 * @param left The structure to copy right's vales into.
	 * @param right The values to copy into left.
	 */
	private void mergeProtected(Map<String, List<SpecialType>> left,
					   Map<String, List<SpecialType>> right) {

		/* Get all identifiers from the right element. */
		for(Entry<String, List<SpecialType>> rightIdentifiers : right.entrySet()) {

			String identifier = rightIdentifiers.getKey();

			/* The list for storing the left special types. */
			List<SpecialType> specialTypes = left.get(identifier);
			if(specialTypes == null) {
				specialTypes = new LinkedList<SpecialType>();
				left.put(identifier, specialTypes);
			}

			/* Merge right into left. */
			for(SpecialType specialType : rightIdentifiers.getValue()) {
				specialTypes.add(specialType);
			}

		}
	}

	@Override
	public void transfer(CFGEdge edge, ProtectedLatticeElement sourceLE,
			Scope<AstNode> scope, Map<IPredicate, IRelation> facts,
			SourceCodeFileChange sourceCodeFileChange) {
		// TODO Auto-generated method stub

	}

	@Override
	public void transfer(CFGNode node, ProtectedLatticeElement sourceLE,
			Scope<AstNode> scope, Map<IPredicate, IRelation> facts,
			SourceCodeFileChange sourceCodeFileChange) {
		// TODO Auto-generated method stub

	}

	@Override
	public ProtectedLatticeElement copy(ProtectedLatticeElement le) {
		// TODO Auto-generated method stub
		return null;
	}

}