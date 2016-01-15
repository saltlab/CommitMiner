package ca.ubc.ece.salt.pangor.analysis;

import java.util.Map;

import org.deri.iris.api.basics.IPredicate;
import org.deri.iris.api.basics.ITuple;
import org.deri.iris.api.factory.IBasicFactory;
import org.deri.iris.api.terms.ITerm;
import org.deri.iris.factory.Factory;
import org.deri.iris.storage.IRelation;
import org.deri.iris.storage.IRelationFactory;
import org.deri.iris.storage.simple.SimpleRelationFactory;

/**
 * Utilities to help the analysis engines extract and store facts.
 */
public class Utilities {

	/**
	 * Stores a new fact in the fact database.
	 * @param predicateName The name of the predicate to create.
	 * @param terms The terms of the predicate.
	 */
	public static void addFact(Map<IPredicate, IRelation> facts,
								  String predicateName, ITerm... terms) {

		/* The factories we need to create the predicates. */
		IBasicFactory basicFactory = Factory.BASIC;

		/* Create the predicate for this analysis. */
		IPredicate predicate = basicFactory.createPredicate(predicateName, terms.length);

		/* Get the relation for this predicate from the fact base. */
		IRelation relation = facts.get(predicate);
		if(relation == null) {

			/* The predicate does not yet exist in the fact base. Create a
			 * relation for the predicate and add it to the fact base. */
			IRelationFactory relationFactory = new SimpleRelationFactory();
			relation = relationFactory.createRelation();
			facts.put(predicate, relation);

		}

		/* Add the new tuple to the relation. */
		ITuple tuple = basicFactory.createTuple(terms);
		relation.add(tuple);

	}

}