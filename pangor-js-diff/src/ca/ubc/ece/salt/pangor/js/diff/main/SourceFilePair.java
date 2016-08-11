package ca.ubc.ece.salt.pangor.js.diff.main;

import ca.ubc.ece.salt.pangor.js.diff.main.SourceFile.DiffType;


public class SourceFilePair {

	public String project;
	public String commit;
	public String file;
	public String url;

	public SourceFile source;
	public SourceFile destination;

	public SourceFilePair(String project, String commit, String file, String url) {
		this.project = project;
		this.commit = commit;
		this.file = file;
		this.url = url;
		this.source = new SourceFile();
		this.destination = new SourceFile();
	}

	/**
	 * @return the averages size of the file in # of lines
	 */
	public int getAvgTotalLines() {
		int s = this.source.totalLines;
		int d = this.destination.totalLines;
		return (s+d)/2;
	}

	/**
	 * Set subtracts the right set from the left set.
	 * @return the number of lines in {@code left} that were not in {@code right}
	 */
	public int subtract(DiffType left, DiffType right) {
		int s = this.source.subtract(left, right);
		int d = this.destination.subtract(left, right);
		return s + d;
	}

	/**
	 * @return the size of the diff for the file
	 */
	public int getFactSize(DiffType type) {
		int s = this.source.getFactSize(type);
		int d = this.destination.getFactSize(type);
		return s + d;
	}

	@Override
	public int hashCode() {
		return (project + "~" + commit + "~" + file).hashCode();
	}

	@Override
	public boolean equals(Object o) {
		if(o instanceof FV) {
			FV fv = (FV)o;
			if(this.project.equals(fv.project)
					&& this.commit.equals(fv.bfc)
					&& this.file.equals(fv.file))
				return true;
		}
		else if(o instanceof SourceFilePair) {
			SourceFilePair sfp = (SourceFilePair)o;
			if(this.project.equals(sfp.project)
					&& this.commit.equals(sfp.commit)
					&& this.file.equals(sfp.file))
				return true;
		}
		return false;
	}
}