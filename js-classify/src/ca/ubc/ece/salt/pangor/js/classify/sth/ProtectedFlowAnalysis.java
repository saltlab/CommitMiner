package ca.ubc.ece.salt.pangor.js.classify.sth;

import org.mozilla.javascript.ast.ScriptNode;

import ca.ubc.ece.salt.pangor.analysis.flow.FlowAnalysis;

/**
 * A change-sensitive analysis that identifies where identifiers are protected in JavaScript.
 */
public class ProtectedFlowAnalysis extends FlowAnalysis {

	@Override
	public ProtectedAbstractState entryValue(ScriptNode function) {
		return new ProtectedAbstractState();
	}

}