package ca.ubc.ece.salt.pangor.java.test.analysis;

import java.util.LinkedList;
import java.util.List;

import org.deri.iris.api.basics.ILiteral;
import org.deri.iris.api.basics.IPredicate;
import org.deri.iris.api.basics.IQuery;
import org.deri.iris.api.basics.IRule;
import org.deri.iris.api.basics.ITuple;
import org.deri.iris.api.terms.ITerm;
import org.deri.iris.factory.Factory;
import org.junit.Test;

import ca.ubc.ece.salt.pangor.analysis.Commit;
import ca.ubc.ece.salt.pangor.analysis.CommitAnalysis;
import ca.ubc.ece.salt.pangor.analysis.DomainAnalysis;
import ca.ubc.ece.salt.pangor.analysis.SourceCodeFileAnalysis;
import ca.ubc.ece.salt.pangor.analysis.SourceCodeFileChange;
import ca.ubc.ece.salt.pangor.analysis.simple.SimpleAlert;
import ca.ubc.ece.salt.pangor.analysis.simple.SimpleDataSet;
import ca.ubc.ece.salt.pangor.cfg.CFGFactory;
import ca.ubc.ece.salt.pangor.java.analysis.ClassAnalysis;
import ca.ubc.ece.salt.pangor.java.analysis.MethodAnalysis;
import ca.ubc.ece.salt.pangor.java.analysis.simple.SimpleMethodDstAnalysis;
import ca.ubc.ece.salt.pangor.java.analysis.simple.SimpleMethodSrcAnalysis;
import ca.ubc.ece.salt.pangor.java.cfg.JavaCFGFactory;

public class TestRenameRefactoring extends TestAnalysis{


	private void runTest(Commit commit, SourceCodeFileChange sourceFileChange,
			List<SimpleAlert> expectedAlerts,
			boolean printAlerts) throws Exception {

		/* Build the CFG with the JDT Java CFG factory. */
		CFGFactory cfgFactory = new JavaCFGFactory();

		/* Set up the data set (stores alerts aka feature vectors). */
		SimpleDataSet dataSet = new SimpleDataSet(getRules(), getQueries());

		/* Set up the source and destination file analyses. */
		List<MethodAnalysis> srcMethodAnalyses = new LinkedList<MethodAnalysis>();
		srcMethodAnalyses.add(new SimpleMethodSrcAnalysis());
		SourceCodeFileAnalysis srcAnalysis = new ClassAnalysis(srcMethodAnalyses);

		List<MethodAnalysis> dstMethodAnalyses = new LinkedList<MethodAnalysis>();
		dstMethodAnalyses.add(new SimpleMethodDstAnalysis());
		SourceCodeFileAnalysis dstAnalysis = new ClassAnalysis(dstMethodAnalyses);

		/* Set up the domain analysis. */
		DomainAnalysis domainAnalysis = new DomainAnalysis(srcAnalysis,
														   dstAnalysis,
														   cfgFactory, false);

		List<DomainAnalysis> domainAnalyses = new LinkedList<DomainAnalysis>();
		domainAnalyses.add(domainAnalysis);

		/* Set up the commit analysis. */
		CommitAnalysis commitAnalysis;
		commitAnalysis = new CommitAnalysis(dataSet, domainAnalyses);

		/* Run the analysis. */
		this.runTest(commit, sourceFileChange, expectedAlerts, printAlerts, commitAnalysis, dataSet);

	}

	/**
	 * @return The list of queries for the test.
	 */
	private static List<IQuery> getQueries() {

		ITerm termX = Factory.TERM.createVariable("F"); // F[ile]
		ITerm termY = Factory.TERM.createVariable("S"); // S[ource method name]
		ITerm termZ = Factory.TERM.createVariable("D"); // D[estination method name]
		ITuple tuple = Factory.BASIC.createTuple(termX, termY, termZ); // X,Y,Z

		IPredicate sourcePredicate = Factory.BASIC.createPredicate("MethodRename", 3); // MethodRename(,)
		ILiteral methodRename = Factory.BASIC.createLiteral(true, sourcePredicate, tuple); // MethodRename(F,M)
		IQuery query = Factory.BASIC.createQuery(methodRename); // ?- methodRename(F,M).

		List<IQuery> queries = new LinkedList<IQuery>();
		queries.add(query);

		return queries;

	}

	/**
	 * @return The list of rules for the test.
	 */
	private static List<IRule> getRules() {

		List<ILiteral> head = new LinkedList<ILiteral>();
		List<ILiteral> body = new LinkedList<ILiteral>();

		ITerm termX = Factory.TERM.createVariable("F"); // F[ile]
		ITerm termY = Factory.TERM.createVariable("S"); // S[ource method name]
		ITerm termZ = Factory.TERM.createVariable("D"); // D[estination method name]

		/* Create literal `MethodRename(F,S,D)` */
		ITuple methodRenameTuple = Factory.BASIC.createTuple(termX, termY, termZ);
		IPredicate methodRenamePredicate = Factory.BASIC.createPredicate("MethodRename", 3);
		ILiteral methodRename = Factory.BASIC.createLiteral(true, methodRenamePredicate, methodRenameTuple);

		head.add(methodRename);

		/* Create literal `MethodExists(F,S)` */
		ITuple methodExistsTuple = Factory.BASIC.createTuple(termX, termY);
		IPredicate methodExistsPredicate = Factory.BASIC.createPredicate("MethodExists", 2);
		ILiteral methodExists = Factory.BASIC.createLiteral(true, methodExistsPredicate, methodExistsTuple);

		body.add(methodExists);

		/* Create literal `DstMethodRename(F,S,D)` */
		ITuple dstMethodRenameTuple = Factory.BASIC.createTuple(termX, termY, termZ);
		IPredicate dstMethodRenamePredicate = Factory.BASIC.createPredicate("DstMethodRename", 3);
		ILiteral dstMethodRename = Factory.BASIC.createLiteral(true, dstMethodRenamePredicate, dstMethodRenameTuple);

		body.add(dstMethodRename);

		/* MethodRename(F,S,D) |- MethodExists(F,S), DstMethodRename(F,S,D) */
		List<IRule> rules = new LinkedList<IRule>();
		rules.add(Factory.BASIC.createRule(head, body));

		return rules;

	}

	@Test
	public void testSimpleRename() throws Exception {

		/* The test files. */
		String srcFile = "/Users/qhanam/Documents/workspace_commitminer/pangor/core/test/input/java-source/User.java";
		String dstFile = "/Users/qhanam/Documents/workspace_commitminer/pangor/core/test/input/java-destination/User.java";

		/* Set up the dummy data. */
		Commit commit = TestAnalysis.getCommit();
		SourceCodeFileChange sourceFileChange = TestAnalysis.getSourceCodeFileChange(srcFile, dstFile);

		/* Define the expected results. */
		List<SimpleAlert> expectedAlerts = new LinkedList<SimpleAlert>();
		expectedAlerts.add(new SimpleAlert(commit, "getName -> getUserName"));

		this.runTest(commit, sourceFileChange, expectedAlerts, true);

	}

}
