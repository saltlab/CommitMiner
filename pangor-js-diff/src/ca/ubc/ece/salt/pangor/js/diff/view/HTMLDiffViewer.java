package ca.ubc.ece.salt.pangor.js.diff.view;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import ca.ubc.ece.salt.pangor.classify.analysis.ClassifierFeatureVector;

/**
 * Prints source code as annotated HTML.
 */
public class HTMLDiffViewer {

	/**
	 * Creates an html file annotated with alerts.
	 * @param inputPath The original source code file (text)
	 * @param outputPath The path to write the annotated html file
	 * @param alerts The alerts used to annotate the file
	 * @throws IOException when files cannot be read or written
	 */
	public static void annotate(String inputPath,
						  String outputPath,
						  List<ClassifierFeatureVector> alerts,
						  String version) throws IOException {

		String source = new String(Files.readAllBytes(Paths.get(inputPath)));

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

		for(ClassifierFeatureVector fv : filtered) {
			System.out.println(fv.subtype + "-" + fv.absolutePosition + "-" + fv.length);
		}

		try(BufferedWriter out = new BufferedWriter(new FileWriter(outputPath))) {

			ClassifierFeatureVector current = null;

			if(!filtered.isEmpty()) current = filtered.pop();

			char[] chars = source.toCharArray();

			Map<Integer,Integer> closeAt = new HashMap<Integer,Integer>();

			for(int i = 0; i < chars.length; i++) {

				/* Close tags where needed. */
				Integer sem = closeAt.get(i);
				for(int j = 0; sem != null && j < sem; j++) {
					out.write("</span>");
				}
				closeAt.remove(i);

				/* Open tags where needed. */
				while(current != null && Integer.parseInt(current.absolutePosition) == i) {

					/* Open the tag. */
					out.write("<span class='" + current.subtype + "-tag'>");

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
				out.write(chars[i]);

			}

		}

	}

}
