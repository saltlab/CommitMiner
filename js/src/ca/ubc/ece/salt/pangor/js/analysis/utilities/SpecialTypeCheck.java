package ca.ubc.ece.salt.pangor.js.analysis.utilities;

import ca.ubc.ece.salt.gumtree.ast.ClassifiedASTNode.ChangeType;
import ca.ubc.ece.salt.pangor.js.analysis.utilities.SpecialTypeAnalysisUtilities.SpecialType;


/**
 * Stores the details of a special typ check in a conditional.
 */
public class SpecialTypeCheck {

	public String identifier;
	public SpecialType specialType;
	public boolean isSpecialType;
	public ChangeType changeType;

	public SpecialTypeCheck(String identifier, SpecialType specialType,
							boolean isSpecialType, ChangeType changeType) {
		this.identifier = identifier;
		this.specialType = specialType;
		this.isSpecialType = isSpecialType;
		this.changeType = changeType;
	}

}