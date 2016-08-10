package ca.ubc.ece.salt.pangor.js.diff.main;

import java.util.HashSet;
import java.util.Set;

public class SourceFile {

	public int totalLines;

	public Set<Integer> line;
	public Set<Integer> ast;
	public Set<Integer> astUpdated;
	public Set<Integer> control;
	public Set<Integer> environment;
	public Set<Integer> value;

	public SourceFile() {
		this.line = new HashSet<Integer>();
		this.ast = new HashSet<Integer>();
		this.control = new HashSet<Integer>();
		this.environment = new HashSet<Integer>();
		this.value = new HashSet<Integer>();
	}

	/**
	 * Loads a feature vector into this source file.
	 * @param fv Any feature vector.
	 */
	public void interpretFeatureVector(FV fv) {

		if(fv.subType.equals("TOTAL_LINES")) {
			this.totalLines = Integer.parseInt(fv.change);
		}
		else if(fv.subType.equals("LINE")) {
			this.line.add(fv.line);
		}
		else if(fv.subType.equals("AST") && !fv.change.equals("MOVED")) {
//		else if(fv.subType.equals("AST")) {
			this.ast.add(fv.line);
		}
		else if(fv.subType.equals("CONTROL")) {
			this.control.add(fv.line);
		}
		else if(fv.subType.equals("ENV")) {
			this.environment.add(fv.line);
		}
		else if(fv.subType.equals("VAL")) {
			this.value.add(fv.line);
		}

	}

	/**
	 * @return the total number of lines in the file
	 */
	public int getTotalLines() {
		return this.totalLines;
	}

	/**
	 * @return the size of the diff for the file
	 */
	public int getSize(DiffType type) {
		return getDiffSet(type).size();
	}

	/**
	 * Set subtracts the right set from the left set.
	 * @return the number of lines in {@code left} that were not in {@code right}
	 */
	public int subtract(DiffType left, DiffType right) {
		Set<Integer> leftSet = getDiffSet(left);
		Set<Integer> rightSet = getDiffSet(right);
		Set<Integer> tmp = new HashSet<Integer>(leftSet);
		tmp.removeAll(rightSet);
		return tmp.size();
	}

	/**
	 * @return The set associated with the diff type.
	 */
	private Set<Integer> getDiffSet(DiffType type) {
		switch(type) {
		default:
		case MULTI:
			Set<Integer> multi = new HashSet<Integer>();
			multi.addAll(this.control);
			multi.addAll(this.environment);
			multi.addAll(this.value);
			return multi;
		case LINE: return this.line;
		case AST: return this.ast;
		case CONTROL: return this.control;
		case ENVIRONMENT: return this.environment;
		case VALUE: return this.value;
		}
	}

	public static enum DiffType {
		MULTI,
		LINE,
		AST,
		CONTROL,
		ENVIRONMENT,
		VALUE
	}

}
