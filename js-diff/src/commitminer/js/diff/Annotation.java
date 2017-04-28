package commitminer.js.diff;

public class Annotation {

	public Integer line;
	public Integer absolutePosition;
	public Integer length;

	public Annotation(int line, int absolutePosition, int length) {
		this.line = line;
		this.absolutePosition = absolutePosition;
		this.length = length;
	}

}
