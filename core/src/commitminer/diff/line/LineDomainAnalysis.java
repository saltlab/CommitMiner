package commitminer.diff.line;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.deri.iris.api.basics.IPredicate;
import org.deri.iris.storage.IRelation;

import commitminer.analysis.DomainAnalysis;
import commitminer.analysis.SourceCodeFileChange;
import commitminer.analysis.annotation.Annotation;
import commitminer.analysis.annotation.AnnotationFactBase;
import commitminer.analysis.annotation.DependencyIdentifier;

/**
 * An analysis of a JavaScript file for extracting line-level-diff change facts.
 *
 * Uses the Myers diff algorithm.
 */
public class LineDomainAnalysis extends DomainAnalysis {

	public LineDomainAnalysis() {
		super(null, null, null, false);
	}

	@Override
	protected void analyzeFile(SourceCodeFileChange sourceCodeFileChange,
							   Map<IPredicate, IRelation> facts) throws Exception {
		
		/* Get the annotation database. */
		AnnotationFactBase factBase = AnnotationFactBase.getInstance(sourceCodeFileChange);
		
		String[] srcLines = sourceCodeFileChange.buggyCode.split("\n");
		String[] dstLines = sourceCodeFileChange.repairedCode.split("\n");

		DiffMatchPatch dmp = new DiffMatchPatch();

		LinkedList<DiffMatchPatch.Diff> diffs;
		diffs = dmp.diff_main_line_mode(sourceCodeFileChange.buggyCode,
							  			sourceCodeFileChange.repairedCode);

		int i = 0; // Track the line number in the source file.
		int j = 0; // Track the line number in the destination file.
		
		int srcAbs = 0; // Track the absolute position in the source file.
		int dstAbs = 0;	// Track the absolute position in the destination file.

		/* For convenience. Since there are no program elements in line-diff, we don't
		* need to track any IDs. */
		List<DependencyIdentifier> ids = new LinkedList<DependencyIdentifier>();

		for (DiffMatchPatch.Diff diff : diffs) {
		  for (int y = 0; y < diff.text.length(); y++) {
			  Annotation annotation;
			  switch(diff.operation) {
			  case EQUAL:
				  annotation = new Annotation("LINE-UNCHANGED", ids, j,
											  dstAbs, dstLines[j].length());
//				  factBase.registerAnnotationFact(annotation);
				  srcAbs += srcLines[i].length() + 1;
				  dstAbs += dstLines[j].length() + 1;
				  i++;
				  j++;
				  break;
			  case DELETE:
				  srcAbs += srcLines[i].length() + 1;
				  i++;
				  break;
			  case INSERT:
				  annotation = new Annotation("LINE-INSERTED", ids, j,
											  dstAbs, dstLines[j].length());
				  factBase.registerAnnotationFact(annotation);
				  dstAbs += dstLines[j].length() + 1;
				  j++;
				  break;
			  }
		  }
		}

	}

}
