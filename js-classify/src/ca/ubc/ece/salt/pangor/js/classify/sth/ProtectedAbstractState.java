package ca.ubc.ece.salt.pangor.js.classify.sth;

import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

import org.mozilla.javascript.ast.AstNode;

import ca.ubc.ece.salt.pangor.analysis.flow.IAbstractState;
import ca.ubc.ece.salt.pangor.cfg.CFGEdge;
import ca.ubc.ece.salt.pangor.cfg.CFGNode;
import ca.ubc.ece.salt.pangor.js.analysis.utilities.SpecialTypeAnalysisUtilities.SpecialType;
import ca.ubc.ece.salt.pangor.js.analysis.utilities.SpecialTypeCheck;
import ca.ubc.ece.salt.pangor.js.classify.sth.TypeLatticeElement.Change;
import ca.ubc.ece.salt.pangor.js.classify.sth.TypeLatticeElement.Element;

public class ProtectedAbstractState implements IAbstractState<ProtectedAbstractState> {

	/**
	 * Keeps track of identifiers and their lattice elements.
	 */
	public Map<LatticeElement, TypeLatticeElement> latticeElements;

	public ProtectedAbstractState() {
		this.latticeElements = new TreeMap<LatticeElement, TypeLatticeElement>();
	}

	@Override
	public ProtectedAbstractState transfer(CFGEdge edge) {

		/* Make a copy of the abstract state. */
		ProtectedAbstractState as = new ProtectedAbstractState();
		for(Entry<LatticeElement, TypeLatticeElement> entries : this.latticeElements.entrySet()) {
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

			TypeLatticeElement.Element element;
			TypeLatticeElement.Change change;

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

			as.latticeElements.put(key, new TypeLatticeElement(element, change));

		}

		return as;

	}

	@Override
	public ProtectedAbstractState transfer(CFGNode node) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ProtectedAbstractState join(ProtectedAbstractState as) {
		ProtectedAbstractState joined = new ProtectedAbstractState();

		for(LatticeElement le : this.latticeElements.keySet()) {
			TypeLatticeElement left = this.latticeElements.get(le);
			TypeLatticeElement right = as.latticeElements.containsKey(le)
					? as.latticeElements.get(le)
					: new TypeLatticeElement();

			joined.latticeElements.put(le, TypeLatticeElement.join(left, right));
		}

		for(LatticeElement le : as.latticeElements.keySet()) {
			if(joined.latticeElements.containsKey(le)) continue;
			TypeLatticeElement left = as.latticeElements.get(le);
			TypeLatticeElement right = new TypeLatticeElement();
			joined.latticeElements.put(le,  TypeLatticeElement.join(left, right));
		}

		return joined;
	}

	/**
	 * Identifies a lattice element. There is one lattice element for each
	 * <identifier, special type> pair.
	 */
	private class LatticeElement {
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
