package commitminer.diff.line;

/**
 * Stores an integer range for a RangeMap.
 */
public class Range implements Comparable<Range> {
	
	public Integer start;
	public Integer end;

	public Range(Integer start, Integer end) {
		this.start = start;
		this.end = end;
	}
	
	@Override
	public String toString() {
		return start + "-" + end;
	}

	@Override
	public int compareTo(Range r) {
		if(start < r.start) return -1;
		if(start > r.start) return 1;
		if(end < r.end) return -1;
		if(end > r.end) return 1;
		return 0;
	}

}
