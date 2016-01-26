package ca.ubc.ece.salt.pangor.learn.analysis;

import java.util.Comparator;
import java.util.TreeSet;

import ca.ubc.ece.salt.pangor.api.KeywordUse;

/**
 * Stores a set of metrics for a learning data set.
 */
public class LearningMetrics {

	/** ChangeType == INSERTED, REMOVED or UPDATED **/
	public TreeSet<KeywordFrequency> changedKeywordFrequency;

	/** Includes all change types. **/
	public TreeSet<KeywordFrequency> keywordFrequency; // Includes all change types.

	public LearningMetrics() {

		/* Keyword frequencies are sorted by the number or rows they occur in. */
		Comparator<KeywordFrequency> comparator = new Comparator<KeywordFrequency>() {

			@Override
			public int compare(KeywordFrequency o1, KeywordFrequency o2) {

				if(o1.frequency > o2.frequency) return -1;
				else if(o1.frequency < o2.frequency) return 1;

				return o1.keyword.toString().compareTo(o2.keyword.toString());

			}

		};

		this.changedKeywordFrequency = new TreeSet<KeywordFrequency>(comparator);
		this.keywordFrequency = new TreeSet<KeywordFrequency>(comparator);

	}

	/**
	 * Adds a keyword frequency to the ordered set.
	 * @param keyword The keyword.
	 * @param frequency The number of rows the keyword appeared in the data set.
	 */
	public void addKeywordFrequency(KeywordUse keyword, int frequency) {
		switch(keyword.changeType) {
		case INSERTED:
		case REMOVED:
		case UPDATED:
			this.changedKeywordFrequency.add(new KeywordFrequency(keyword, frequency));
		default:
			this.keywordFrequency.add(new KeywordFrequency(keyword, frequency));
		}
	}

	/**
	 * Keeps track of the number of rows in which the keyword appears.
	 */
	public class KeywordFrequency {
		public KeywordUse keyword;
		public int frequency;

		public KeywordFrequency(KeywordUse keyword, int frequency) {
			this.keyword = keyword;
			this.frequency = frequency;
		}
	}

}