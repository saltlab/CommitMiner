package ca.ubc.ece.salt.pangor.js.diff.main;

public class SourceFileDiffComparison {

	// Intersection: Lines covered by both lhs and rhs
	public int astLineIntersection;

	public int conLineIntersection;
	public int envLineIntersection;
	public int valLineIntersection;

	public int conAstIntersection;
	public int envAstIntersection;
	public int valAstIntersection;

	// Subtraction: Lines covered by lhs which are not covered by rhs
	public int astLineSubtraction;

	public int conLineSubtraction;
	public int envLineSubtraction;
	public int valLineSubtraction;

	public int conAstSubtraction;
	public int envAstSubtraction;
	public int valAstSubtraction;

}
