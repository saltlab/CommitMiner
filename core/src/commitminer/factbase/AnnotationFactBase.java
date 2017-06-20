package commitminer.factbase;

import java.util.Map;

import org.deri.iris.api.basics.IPredicate;
import org.deri.iris.storage.IRelation;
import org.deri.iris.api.basics.ITuple;
import org.deri.iris.factory.Factory;
import org.deri.iris.storage.IRelationFactory;
import org.deri.iris.storage.simple.SimpleRelationFactory;

import ca.ubc.ece.salt.gumtree.ast.ClassifiedASTNode.Version;
import commitminer.analysis.SourceCodeFileChange;
import commitminer.annotation.Annotation;
import commitminer.annotation.DependencyIdentifier;

/**
 * Registers and stores facts related to annotating the source code file.
 */
public class AnnotationFactBase extends FactBase {

	protected AnnotationFactBase(Map<IPredicate, IRelation> facts,
			SourceCodeFileChange sourceCodeFileChange) {
		super(facts, sourceCodeFileChange);
	}

	/**
	 * Register a fact that will be used as a source code annotation.
	 * @param predicateName The name of the fact.
	 * @param address The identifier which will link the dependency to its criterion.
	 * @param annotation Where to annotate the source file.
	 */
	protected void registerAnnotationFact(String predicateName, DependencyIdentifier address, Annotation annotation) {

		IPredicate predicate = Factory.BASIC.createPredicate(predicateName, 6);
		IRelation relation = facts.get(predicate);
		if(relation == null) {
			IRelationFactory relationFactory = new SimpleRelationFactory();
			relation = relationFactory.createRelation();
			facts.put(predicate, relation);
		}

		/* Add the new tuple to the relation. */
		ITuple tuple = Factory.BASIC.createTuple(
				Factory.TERM.createString(Version.DESTINATION.toString()),
				Factory.TERM.createString(sourceCodeFileChange.repairedFile),
				Factory.TERM.createString(annotation.line.toString()),
				Factory.TERM.createString(annotation.absolutePosition.toString()),
				Factory.TERM.createString(annotation.length.toString()),
				Factory.TERM.createString(address.getAddress()));
		relation.add(tuple);
		
	}
	
	/* Navigation facts. */
	
	public void registerFunctionDef(DependencyIdentifier address, Annotation annotation) {
		registerAnnotationFact("functionDef", address, annotation);
	}

	public void registerFunctionUse(DependencyIdentifier address, Annotation annotation) {
		registerAnnotationFact("functionUse", address, annotation);
	}
	
	/* Change slicing facts. */
	
	public void registerCallChangeCriterion(DependencyIdentifier address, Annotation annotation) {
		registerAnnotationFact("callChangeCriterion", address, annotation);
	}
	
	public void registerCallChangeDependency(DependencyIdentifier address, Annotation annotation) {
		registerAnnotationFact("callChangeDependency", address, annotation);
	}
	
	public void registerConditionChangeCriterion(DependencyIdentifier address, Annotation annotation) {
		registerAnnotationFact("conditionChangeCriterion", address, annotation);
	}

	public void registerConditionChangeDependency(DependencyIdentifier address, Annotation annotation) {
		registerAnnotationFact("conditionChangeDependency", address, annotation);
	}
	
	public void registerValueChangeCriterion(DependencyIdentifier address, Annotation annotation) {
		registerAnnotationFact("valueChangeCriterion", address, annotation);
	}

	public void registerValueChangeDependency(DependencyIdentifier address, Annotation annotation) {
		registerAnnotationFact("valueChangeDependency", address, annotation);
	}

	public void registerFunctionChangeCriterion(DependencyIdentifier address, Annotation annotation) {
		registerAnnotationFact("functionChangeCriterion", address, annotation);
	}

	public void registerFunctionChangeDependency(DependencyIdentifier address, Annotation annotation) {
		registerAnnotationFact("functionChangeDependency", address, annotation);
	}

	public void registerVariableChangeCriterion(DependencyIdentifier address, Annotation annotation) {
		registerAnnotationFact("variableChangeCriterion", address, annotation);
	}

	public void registerVariableChangeDependency(DependencyIdentifier address, Annotation annotation) {
		registerAnnotationFact("variableChangeDependency", address, annotation);
	}
	
}
