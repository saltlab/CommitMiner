package ca.ubc.ece.salt.pangor.learn.api;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import ca.ubc.ece.salt.gumtree.ast.ClassifiedASTNode.ChangeType;

/**
 * Stores the use of a keyword in a source code file. This includes the context
 * under which it is used and the type of change that occurred on the keyword
 * from the source to the destination code.
 */
public class KeywordUse extends KeywordDefinition {

	/** The context under which the keyword is used. **/
	public KeywordContext context;

	/** How this keyword was modified from the source to the destination file. **/
	public ChangeType changeType;

	/**
	 * Stores the api as a string if the AbstractAPI object is not available
	 * (i.e., after de-serialization).
	 */
	public String apiPackage;

	/**
	 * To be used when investigating a single function.
	 * @param type
	 * @param context
	 * @param keyword The keyword string.
	 * @param changeType
	 * @param api The name of the package where the keyword resides.
	 */
	public KeywordUse(KeywordType type, KeywordContext context, String keyword,
			ChangeType changeType, String api) {
		super(type, keyword);

		this.context = context;
		this.changeType = changeType;
		this.apiPackage = api;
	}

	/**
	 * To be used when investigating a single function.
	 * @param type
	 * @param context
	 * @param keyword The keyword string.
	 * @param changeType
	 */
	public KeywordUse(KeywordType type, KeywordContext context, String keyword,
			ChangeType changeType) {
		super(type, keyword);

		this.context = context;
		this.changeType = changeType;
		this.apiPackage = "_unknownapi_";
	}

	/**
	 * To be used in the initial scan of the entire script for the purpose of
	 * building the API model for the class.
	 * @param type
	 * @param keyword The keyword string.
	 */
	public KeywordUse(KeywordType type, String keyword) {
		this(type, KeywordContext.UNKNOWN, keyword, ChangeType.UNKNOWN);
	}

	/**
	 *
	 * @param type
	 * @param context
	 * @param keyword The keyword string.
	 * @param changeType
	 * @param path The package as an AbstractAPI
	 */
	public KeywordUse(KeywordType type, KeywordContext context, String keyword, ChangeType changeType,
			AbstractAPI path) {
		this(type, context, keyword, changeType);
		setAPI(path);
	}

	/**
	 * Return the package name of the API this Keywords belongs to.
	 *
	 * @return the package name, "global", or null
	 */
	@Override
	public String getPackageName() {
		if (api != null)
			return api.getPackageName();

		return this.apiPackage;
	}

	@Override
	public boolean equals(Object obj) {

		if(obj instanceof KeywordUse) {
			KeywordUse that = (KeywordUse) obj;

			if(this.type == that.type && this.context == that.context &&
					this.keyword.equals(that.keyword) &&
					this.changeType == that.changeType) {
				return true;
			}
		}
		else if(obj instanceof KeywordDefinition) {
			KeywordDefinition that = (KeywordDefinition) obj;

			if(this.type == that.type && this.keyword.equals(that.keyword))
				return true;
		}
		return false;
	}

	@Override
	public int hashCode() {
		return (this.type.toString() + "_" + this.keyword).hashCode();
	}

	@Override
	public String toString() {

		if(this.type == KeywordType.PACKAGE) {
			/* Don't print the package twice if the keyword type is a package. */
			return "KEYWORD:" + this.type.toString() + ":" + this.context.toString() + ":" +
				   this.changeType.toString() + ":" + this.keyword;
		}
		else if(this.api != null) {
			return "KEYWORD:" + this.type.toString() + ":" + this.context.toString() + ":" +
				   this.changeType.toString() + ":" + this.api.getName() + ":" + this.keyword;
		}
		return "KEYWORD:" + this.type.toString() + ":" + this.context.toString() + ":" +
			   this.changeType.toString() + ":" + this.apiPackage + ":" + this.keyword;

	}

	/**
	 * The possible contexts for a keyword. Reads like "The keyword is being
	 * used in a ... ".
	 */
	public enum KeywordContext {
		UNKNOWN,
		CONDITION,
		EXPRESSION,
		ASSIGNMENT_LHS,
		ASSIGNMENT_RHS,
		REQUIRE,
		CLASS_DECLARATION,
		METHOD_DECLARATION,
		PARAMETER_DECLARATION,
		VARIABLE_DECLARATION,
		METHOD_CALL,
		ARGUMENT,
		EXCEPTION_CATCH,
		EVENT_REGISTER,
		EVENT_REMOVE,
		STATEMENT
	}

	/**
	 * Helper method to filter a list of keywords by ChangeType. It returns a
	 * new list with the filtered elements. Original list is kept untouched.
	 *
	 * @param keywordsMap list of keywords
	 * @param changeTypes types of keywords that should stay
	 * @return a new list with elements matching the filter.
	 */
	public static Map<KeywordUse, Integer> filterMapByChangeType(Map<KeywordUse, Integer> keywordsMap,
			ChangeType... changeTypes) {
		Map<KeywordUse, Integer> newList = new HashMap<KeywordUse, Integer>();

		for (Entry<KeywordUse, Integer> keywordEntry : keywordsMap.entrySet()) {
			if (Arrays.asList(changeTypes).contains(keywordEntry.getKey().changeType))
				newList.put(keywordEntry.getKey(), keywordEntry.getValue());
		}

		return newList;
	}
}