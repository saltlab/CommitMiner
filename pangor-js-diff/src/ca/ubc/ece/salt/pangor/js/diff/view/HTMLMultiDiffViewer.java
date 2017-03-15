package ca.ubc.ece.salt.pangor.js.diff.view;

import java.io.IOException;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import ca.ubc.ece.salt.pangor.classify.analysis.ClassifierFeatureVector;

/**
 * Prints source code as annotated HTML.
 */
public class HTMLMultiDiffViewer {

	/**
	 * Adds html alert annotations to the file.
	 * @param inputPath The original source code file (text)
	 * @param alerts The alerts used to annotate the file
	 * @return The annotated file
	 * @throws IOException when files cannot be read or written
	 */
	public static String annotate(String source,
						  List<ClassifierFeatureVector> alerts,
						  String version) throws IOException {

		LinkedList<ClassifierFeatureVector> filtered = new LinkedList<ClassifierFeatureVector>();
		for(ClassifierFeatureVector alert : alerts) {
			if(alert.version.equals(version)) {
				filtered.add(alert);
			}
		}

		Collections.sort(filtered, new Comparator<ClassifierFeatureVector>() {

			@Override
			public int compare(ClassifierFeatureVector o1,
					ClassifierFeatureVector o2) {

				/* Annotations are ordered by when they appear in the file */

				Integer i1 = Integer.parseInt(o1.absolutePosition);
				Integer i2 = Integer.parseInt(o2.absolutePosition);

				if(!i1.equals(i2)) return i1.compareTo(i2);

				/* Since these annotations start at the same spot, we need
				 * the longer annotation to be printed first. */

				i1 = Integer.parseInt(o1.length);
				i2 = Integer.parseInt(o2.length);

				return i2.compareTo(i1);

			}

		});

		String out = "";

		ClassifierFeatureVector current = null;

		if(!filtered.isEmpty()) current = filtered.pop();

		char[] chars = source.toCharArray();

		/* Track when to close tags key=position value=semaphore. */
		Map<Integer,Integer> closeAt = new HashMap<Integer,Integer>();

		/* Track which tags are currently open. */
		LinkedList<ClassifierFeatureVector> openTags = new LinkedList<ClassifierFeatureVector>();

		for(int i = 0; i < chars.length; i++) {

			/* Close tags where needed. */
			Integer sem = closeAt.get(i);
			for(int j = 0; sem != null && j < sem; j++) {
				out = out.concat("</span>");
				openTags.pop();
			}
			closeAt.remove(i);

			/* Re-open all closed tags after a line break. */
			if(i > 0 && chars[i-1] == '\n') {
				Iterator<ClassifierFeatureVector> it = openTags.descendingIterator();
				while(it.hasNext()) {
					ClassifierFeatureVector openTag = it.next();
					out = out.concat("<span class='" + openTag.subtype + "-tag' data-address='" + openTag.line + "'>");
				}
			}

			/* Close all tags before a line break. */
			for(int k = 0; chars[i] == '\n' && k < openTags.size(); k++)
				out = out.concat("</span>");

			/* Open tags where needed. */
			while(current != null && Integer.parseInt(current.absolutePosition) == i) {

				/* Open the tag. */
				out = out.concat("<span class='" + current.subtype + "-tag' data-address='" + current.line + "'>");
				openTags.push(current);

				/* Set the close tag position. */
				Integer closePosition = Integer.parseInt(current.length) + i;
				Integer count = closeAt.get(closePosition);
				if(count == null)
					closeAt.put(closePosition, 1);
				else
					closeAt.put(closePosition, count + 1);

				current = filtered.isEmpty() ? null : filtered.pop();
			}

			/* Write the next character in the file. */
			out = out.concat(String.valueOf(chars[i]));

		}

		return out;

	}

}
