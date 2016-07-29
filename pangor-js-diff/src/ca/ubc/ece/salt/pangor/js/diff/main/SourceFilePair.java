package ca.ubc.ece.salt.pangor.js.diff.main;

public class SourceFilePair {

	public String project;
	public String commit;
	public String file;

	public SourceFile source;
	public SourceFile destination;

	public SourceFilePair(String commit, String file) {
		this.commit = commit;
		this.file = file;
		this.source = new SourceFile();
		this.destination = new SourceFile();
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