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
		this.astMoved = new HashSet<Integer>();
		this.astUpdated = new HashSet<Integer>();
		this.control = new HashSet<Integer>();
		this.environment = new HashSet<Integer>();
		this.value = new HashSet<Integer>();
	}

}
