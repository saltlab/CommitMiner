package ca.ubc.ece.salt.pangor.js.diff.main;


public class SourceFileDiffComparison {

	// Identifier information
	public String commit;
	public String file;

	// Total number of lines in the file
	public int totalLines;

	// Total number of facts presented by each diff
	public int lineChanges;
	public int astChanges;
	public int conChanges;
	public int envChanges;
	public int valChanges;
	public int multiChanges;

	// Subtraction: Lines covered by lhs which are not covered by rhs
	public int conAstSubtraction;
	public int envAstSubtraction;
	public int valAstSubtraction;
	public int multiAstSubtraction;

}
