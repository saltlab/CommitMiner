package ca.ubc.ece.salt.pangor.java.test.ast;

import java.util.List;

import org.junit.Test;

import ca.ubc.ece.salt.pangor.analysis.Commit;
import ca.ubc.ece.salt.pangor.classify.alert.ClassifierAlert;

public class TestASTDifferencing extends TestAnalysis {

	private final Commit AMI = new Commit(0, 0, "test", "homepage", "src file", "dst file", "src commit", "dst commit", "src code", "dst code");

	private void runTest(String[] args, List<ClassifierAlert> expectedAlerts, boolean printAlerts) throws Exception {
//		ClassifierDataSet dataSet = new ClassifierDataSet(null, null);
//		RenameMethodAnalysis analysis = new RenameMethodAnalysis(dataSet, AMI);
//		super.runTest(args, expectedAlerts, printAlerts, analysis, dataSet);
	}

	@Test
	public void testSimple() throws Exception{
//		String src = "./test/input/promises/simple.js";
//		String dst = "./test/input/promises/simple-human.js";
//		List<ClassifierAlert> expectedAlerts = new LinkedList<ClassifierAlert>();
//		expectedAlerts.add(new RenameMethodAlert(AMI, "UNKNOWN", "REF", "PROM"));
//		this.runTest(new String[] {src, dst}, expectedAlerts, false);
	}

}