package ca.ubc.ece.salt.pangor.learn.analysis;

import ca.ubc.ece.salt.gumtree.ast.ClassifiedASTNode.ChangeType;
import ca.ubc.ece.salt.pangor.api.KeywordDefinition.KeywordType;
import ca.ubc.ece.salt.pangor.api.KeywordUse.KeywordContext;

public class KeywordFilter {

	public FilterType filterType;
	public KeywordType type;
	public KeywordContext context;
	public ChangeType changeType;
	public String pack;
	public String keyword;

	/** If true, includes or excludes patterns that do not match the pattern. */
	public boolean not;

	/**
	 * The default filter includes all keywords.
	 */
	public KeywordFilter() {
		this.filterType = FilterType.INCLUDE;
		this.type = KeywordType.UNKNOWN;
		this.context = KeywordContext.UNKNOWN;
		this.changeType = ChangeType.UNKNOWN;
		this.pack = "";
		this.keyword = "";
		this.not = false;
	}

	/**
	 * @param query Creates a KeywordFilter by parsing a query string into a
	 * 				tree.
	 */
	public KeywordFilter(String query) {

	}

	/**
	 * Create a new filter with all options.
	 * @param filterType
	 * @param type
	 * @param context
	 * @param changeType
	 * @param pack
	 * @param keyword
	 */
	public KeywordFilter(FilterType filterType, KeywordType type,
			KeywordContext context, ChangeType changeType, String pack,
			String keyword) {

		this.filterType = filterType;
		this.type = type;
		this.context = context;
		this.changeType = changeType;
		this.pack = pack;
		this.keyword = keyword;
		this.not = false;

	}

	/**
	 * Create a new filter with all options.
	 * @param filterType
	 * @param type
	 * @param context
	 * @param changeType
	 * @param pack
	 * @param keyword
	 * @param not
	 */
	public KeywordFilter(FilterType filterType, KeywordType type,
			KeywordContext context, ChangeType changeType, String pack,
			String keyword, boolean not) {

		this.filterType = filterType;
		this.type = type;
		this.context = context;
		this.changeType = changeType;
		this.pack = pack;
		this.keyword = keyword;
		this.not = not;

	}

	/**
	 * INCLUDE includes all rows that match the filter.
	 * EXCLUDE excludes all rows that match the filter.
	 */
	public enum FilterType {
		INCLUDE,
		EXCLUDE
	}

	/**
	 * Factory method for building a package filter
	 *
	 * @param packageName
	 * @return a KeywordFilter
	 */
	public static KeywordFilter buildPackageFilter(String packageName) {
		return new KeywordFilter(FilterType.INCLUDE, KeywordType.UNKNOWN, KeywordContext.UNKNOWN, ChangeType.UNKNOWN,
				packageName, "");
	}

	/**
	 * A node in the query's AST.
	 */
	private class QueryNode {

	}

}