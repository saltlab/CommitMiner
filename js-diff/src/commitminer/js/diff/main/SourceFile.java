package commitminer.js.diff.main;

import java.util.HashSet;
import java.util.Set;

public class SourceFile {

	public int totalLines;

	public Set<Integer> lineLines;
	public Set<Integer> astLines;
	public Set<Integer> controlLines;
	public Set<Integer> environmentLines;
	public Set<Integer> valueLines;

	public Set<String> lineFact;
	public Set<String> astFact;
	public Set<String> controlFact;
	public Set<String> environmentFact;
	public Set<String> valueFact;

	public SourceFile() {
		this.lineLines = new HashSet<Integer>();
		this.astLines = new HashSet<Integer>();
		this.controlLines = new HashSet<Integer>();
		this.environmentLines = new HashSet<Integer>();
		this.valueLines = new HashSet<Integer>();

		this.lineFact = new HashSet<String>();
		this.astFact = new HashSet<String>();
		this.controlFact = new HashSet<String>();
		this.environmentFact = new HashSet<String>();
		this.valueFact = new HashSet<String>();
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
			for(int line : fv.lines) {
				this.lineLines.add(line);
				this.lineFact.add(fv.subType + "~" + line + "~" + fv.change);
			}
		}
//		else if(fv.subType.equals("AST") && !fv.change.equals("MOVED")) {
		else if(fv.subType.equals("AST")) {
			for(int line : fv.lines) {
				this.astLines.add(line);
				this.astFact.add(fv.subType + "~" + line + "~" + fv.change);
			}
		}
		else if(fv.subType.equals("CONTROL")) {
			for(int line : fv.lines) {
				this.controlLines.add(line);
				this.controlFact.add(fv.subType + "~" + line + "~" + fv.change);
			}
		}
		else if(fv.subType.equals("ENV")) {
			for(int line : fv.lines) {
				this.environmentLines.add(line);
				this.environmentFact.add(fv.subType + "~" + line + "~" + fv.change);
			}
		}
		else if(fv.subType.equals("VAL")) {
			for(int line : fv.lines) {
				this.valueLines.add(line);
				this.valueFact.add(fv.subType + "~" + line + "~" + fv.change);
			}
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
	public int getFactSize(DiffType type) {
		return getFactSet(type).size();
	}

	/**
	 * Set subtracts the right set from the left set.
	 * @return the number of lines in {@code left} that were not in {@code right}
	 */
	public int subtract(DiffType left, DiffType right) {
		Set<Integer> leftSet = getLineSet(left);
		Set<Integer> rightSet = getLineSet(right);
		Set<Integer> tmp = new HashSet<Integer>(leftSet);
		tmp.removeAll(rightSet);
		return tmp.size();
	}

	/**
	 * @return The fact set associated with the diff type.
	 */
	private Set<String> getFactSet(DiffType type) {
		switch(type) {
		default:
		case MULTI:
			Set<String> multi = new HashSet<String>();
			multi.addAll(this.controlFact);
			multi.addAll(this.environmentFact);
			multi.addAll(this.valueFact);
			return multi;
		case LINE: return this.lineFact;
		case AST: return this.astFact;
		case CONTROL: return this.controlFact;
		case ENVIRONMENT: return this.environmentFact;
		case VALUE: return this.valueFact;
		}
	}

	/**
	 * @return The line set associated with the diff type.
	 */
	private Set<Integer> getLineSet(DiffType type) {
		switch(type) {
		default:
		case MULTI:
			Set<Integer> multi = new HashSet<Integer>();
			multi.addAll(this.controlLines);
			multi.addAll(this.environmentLines);
			multi.addAll(this.valueLines);
			return multi;
		case LINE: return this.lineLines;
		case AST: return this.astLines;
		case CONTROL: return this.controlLines;
		case ENVIRONMENT: return this.environmentLines;
		case VALUE: return this.valueLines;
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
