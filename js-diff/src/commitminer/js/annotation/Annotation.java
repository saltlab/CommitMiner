package commitminer.js.annotation;

import java.util.List;

import commitminer.analysis.annotation.DependencyIdentifier;

/**
 * Stores the label, references, and location of a source file annotation.
 */
public class Annotation {

	public String label;
	public List<DependencyIdentifier> dependencyIDs;
	public Integer line;
	public Integer absolutePosition;
	public Integer length;

	public Annotation(String label, 
					  List<DependencyIdentifier> dependencyIDs,
					  int line, int absolutePosition, int length) {
		this.label = label;
		this.dependencyIDs = dependencyIDs;
		this.line = line;
		this.absolutePosition = absolutePosition;
		this.length = length;
	}
	
	public String getLabel() {
		return label;
	}
	
	public List<DependencyIdentifier> getDependencyIdentifiers() {
		return dependencyIDs;
	}
	
	public Integer getLine() {
		return line;
	}
	
	public Integer getAbsolutePosition() {
		return absolutePosition;
	}
	
	public Integer getLength() {
		return length;
	}
	
	@Override
	public String toString() {
		String str = "Annotation: <" + label + "|" + line + "," + absolutePosition + "," + length + ",{";
		for(DependencyIdentifier id : dependencyIDs) {
			str += id.getAddress() + ",";
		}
		return str.substring(0, str.length() - 1) + "}>";
	}

}
