package ca.ubc.ece.salt.pangor.js.classify.sth;

import java.util.Map;
import java.util.TreeMap;

import ca.ubc.ece.salt.pangor.analysis.flow.IAbstractState;
import ca.ubc.ece.salt.pangor.cfg.CFGEdge;
import ca.ubc.ece.salt.pangor.cfg.CFGNode;
import ca.ubc.ece.salt.pangor.js.analysis.utilities.SpecialTypeAnalysisUtilities.SpecialType;

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
		// TODO Auto-generated method stub
		return null;
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
