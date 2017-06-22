package commitminer.js.diff.environment;

import java.util.Map;

import org.deri.iris.api.basics.IPredicate;
import org.deri.iris.api.basics.IQuery;
import org.deri.iris.compiler.Parser;
import org.deri.iris.compiler.ParserException;
import org.deri.iris.storage.IRelation;

import commitminer.analysis.SourceCodeFileChange;
import commitminer.analysis.annotation.DependencyIdentifier;
import commitminer.classify.ClassifierFeatureVector;
import commitminer.classify.Transformer;
import commitminer.factbase.Annotation;
import commitminer.js.annotation.AnnotationFactBase;

/**
 * Registers facts related to navigating the criterion and dependencies of the
 * abstract slice.
 */
public class EnvFactBase extends AnnotationFactBase {
	
	

	protected EnvFactBase(Map<IPredicate, IRelation> facts,
			SourceCodeFileChange sourceCodeFileChange) {
		super(facts, sourceCodeFileChange);
	}
	
	public void registerVariableChangeCriterion(DependencyIdentifier address, Annotation annotation) {
		registerAnnotationFact("variableChangeCriterion", address, annotation);
	}

	public void registerVariableChangeDependency(DependencyIdentifier address, Annotation annotation) {
		registerAnnotationFact("variableChangeDependency", address, annotation);
	}

	private static IQuery getEnvQuery() throws ParserException {

		String qs = "";
		qs += "?- variableChangeCriterion(?Version,?File,?Line,?Position,?Length,?Identifier).";

		/* The query that produces the results. */
		Parser parser = new Parser();
		parser.parse(qs);
		IQuery query = parser.getQueries().get(0);

		return query;

	}
	
	public static Transformer getEnvTransformer() {

		/* Transforms the query results to a ClassifierFeatureVector. */
		Transformer transformer = (commit, tuple) -> {
				return new ClassifierFeatureVector(commit,
						tuple.get(0).toString().replace("\'", ""),			// Version
						tuple.get(1).toString().replace("\'", ""), 			// Class
						tuple.get(5).toString().replace("\'",  ""),			// AST Node ID
						tuple.get(2).toString().replace("\'", ""),			// Line
						tuple.get(3).toString().replace("\'", ""),			// Position
						tuple.get(4).toString().replace("\'", ""),			// Length
						"DIFF",												// Type
						"ENV",												// Subtype
						tuple.get(6).toString().replace("\'", "")
							+ "_" + tuple.get(7).toString().replace("\'", "")
							+ "_" + tuple.get(8).toString().replace("\'", ""));	// Description
		};
		
		return transformer;
		
	}

}
