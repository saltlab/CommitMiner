package ca.ubc.ece.salt.pangor.js.classify.protect;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

import org.apache.commons.lang3.tuple.Pair;
import org.mozilla.javascript.ast.AstNode;

import ca.ubc.ece.salt.gumtree.ast.ClassifiedASTNode.ChangeType;
import ca.ubc.ece.salt.pangor.analysis.flow.IAbstractState;
import ca.ubc.ece.salt.pangor.cfg.CFGEdge;
import ca.ubc.ece.salt.pangor.cfg.CFGNode;
import ca.ubc.ece.salt.pangor.js.analysis.utilities.SpecialTypeAnalysisUtilities;
import ca.ubc.ece.salt.pangor.js.analysis.utilities.SpecialTypeAnalysisUtilities.SpecialType;
import ca.ubc.ece.salt.pangor.js.analysis.utilities.SpecialTypeCheck;
import ca.ubc.ece.salt.pangor.js.classify.protect.ProtectedLatticeElement.Change;
import ca.ubc.ece.salt.pangor.js.classify.protect.ProtectedLatticeElement.Element;

public class ProtectedAbstractState implements IAbstractState {

	/**
	 * Keeps track of identifiers and their lattice elements.
	 */
	public Map<LatticeElement, ProtectedLatticeElement> latticeElements;

	public ProtectedAbstractState() {
		this.latticeElements = new TreeMap<LatticeElement, ProtectedLatticeElement>();
	}

	@Override
	public ProtectedAbstractState transfer(CFGEdge edge) {

		/* Make a copy of the abstract state. */
		ProtectedAbstractState as = new ProtectedAbstractState();
		for(Entry<LatticeElement, ProtectedLatticeElement> entries : this.latticeElements.entrySet()) {
			as.latticeElements.put(entries.getKey(), entries.getValue().copy());
		}

		AstNode condition = (AstNode)edge.getCondition();
		if(condition == null) return as;

		/* Check if condition has an inserted special type check and whether
		 * the check evaluates to true or false. */
		ProtectedVisitor visitor = new ProtectedVisitor(condition);
		condition.visit(visitor);

		/* Add any special type checks to the lattice element. */
		for(SpecialTypeCheck specialTypeCheck : visitor.getSpecialTypeChecks()) {

			LatticeElement key = new LatticeElement(specialTypeCheck.identifier, specialTypeCheck.specialType);

			Element element;
			Change change;

			switch(specialTypeCheck.changeType) {
			case INSERTED:
			case REMOVED:
				change = Change.IR;
			case UNCHANGED:
			case MOVED:
			case UPDATED:
			default:
				change = Change.U;
			}

			if(specialTypeCheck.isSpecialType) element = Element.EQ;
			else element = Element.NE;

			as.latticeElements.put(key, new ProtectedLatticeElement(element, change));

		}

		return as;

	}

	@Override
	public ProtectedAbstractState transfer(CFGNode node) {

		/* Make a copy of the abstract state. */
		ProtectedAbstractState as = new ProtectedAbstractState();
		for(Entry<LatticeElement, ProtectedLatticeElement> entries : this.latticeElements.entrySet()) {
			as.latticeElements.put(entries.getKey(), entries.getValue().copy());
		}

		AstNode statement = (AstNode)node.getStatement();

        /* Get the list of assignments for the statement. */
        List<Pair<String, AstNode>> assignments = SpecialTypeAnalysisUtilities.getIdentifierAssignments(statement);

        /* Update the lattice elements of any assigned identifiers. */
        for(Pair<String, AstNode> assignment : assignments) {

        	SpecialType specialType = SpecialTypeAnalysisUtilities.getSpecialType(assignment.getValue());

			Element element = Element.EQ;
        	Change change = assignment.getValue().getChangeType() == ChangeType.INSERTED ||
        			assignment.getValue().getChangeType() == ChangeType.REMOVED
        			? Change.IR
        			: Change.U;

        	LatticeElement le = new LatticeElement(assignment.getKey(), specialType);
        	as.latticeElements.put(le, new ProtectedLatticeElement(element, change));

        }

		return as;
	}

	@Override
	public ProtectedAbstractState join(IAbstractState ias) {

		if(!(ias instanceof ProtectedAbstractState)) throw new IllegalArgumentException("Need a ProtectedAbstractState.");

		ProtectedAbstractState as = (ProtectedAbstractState)ias;

		ProtectedAbstractState joined = new ProtectedAbstractState();

		for(LatticeElement le : this.latticeElements.keySet()) {
			ProtectedLatticeElement left = this.latticeElements.get(le);
			ProtectedLatticeElement right = as.latticeElements.containsKey(le)
					? as.latticeElements.get(le)
					: new ProtectedLatticeElement();

			joined.latticeElements.put(le, ProtectedLatticeElement.join(left, right));
		}

		for(LatticeElement le : as.latticeElements.keySet()) {
			if(joined.latticeElements.containsKey(le)) continue;
			ProtectedLatticeElement left = as.latticeElements.get(le);
			ProtectedLatticeElement right = new ProtectedLatticeElement();
			joined.latticeElements.put(le,  ProtectedLatticeElement.join(left, right));
		}

		return joined;

	}

	/**
	 * Identifies a lattice element. There is one lattice element for each
	 * <identifier, special type> pair.
	 */
	public class LatticeElement {
		public String identifier;
		public SpecialType type;

		public LatticeElement(String identifier, SpecialType type) {
			this.identifier = identifier;
			this.type = type;
		}

		@Override
		public int hashCode() {
			return this.identifier.hashCode();
		}

		@Override
		public boolean equals(Object o) {
			if(o instanceof LatticeElement) {
				LatticeElement lid = (LatticeElement) o;
				if(this.identifier.equals(lid.identifier)
						&& this.type == lid.type) return true;
			}
			return false;
		}
	}

}
