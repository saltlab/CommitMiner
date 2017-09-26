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
	
	private void processGumtreeAnnotations(Commit commit, SourceCodeFileChange file, AnnotationFactBase factBase, CommitMetrics metrics) throws IOException {

		/* Convenience */
		String code = file.repairedCode;
		SortedSet<Annotation> annotations = factBase.getAnnotations();
		
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
		
	}
	
	public void process(Commit commit, SourceCodeFileChange file, 
						AnnotationFactBase gumTreeFactBase,
						AnnotationFactBase meyersFactBase) throws IOException {

		CommitMetrics metrics = new CommitMetrics(commit, file);

		SortedSet<Annotation> gumTreeAnnotations = gumTreeFactBase.getAnnotations();
		SortedSet<Annotation> meyersAnnotations = meyersFactBase.getAnnotations();
		
		/* Find false negatives in Meyers diff. */
		for(Annotation annotation : gumTreeAnnotations) {
			if(!meyersAnnotations.contains(annotation))
				incrementFPN(metrics, annotation, true);
		}
		
		/* Find false positives in Meyers diff. */
		for(Annotation annotation : meyersAnnotations) {
			if(!gumTreeAnnotations.contains(annotation))
				incrementFPN(metrics, annotation, false);
		}
		
		processGumtreeAnnotations(commit, file, gumTreeFactBase, metrics);

		writeRow(metrics);
		System.out.println(metrics.toString());
		
	}

	public void incrementFPN(CommitMetrics metrics, Annotation annotation, boolean fn) throws IOException {
		
		switch(annotation.label) {
		case "CONDEP-DEF":
			if(fn) metrics.cntDefFN++;
			else metrics.cntDefFP++;
			break;
		case "CONDEP-USE":
			if(fn) metrics.cntUseFN++;
			else metrics.cntUseFP++;
			break;
		case "DATDEP-XDEF":
			if(fn) metrics.datDefFN++;
			else metrics.datDefFP++;
			break;
		case "DATDEP-USE":
			if(fn) metrics.datUseFN++;
			else metrics.datUseFP++;
			break;
		case "ENV-DEF":
			if(fn) metrics.varDefFN++;
			else metrics.varDefFP++;
			break;
		case "ENV-USE":
			if(fn) metrics.varUseFN++;
			else metrics.varUseFP++;
			break;
		case "CON-DEF":
			if(fn) metrics.cndDefFN++;
			else metrics.cndDefFP++;
			break;
		case "CON-USE":
			if(fn) metrics.cndUseFN++;
			else metrics.cndUseFP++;
			break;
		case "CALL-DEF":
			if(fn) metrics.calDefFN++;
			else metrics.calDefFP++;
			break;
		case "CALL-USE":
			if(fn) metrics.calUseFN++;
			else metrics.calUseFP++;
			break;
		case "VAL-DEF":
			if(fn) metrics.valDefFN++;
			else metrics.valDefFP++;
			break;
		case "VAL-USE":
			if(fn) metrics.valUseFN++;
			else metrics.valUseFP++;
			break;
		}
		
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
		s += ", DataDepDefFN";
		s += ", DataDepDefFP";
		s += ", DataDepUseFN";
		s += ", DataDepUseFP";
		s += ", ControlDepDef";
		s += ", ControlDepUse";
		s += ", ControlDepLines";
		s += ", ControlDepOverlap";
		s += ", ControlDepDefFN";
		s += ", ControlDepDefFP";
		s += ", ControlDepUseFN";
		s += ", ControlDepUseFP";
		s += ", VariableDef";
		s += ", VariableUse";
		s += ", VariableLines";
		s += ", VariableOverlap";
		s += ", VariableDefFN";
		s += ", VariableDefFP";
		s += ", VariableUseFN";
		s += ", VariableUseFP";
		s += ", CallDef";
		s += ", CallUse";
		s += ", CallLines";
		s += ", CallOverlap";
		s += ", CallDefFN";
		s += ", CallDefFP";
		s += ", CallUseFN";
		s += ", CallUseFP";
		s += ", ConditionDef";
		s += ", ConditionUse";
		s += ", ConditionLines";
		s += ", ConditionOverlap";
		s += ", ConditionDefFN";
		s += ", ConditionDefFP";
		s += ", ConditionUseFN";
		s += ", ConditionUseFP";
		s += ", ValueDef";
		s += ", ValueUse";
		s += ", ValueLines";
		s += ", ValueOverlap";
		s += ", ValueDefFN";
		s += ", ValueDefFP";
		s += ", ValueUseFN";
		s += ", ValueUseFP";
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
		public int datDefFN = 0;
		public int datDefFP = 0;
		public int datUseFN = 0;
		public int datUseFP = 0;
		public Set<Integer> datLin = new HashSet<Integer>();
		public int cntDef = 0;
		public int cntUse = 0;
		public int cntDefFN = 0;
		public int cntDefFP = 0;
		public int cntUseFN = 0;
		public int cntUseFP = 0;
		public Set<Integer> cntLin = new HashSet<Integer>();
		public int varDef = 0;
		public int varUse = 0;
		public int varDefFN = 0;
		public int varDefFP = 0;
		public int varUseFN = 0;
		public int varUseFP = 0;
		public Set<Integer> varLin = new HashSet<Integer>();
		public int calDef = 0;
		public int calUse = 0;
		public int calDefFN = 0;
		public int calDefFP = 0;
		public int calUseFN = 0;
		public int calUseFP = 0;
		public Set<Integer> calLin = new HashSet<Integer>();
		public int cndDef = 0;
		public int cndUse = 0;
		public int cndDefFN = 0;
		public int cndDefFP = 0;
		public int cndUseFN = 0;
		public int cndUseFP = 0;
		public Set<Integer> cndLin = new HashSet<Integer>();
		public int valDef = 0;
		public int valUse = 0;
		public int valDefFN = 0;
		public int valDefFP = 0;
		public int valUseFN = 0;
		public int valUseFP = 0;
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
			s += "Datadep DefFN: " + datDefFN + "\n";
			s += "Datadep DefFN: " + datDefFP + "\n";
			s += "Datadep UseFN: " + datUseFN + "\n";
			s += "Datadep UseFN: " + datUseFP + "\n";
			s += "---------------\n";
			s += "Condep Def:    " + cntDef + "\n";
			s += "Condep Use:    " + cntUse + "\n";
			s += "Condep Lines:  " + cntLin.size() + "\n";
			s += "Condep Ovrlp:  " + intersect(insLin, cntLin) + "\n";
			s += "Condep DefFN:  " + cntDefFN + "\n";
			s += "Condep DefFP:  " + cntDefFP + "\n";
			s += "Condep UseFN:  " + cntUseFN + "\n";
			s += "Condep UseFP:  " + cntUseFP + "\n";
			s += "---------------\n";
			s += "Var Def:       " + varDef + "\n";
			s += "Var Use:       " + varUse + "\n";
			s += "Var Lins:      " + varLin.size() + "\n";
			s += "Var Ovrlp:     " + intersect(insLin, varLin) + "\n";
			s += "Var DefFN:     " + varDefFN + "\n";
			s += "Var DefFP:     " + varDefFP + "\n";
			s += "Var UseFN:     " + varUseFN + "\n";
			s += "Var UseFP:     " + varUseFP + "\n";
			s += "---------------\n";
			s += "Call Def:      " + calDef + "\n";
			s += "Call Use:      " + calUse + "\n";
			s += "Call Lines:    " + calLin.size() + "\n";
			s += "Call Overlap:  " + intersect(insLin, calLin) + "\n";
			s += "Call DefFN:    " + calDefFN + "\n";
			s += "Call DefFP:    " + calDefFP + "\n";
			s += "Call UseFN:    " + calUseFN + "\n";
			s += "Call UseFP:    " + calUseFP + "\n";
			s += "---------------\n";
			s += "Cond Def:      " + cndDef + "\n";
			s += "Cond Use:      " + cndUse + "\n";
			s += "Cond Lines:    " + cndLin.size() + "\n";
			s += "Cond Overlap:  " + intersect(insLin, cndLin) + "\n";
			s += "Cond DefFN:    " + cndDefFN + "\n";
			s += "Cond DefFP:    " + cndDefFP + "\n";
			s += "Cond UseFN:    " + cndUseFN + "\n";
			s += "Cond UseFP:    " + cndUseFP + "\n";
			s += "---------------\n";
			s += "Value Def:     " + valDef + "\n";
			s += "Value Use:     " + valUse + "\n";
			s += "Value Lines:   " + valLin.size() + "\n";
			s += "Value Overlap: " + intersect(insLin, valLin) + "\n";
			s += "Value DefFN:   " + valDefFN + "\n";
			s += "Value DefFP:   " + valDefFP + "\n";
			s += "Value UseFN:   " + valUseFN + "\n";
			s += "Value UseFP:   " + valUseFP + "\n";
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
			s += ", " + datDefFN;
			s += ", " + datDefFP;
			s += ", " + datUseFN;
			s += ", " + datUseFP;
			s += ", " + cntDef;
			s += ", " + cntUse;
			s += ", " + cntLin.size();
			s += ", " + intersect(insLin, cntLin);
			s += ", " + cntDefFN;
			s += ", " + cntDefFP;
			s += ", " + cntUseFN;
			s += ", " + cntUseFP;
			s += ", " + varDef;
			s += ", " + varUse;
			s += ", " + varLin.size();
			s += ", " + intersect(insLin, varLin);
			s += ", " + varDefFN;
			s += ", " + varDefFP;
			s += ", " + varUseFN;
			s += ", " + varUseFP;
			s += ", " + calDef;
			s += ", " + calUse;
			s += ", " + calLin.size();
			s += ", " + intersect(insLin, calLin);
			s += ", " + calDefFN;
			s += ", " + calDefFP;
			s += ", " + calUseFN;
			s += ", " + calUseFP;
			s += ", " + cndDef;
			s += ", " + cndUse;
			s += ", " + cndLin.size();
			s += ", " + intersect(insLin, cndLin);
			s += ", " + cndDefFN;
			s += ", " + cndDefFP;
			s += ", " + cndUseFN;
			s += ", " + cndUseFP;
			s += ", " + valDef;
			s += ", " + valUse;
			s += ", " + valLin.size();
			s += ", " + intersect(insLin, valLin);
			s += ", " + valDefFN;
			s += ", " + valDefFP;
			s += ", " + valUseFN;
			s += ", " + valUseFP;
			return s + "\n";
		}
	}
	
}
