package ca.ubc.ece.salt.pangor.js.diff.main;

public class MetricsRow {

	public Type type;
	public int added;
	public int ast;

	public MetricsRow(Type type, int added, int ast) {
		this.type = type;
		this.added = added;
		this.ast = ast;
	}

	public String serialize() {
		return type + "," + added + "," + ast;
	}

	public enum Type {
		ALL,
		CONTROL,
		ENVIRONMENT,
		VALUE
	}

}
