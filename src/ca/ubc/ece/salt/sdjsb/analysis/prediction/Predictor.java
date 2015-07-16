package ca.ubc.ece.salt.sdjsb.analysis.prediction;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import ca.ubc.ece.salt.sdjsb.analysis.learning.apis.Keyword;
import ca.ubc.ece.salt.sdjsb.analysis.learning.apis.Keyword.KeywordType;
import ca.ubc.ece.salt.sdjsb.analysis.learning.apis.TopLevelAPI;

/**
 * Abstract class to model a Predictor
 */
public abstract class Predictor {
	/** The top level API where API's are looked for */
	protected TopLevelAPI api;

	protected Map<Keyword, Integer> insertedKeywords;
	protected Map<Keyword, Integer> removedKeywords;
	protected Map<Keyword, Integer> updatedKeywords;
	protected Map<Keyword, Integer> unchangedKeywords;

	/**
	 * The "required name" of packages that were actually imported on the file
	 * and will serve as a filter on our predictions
	 */
	protected Set<String> requiredAPIsNames;

	public Predictor(TopLevelAPI api, Map<Keyword, Integer> insertedKeywords, Map<Keyword, Integer> removedKeywords,
			Map<Keyword, Integer> updatedKeywords, Map<Keyword, Integer> unchangedKeywords) {
		this.api = api;
		this.insertedKeywords = insertedKeywords;
		this.removedKeywords = removedKeywords;
		this.updatedKeywords = updatedKeywords;
		this.unchangedKeywords = unchangedKeywords;

		/*
		 * Look on input for PACKAGEs keywords.
		 */

		requiredAPIsNames = lookupRequiredAPIs(insertedKeywords, unchangedKeywords);
	}

	public abstract PredictionResults predictKeyword(Keyword keyword);

	/**
	 * Internal method to look for KeywordType.PACKAGE keywords on the input
	 */
	protected Set<String> lookupRequiredAPIs(Map<Keyword, Integer>... keywordsMaps) {
		Set<String> outputSet = new HashSet<>();

		/*
		 * we assume "global" is always imported
		 */
		outputSet.add("global");

		/*
		 * If no input is given, just return empty list
		 */
		if (keywordsMaps.length == 0)
			return outputSet;

		for (Map<Keyword, Integer> keywordsMap : keywordsMaps) {
			/*
			 * If null map is given, skip it
			 */
			if (keywordsMap == null)
				continue;

			for (Keyword keyword : keywordsMap.keySet()) {
				if (keyword.type.equals(KeywordType.PACKAGE))
					outputSet.add(keyword.api.getName());
			}
		}

		return outputSet;
	}
}
