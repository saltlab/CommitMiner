package commitminer.js.diff.main;

import org.apache.commons.lang3.StringUtils;

class FV {
	public int id;					// 0
	public String project;			// 1
	public String commitType;		// 2
	public String url;				// 3
	public String bic;				// 4
	public String bfc;				// 5
	public String version;			// 6
	public String file;				// 7
	public int statement;			// 8
	public int[] lines; 			// 9
	public String type;				// 10
	public String subType; 			// 11
	public String change;			// 12

	public FV(String[] tokens) {
		this.id = Integer.parseInt(tokens[0]);
		this.project = tokens[1];
		this.commitType = tokens[2];
		this.url = tokens[3];
		this.bic = tokens[4];
		this.bfc = tokens[5];
		this.version = tokens[6];
		this.file = tokens[7];
		this.statement = StringUtils.isNumeric(tokens[8]) ? Integer.parseInt(tokens[8]) : -1;
		this.lines = getLines(tokens[9]);
		this.type = tokens[10];
		this.subType = tokens[11];
		this.change = tokens[12];
	}

	/**
	 * Create an array of line numbers from the serialized data.
	 * @param token has the format "{n1,n2,...,nN}"
	 * @return The de-serialized integer array
	 */
	private int[] getLines(String serial) {
		if(serial.length() == 2) return null;
		String csv = serial.substring(1, serial.length() - 1);
		String[] tokens = csv.split(";");
		int[] lines = new int[tokens.length];
		for(int i = 0; i < tokens.length; i++) {
			lines[i] = Integer.parseInt(tokens[i]);
		}
		return lines;
	}

	@Override
	public int hashCode() {
		return (project + "~" + bfc + "~" + file).hashCode();
	}

	@Override
	public boolean equals(Object o) {
		if(o instanceof FV) {
			FV fv = (FV)o;
			if(this.project.equals(fv.project)
					&& this.bfc.equals(fv.bfc)
					&& this.file.equals(fv.file))
				return true;
		}
		else if(o instanceof SourceFilePair) {
			SourceFilePair sfp = (SourceFilePair)o;
			if(this.project.equals(sfp.project)
					&& this.bfc.equals(sfp.commit)
					&& this.file.equals(sfp.file))
				return true;
		}
		return false;
	}

}