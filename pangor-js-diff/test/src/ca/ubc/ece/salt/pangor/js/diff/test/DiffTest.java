package ca.ubc.ece.salt.pangor.js.diff.test;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.junit.Test;

import difflib.Delta;
import difflib.DiffRow;
import difflib.DiffRow.Tag;
import difflib.DiffRowGenerator;
import difflib.DiffUtils;
import difflib.Patch;


public class DiffTest {

	@Test
	public void parseDiffToSideBySideView() throws IOException {
//		List<String> lines = IOUtils.readLines(FileUtils.openInputStream(new File("httpd.conf.diff")));

		List<String> src = IOUtils.readLines(FileUtils.openInputStream(new File("/Users/qhanam/Documents/workspace_commitminer/pangor/pangor-js-diff/test/input/diff/dyn_old.js")));
		List<String> dst = IOUtils.readLines(FileUtils.openInputStream(new File("/Users/qhanam/Documents/workspace_commitminer/pangor/pangor-js-diff/test/input/diff/dyn_new.js")));

		Patch<String> diff = DiffUtils.diff(src, dst);

		DiffRowGenerator.Builder builder = new DiffRowGenerator.Builder();
		builder.showInlineDiffs(false);
		DiffRowGenerator generator = builder.build();
		SideBySideView view = new SideBySideView();
		for (Object obj : diff.getDeltas()) {
			@SuppressWarnings("unchecked")
			Delta<String> delta = (Delta<String>) obj;

			List<DiffRow> generateDiffRows = generator.generateDiffRows(
					delta.getOriginal().getLines(),
					delta.getRevised().getLines()
					);
			int leftPos = delta.getOriginal().getPosition();
			int rightPos = delta.getRevised().getPosition();
			for (DiffRow row : generateDiffRows) {
				SideBySideView.Line line = new SideBySideView.Line();
				Tag tag = row.getTag();
				if (tag == Tag.INSERT) {
					line.left.cssClass = "old";
					line.right.number = rightPos;
					line.right.text = row.getNewLine();
					line.right.cssClass = "new";
					rightPos++;
				} else if (tag == Tag.CHANGE) {
					line.left.number = leftPos;
					line.left.text = row.getOldLine();
					line.left.cssClass = "old";
					leftPos++;
					line.right.number = rightPos;
					line.right.text = row.getNewLine();
					line.right.cssClass = "new";
					rightPos++;
				} else if (tag == Tag.DELETE) {
					line.left.number = leftPos;
					line.left.text = row.getOldLine();
					line.left.cssClass = "old";
					leftPos++;
					line.right.cssClass = "new";
				} else if (tag == Tag.EQUAL) {
					line.left.number = leftPos;
					line.left.text = row.getOldLine();
					leftPos++;
					line.right.number = rightPos;
					line.right.text = row.getNewLine();
					rightPos++;
				} else {
					throw new IllegalStateException("Unknown pattern tag: " + tag);
				}
				view.addLine(line);
			}
		}
		for (SideBySideView.Line line : view.lines) {
			System.out.printf("<left class=\"%s\"><num>%s</num><text>%s</text></left>" +
					"<right class=\"%s\"><num>%s</num><text>%s</text></right>\n",
					line.left.cssClass,
					line.left.getNumber(),
					line.left.text,
					line.right.cssClass,
					line.right.getNumber(),
					line.right.text);
		}
	}

	public static class SideBySideView {
		private List<Line> lines = new ArrayList<Line>();
		public void addLine(Line line) {
			lines.add(line);
		}
		public static class Line {
			private Item left = new Item();
			private Item right = new Item();
			public static class Item {
				private Integer number;
				private String text = "";
				private String cssClass = "";
				public String getNumber() {
					return (number == null) ? "" : String.valueOf(number);
				}
			}
		}
	}

}
