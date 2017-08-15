package commitminer.js.metrics;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.SortedSet;

import commitminer.analysis.Commit;
import commitminer.analysis.SourceCodeFileChange;
import commitminer.analysis.annotation.Annotation;
import commitminer.analysis.annotation.AnnotationDataSet;

public class AnnotationMetricsPostprocessor {
	
	private String path;

	/**
	 * @param path The path to the output file.
	 */
	public AnnotationMetricsPostprocessor(String path) {
		this.path = path;
	}
	
	public void process(Commit commit, SourceCodeFileChange file, AnnotationDataSet dataSet) throws IOException {

		/* Convenience */
		String code = file.repairedCode;
		SortedSet<Annotation> annotations = dataSet.getAnnotationFactBase().getAnnotations();

		CommitMetrics metrics = new CommitMetrics(commit, file);
		
		metrics.totLin = code.split("\n").length;
		
		for(Annotation annotation : annotations) {
			switch(annotation.label) {
			case "CONDEP-DEF":
				metrics.cntDef++;
				break;
			case "CONDEP-USE":
				metrics.cntUse++;
				break;
			case "DATDEP-XDEF":
				metrics.datDef++;
				break;
			case "DATDEP-USE":
				metrics.datUse++;
				break;
			case "ENV-DEF":
				metrics.varDef++;
				break;
			case "ENV-USE":
				metrics.varUse++;
				break;
			case "CON-DEF":
				metrics.cndDef++;
				break;
			case "CON-USE":
				metrics.cndUse++;
				break;
			case "CALL-DEF":
				metrics.calDef++;
				break;
			case "CALL-USE":
				metrics.calUse++;
				break;
			case "VAL-DEF":
				metrics.valDef++;
				break;
			case "VAL-USE":
				metrics.valUse++;
				break;
			}
			
		}

		this.writeRow(metrics);
		System.out.println(metrics.toString());
		
	}
	
	public void writeRow(CommitMetrics metrics) throws IOException {
		Files.write(Paths.get(path), metrics.toRow().getBytes(), StandardOpenOption.CREATE, StandardOpenOption.APPEND);
	}
	
	public void writeHeader() throws IOException{
		Files.write(Paths.get(path), this.getHeader().getBytes(), StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
	}
	
	public String getHeader() {
		String s = "ProjectID";
		s += ", CommitID";
		s += ", File";
		s += ", TotalLines";
		s += ", DataDepDef";
		s += ", DataDepUse";
		s += ", ControlDepDef";
		s += ", ControlDepUse";
		s += ", VariableDef";
		s += ", VariableUse";
		s += ", CallDef";
		s += ", CallUse";
		s += ", ConditionDef";
		s += ", ConditionUse";
		s += ", ValueDef";
		s += ", ValueUse";
		s += "\n";
		return s;
	}

	private class CommitMetrics {
		
		Commit commit;
		SourceCodeFileChange file;

		public int totLin = 0;
		public int totFun = 0;
		public int datDef = 0;
		public int datUse = 0;
		public int cntDef = 0;
		public int cntUse = 0;
		public int varDef = 0;
		public int varUse = 0;
		public int calDef = 0;
		public int calUse = 0;
		public int cndDef = 0;
		public int cndUse = 0;
		public int valDef = 0;
		public int valUse = 0;
		
		public CommitMetrics(Commit commit, SourceCodeFileChange file) {
			this.commit = commit;
			this.file = file;
		}
		
		@Override
		public String toString() {
			String s = "";
			s += "Project:       " + commit.projectID + "\n";
			s += "Commit:        " + commit.repairedCommitID + "\n";
			s += "File:          " + file.repairedFile + "\n";
			s += "---------------\n";
			s += "Total Lines:   " + totLin + "\n";
			s += "---------------\n";
			s += "Datadep Def:   " + datDef + "\n";
			s += "Datadep Use:   " + datUse + "\n";
			s += "Condep Def:    " + cntDef + "\n";
			s += "Condep Use:    " + cntUse + "\n";
			s += "---------------\n";
			s += "Variable Def:  " + varDef + "\n";
			s += "Variable Use:  " + varUse + "\n";
			s += "Call Def:      " + calDef + "\n";
			s += "Call Use:      " + calUse + "\n";
			s += "Condition Def: " + cndDef + "\n";
			s += "Condition Use: " + cndUse + "\n";
			s += "Value Def:     " + valDef + "\n";
			s += "Value Use:     " + valUse + "\n";
			return s;
		}

		public String toRow() {
			String s = "'" + commit.projectID + "'";
			s += ", '" + commit.repairedCommitID + "'";
			s += ", '" + file.repairedFile + "'";
			s += ", " + totLin;
			s += ", " + datDef;
			s += ", " + datUse;
			s += ", " + cntDef;
			s += ", " + cntUse;
			s += ", " + varDef;
			s += ", " + varUse;
			s += ", " + calDef;
			s += ", " + calUse;
			s += ", " + cndDef;
			s += ", " + cndUse;
			s += ", " + valDef;
			s += ", " + valUse;
			return s;
		}
	}
	
}
