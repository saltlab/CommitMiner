package commitminer.analysis.annotation;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.HashSet;
import java.util.Set;
import java.util.SortedSet;

import commitminer.analysis.Commit;
import commitminer.analysis.SourceCodeFileChange;

public class AnnotationMetricsPostprocessor {
	
	private String path;

	/**
	 * @param path The path to the output file.
	 */
	public AnnotationMetricsPostprocessor(String path) {
		this.path = path;
	}
	
	private int getNumLines(String code, Annotation annotation) {
		String artifact = code.substring(annotation.getAbsolutePosition(), 
										 annotation.getAbsolutePosition() 
										 	+ annotation.getLength());
		return artifact.split("\n").length;
	}
	
	/**
	 * Add the lines from the annotation to the given set.
	 */
	private void addLineNums(String code, Annotation annotation, Set<Integer> lines) {
		int numLines = getNumLines(code, annotation);
		for(int i = annotation.line; i < annotation.line + numLines; i++) {
			lines.add(i);
		}
	}
	
	/**
	 * @return the set of line #s that were inserted.
	 */
	private Set<Integer> getInsertedLineSet(String code, SortedSet<Annotation> annotations) {
		Set<Integer> insertedLines = new HashSet<Integer>();
		for(Annotation annotation : annotations) {
			for(int i = annotation.getLine(); i < getNumLines(code, annotation); i++) {
				insertedLines.add(i);
			}
		}
		return insertedLines;
	}
	
	public void process(Commit commit, SourceCodeFileChange file, AnnotationFactBase factBase) throws IOException {

		/* Convenience */
		String code = file.repairedCode;
		SortedSet<Annotation> annotations = factBase.getAnnotations();

		CommitMetrics metrics = new CommitMetrics(commit, file);
		
		metrics.totLin = code.split("\n").length;
		
		for(Annotation annotation : annotations) {
			switch(annotation.label) {
			case "LINE-INSERTED":
				metrics.insLin.add(annotation.line);
				break;
			case "CONDEP-DEF":
				metrics.cntDef++;
				addLineNums(code, annotation, metrics.cntLin);
				break;
			case "CONDEP-USE":
				metrics.cntUse++;
				addLineNums(code, annotation, metrics.cntLin);
				break;
			case "DATDEP-XDEF":
				metrics.datDef++;
				addLineNums(code, annotation, metrics.datLin);
				break;
			case "DATDEP-USE":
				metrics.datUse++;
				addLineNums(code, annotation, metrics.datLin);
				break;
			case "ENV-DEF":
				metrics.varDef++;
				addLineNums(code, annotation, metrics.varLin);
				break;
			case "ENV-USE":
				metrics.varUse++;
				addLineNums(code, annotation, metrics.varLin);
				break;
			case "CON-DEF":
				metrics.cndDef++;
				addLineNums(code, annotation, metrics.cndLin);
				break;
			case "CON-USE":
				metrics.cndUse++;
				addLineNums(code, annotation, metrics.cndLin);
				break;
			case "CALL-DEF":
				metrics.calDef++;
				addLineNums(code, annotation, metrics.calLin);
				break;
			case "CALL-USE":
				metrics.calUse++;
				addLineNums(code, annotation, metrics.calLin);
				break;
			case "VAL-DEF":
				metrics.valDef++;
				addLineNums(code, annotation, metrics.valLin);
				break;
			case "VAL-USE":
				metrics.valUse++;
				addLineNums(code, annotation, metrics.valLin);
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
		s += ", Runtime";
		s += ", TotalLines";
		s += ", DataDepDef";
		s += ", DataDepUse";
		s += ", DataDepLines";
		s += ", DataDepOverlap";
		s += ", ControlDepDef";
		s += ", ControlDepUse";
		s += ", ControlDepLines";
		s += ", ControlDepOverlap";
		s += ", VariableDef";
		s += ", VariableUse";
		s += ", VariableLines";
		s += ", VariableOverlap";
		s += ", CallDef";
		s += ", CallUse";
		s += ", CallLines";
		s += ", CallOverlap";
		s += ", ConditionDef";
		s += ", ConditionUse";
		s += ", ConditionLines";
		s += ", ConditionOverlap";
		s += ", ValueDef";
		s += ", ValueUse";
		s += ", ValueLines";
		s += ", ValueOverlap";
		s += "\n";
		return s;
	}

	private class CommitMetrics {
		
		Commit commit;
		SourceCodeFileChange file;

		public int totLin = 0;
		public Set<Integer> insLin = new HashSet<Integer>();
		public int datDef = 0;
		public int datUse = 0;
		public Set<Integer> datLin = new HashSet<Integer>();
		public int cntDef = 0;
		public int cntUse = 0;
		public Set<Integer> cntLin = new HashSet<Integer>();
		public int varDef = 0;
		public int varUse = 0;
		public Set<Integer> varLin = new HashSet<Integer>();
		public int calDef = 0;
		public int calUse = 0;
		public Set<Integer> calLin = new HashSet<Integer>();
		public int cndDef = 0;
		public int cndUse = 0;
		public Set<Integer> cndLin = new HashSet<Integer>();
		public int valDef = 0;
		public int valUse = 0;
		public Set<Integer> valLin = new HashSet<Integer>();
		
		public CommitMetrics(Commit commit, SourceCodeFileChange file) {
			this.commit = commit;
			this.file = file;
		}
		
		private int intersect(Set<Integer> one, Set<Integer> two) {
			Set<Integer> intersect  = new HashSet<Integer>(one);
			intersect.retainAll(two);
			return intersect.size();
		}
		
		@Override
		public String toString() {
			String s = "";
			s += "Project:       " + commit.projectID + "\n";
			s += "Commit:        " + commit.repairedCommitID + "\n";
			s += "File:          " + file.repairedFile + "\n";
			s += "Runtime (ms):  " + file.analysisRuntime + "\n";
			s += "---------------\n";
			s += "Total Lines:   " + totLin + "\n";
			s += "---------------\n";
			s += "Datadep Def:   " + datDef + "\n";
			s += "Datadep Use:   " + datUse + "\n";
			s += "Datadep Lines: " + datLin.size() + "\n";
			s += "Datadep Ovrlp: " + intersect(insLin, datLin) + "\n";
			s += "---------------\n";
			s += "Condep Def:    " + cntDef + "\n";
			s += "Condep Use:    " + cntUse + "\n";
			s += "Condep Lines:  " + cntLin.size() + "\n";
			s += "Condep Ovrlp:  " + intersect(insLin, cntLin) + "\n";
			s += "---------------\n";
			s += "Var Def:       " + varDef + "\n";
			s += "Var Use:       " + varUse + "\n";
			s += "Var Lins:      " + varLin.size() + "\n";
			s += "Var Ovrlp:     " + intersect(insLin, varLin) + "\n";
			s += "---------------\n";
			s += "Call Def:      " + calDef + "\n";
			s += "Call Use:      " + calUse + "\n";
			s += "Call Lines:    " + calLin.size() + "\n";
			s += "Call Overlap:  " + intersect(insLin, calLin) + "\n";
			s += "---------------\n";
			s += "Cond Def:      " + cndDef + "\n";
			s += "Cond Use:      " + cndUse + "\n";
			s += "Cond Lines:    " + cndLin.size() + "\n";
			s += "Cond Overlap:  " + intersect(insLin, cndLin) + "\n";
			s += "---------------\n";
			s += "Value Def:     " + valDef + "\n";
			s += "Value Use:     " + valUse + "\n";
			s += "Value Lines:   " + valLin.size() + "\n";
			s += "Value Overlap: " + intersect(insLin, valLin) + "\n";
			return s;
		}

		public String toRow() {
			String s = "'" + commit.projectID + "'";
			s += ", '" + commit.repairedCommitID + "'";
			s += ", '" + file.repairedFile + "'";
			s += ", " + file.analysisRuntime;
			s += ", " + totLin;
			s += ", " + datDef;
			s += ", " + datUse;
			s += ", " + datLin.size();
			s += ", " + intersect(insLin, datLin);
			s += ", " + cntDef;
			s += ", " + cntUse;
			s += ", " + cntLin.size();
			s += ", " + intersect(insLin, cntLin);
			s += ", " + varDef;
			s += ", " + varUse;
			s += ", " + varLin.size();
			s += ", " + intersect(insLin, varLin);
			s += ", " + calDef;
			s += ", " + calUse;
			s += ", " + calLin.size();
			s += ", " + intersect(insLin, calLin);
			s += ", " + cndDef;
			s += ", " + cndUse;
			s += ", " + cndLin.size();
			s += ", " + intersect(insLin, cndLin);
			s += ", " + valDef;
			s += ", " + valUse;
			s += ", " + valLin.size();
			s += ", " + intersect(insLin, valLin);
			return s + "\n";
		}
	}
	
}
