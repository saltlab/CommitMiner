package commitminer.js.annotation;

import java.util.Comparator;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import commitminer.analysis.SourceCodeFileChange;
import commitminer.factbase.FactBase;

/**
 * Registers and stores facts related to annotating the source code file.
 * 
 * An analysis should extend this class to register concrete predicates that 
 * will be used to annotate the source code file (e.g., NavigationFactBase).
 */
public class AnnotationFactBase extends FactBase {

	private SortedSet<Annotation> annotations;
	
	public AnnotationFactBase(SourceCodeFileChange sourceCodeFileChange) {

		super(sourceCodeFileChange);

		/* Order the annotations according to their absolute position in the
		 * file. */
		annotations = new TreeSet<Annotation>(new Comparator<Annotation>() {
			public int compare(Annotation a1, Annotation a2) {
				if(a1.absolutePosition < a2.absolutePosition) return -1;
				if(a1.absolutePosition > a2.absolutePosition) return 1;
				return 0;
			}
		});

	}

	/**
	 * Register an annotation with the fact database.
	 * 
	 * The annotation is assumed to be on the destination file.
	 */
	public void registerAnnotationFact(Annotation annotation) {
		this.annotations.add(annotation);
	}
	
	/**
	 * Register an annotation with the fact database.
	 * 
	 * The annotation is assumed to be on the destination file.
	 */
	public void registerAnnotationFacts(Set<Annotation> annotations) {
		this.annotations.addAll(annotations);
	}
	
	/**
	 * @return The ordered set of annotations in the fact base.
	 */
	public SortedSet<Annotation> getAnnotations() {
		return this.annotations;
	}
	
	/* Def/use facts. */
	
//	public void registerDef(DependencyIdentifier address, Annotation annotation) {
//		registerAnnotationFact("def", address, annotation);
//	}
//
//	public void registerUse(DependencyIdentifier address, Annotation annotation) {
//		registerAnnotationFact("use", address, annotation);
//	}
	
	/* Change slicing facts. */
	
//	public void registerCallChangeCriterion(DependencyIdentifier address, Annotation annotation) {
//		registerAnnotationFact("callChangeCriterion", address, annotation);
//	}
//	
//	public void registerCallChangeDependency(DependencyIdentifier address, Annotation annotation) {
//		registerAnnotationFact("callChangeDependency", address, annotation);
//	}
//	
//	public void registerConditionChangeCriterion(DependencyIdentifier address, Annotation annotation) {
//		registerAnnotationFact("conditionChangeCriterion", address, annotation);
//	}
//
//	public void registerConditionChangeDependency(DependencyIdentifier address, Annotation annotation) {
//		registerAnnotationFact("conditionChangeDependency", address, annotation);
//	}
//	
//	public void registerValueChangeCriterion(DependencyIdentifier address, Annotation annotation) {
//		registerAnnotationFact("valueChangeCriterion", address, annotation);
//	}
//
//	public void registerValueChangeDependency(DependencyIdentifier address, Annotation annotation) {
//		registerAnnotationFact("valueChangeDependency", address, annotation);
//	}
//
//	public void registerFunctionChangeCriterion(DependencyIdentifier address, Annotation annotation) {
//		registerAnnotationFact("functionChangeCriterion", address, annotation);
//	}
//
//	public void registerFunctionChangeDependency(DependencyIdentifier address, Annotation annotation) {
//		registerAnnotationFact("functionChangeDependency", address, annotation);
//	}

}
