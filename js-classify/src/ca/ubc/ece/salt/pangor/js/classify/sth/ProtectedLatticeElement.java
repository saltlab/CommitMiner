package ca.ubc.ece.salt.pangor.js.classify.sth;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import ca.ubc.ece.salt.pangor.analysis.flow.IAbstractState;
import ca.ubc.ece.salt.pangor.js.analysis.utilities.SpecialTypeAnalysisUtilities.SpecialType;

/**
 * ProtectedLatticeElement
 *
 * Tracks identifier types.
 */
public class ProtectedLatticeElement extends IAbstractState{

	/**
	 * Keeps track of identifiers that are special types on the path.
	 */
	public Map<String, List<SpecialType>> specialTypeIdentifiers;

	/**
	 * Keeps track of identifiers that are protected (not special types) on the path.
	 */
	public Map<String, List<SpecialType>> protectedIdentifiers;

	/**
	 * Keeps track of new special type assignments.
	 */
	public Map<String, SpecialType> assignments;

	public ProtectedLatticeElement() {
		super();
		this.specialTypeIdentifiers = new HashMap<String, List<SpecialType>>();
		this.protectedIdentifiers = new HashMap<String, List<SpecialType>>();
		this.assignments = new HashMap<String, SpecialType>();
	}

	public ProtectedLatticeElement(Map<Long, Integer> visitedEdges) {
		super(visitedEdges);
		this.specialTypeIdentifiers = new HashMap<String, List<SpecialType>>();
		this.protectedIdentifiers = new HashMap<String, List<SpecialType>>();
		this.assignments = new HashMap<String, SpecialType>();
	}

	public ProtectedLatticeElement(Map<String, List<SpecialType>> specialTypes, Map<String, List<SpecialType>> nonSpecialTypes, Map<String, SpecialType> assignments, Map<Long, Integer> visitedEdges) {
		super(visitedEdges);
		this.specialTypeIdentifiers = specialTypes;
		this.protectedIdentifiers = nonSpecialTypes;
		this.assignments = assignments;
	}

	/**
	 * @return a copy of the LatticeElement.
	 */
	public static ProtectedLatticeElement copy(ProtectedLatticeElement le) {

		/* Make copies of the special type maps. We also need to make a copy
		 * of all the lists inside each mapping. */
		HashMap<String, List<SpecialType>> specialTypes = new HashMap<String, List<SpecialType>>();
		for(String key : le.specialTypeIdentifiers.keySet()) {
			specialTypes.put(key, new LinkedList<SpecialType>(le.specialTypeIdentifiers.get(key)));
		}

		HashMap<String, List<SpecialType>> nonSpecialTypes = new HashMap<String, List<SpecialType>>();
		for(String key : le.protectedIdentifiers.keySet()) {
			nonSpecialTypes.put(key, new LinkedList<SpecialType>(le.protectedIdentifiers.get(key)));
		}

		/* Return a copy of this lattice element. */
		return new ProtectedLatticeElement(specialTypes,
											 nonSpecialTypes,
											 new HashMap<String, SpecialType>(le.assignments),
											 new HashMap<Long, Integer>(le.visitedEdges));
	}

}