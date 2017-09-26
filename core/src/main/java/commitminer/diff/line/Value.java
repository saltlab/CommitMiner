package commitminer.diff.line;

public class Value implements Comparable<Range>{
	
	private Integer value;

	public Value(Integer value) {
		this.value = value;
	}
	
	public Integer getValue() {
		return value;
	}

	@Override
	public int compareTo(Range r) {
		if(r.start > value) return -1;
		if(r.end < value) return 1;
		return 0;
	}

}
