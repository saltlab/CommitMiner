package commitminer.js.analysis.utilities;

import commitminer.js.analysis.utilities.SpecialTypeAnalysisUtilities.SpecialType;

import ca.ubc.ece.salt.gumtree.ast.ClassifiedASTNode.ChangeType;


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