package ca.ubc.ece.salt.pangor.js.diff.main;

import java.util.HashSet;
import java.util.Set;

public class SourceFile {

	public int totalLines;

	public Set<Integer> line;
	public Set<Integer> ast;
	public Set<Integer> astMoved;
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

}
