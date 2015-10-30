package ca.ubc.ece.salt.pangor.java.test.cfg;

import java.io.File;
import java.io.IOException;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.junit.Assert;
import org.junit.Test;

import ca.ubc.ece.salt.pangor.cfg.CFG;
import ca.ubc.ece.salt.pangor.cfg.CFGFactory;
import ca.ubc.ece.salt.pangor.java.cfg.JavaCFGFactory;
import ca.ubc.ece.salt.pangor.test.cfg.MockCFG;
import ca.ubc.ece.salt.pangor.test.cfg.MockCFGEdge;
import ca.ubc.ece.salt.pangor.test.cfg.MockCFGNode;


public class TestJavaCFGFactory {

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Test
	public void test() throws IOException {

		/* Set up the mock CFG. */
		MockCFGNode entry = new MockCFGNode(0, "METHOD_ENTRY");
		MockCFGNode two = new MockCFGNode(2, "VariableDeclarationStatement");
		MockCFGNode three = new MockCFGNode(3, "VariableDeclarationStatement");
		MockCFGNode four = new MockCFGNode(4, "ExpressionStatement");
		MockCFGNode exit = new MockCFGNode(1, "METHOD_EXIT");

		entry.addEdge(new MockCFGEdge(entry, two, null));
		two.addEdge(new MockCFGEdge(two, three, null));
		three.addEdge(new MockCFGEdge(three, four, null));
		four.addEdge(new MockCFGEdge(four, exit, null));

		MockCFG expected = new MockCFG(entry, true);

		/* The test file. */
		String file = "/Users/qhanam/Documents/workspace_pangor/pangor/java/test/input/SimpleCFG.java";
		String source = readFile(file);

		/* Parse the file into a CompilationUnit. */
		ASTParser parser = ASTParser.newParser(AST.JLS8);
		parser.setSource(source.toCharArray());

		/* Source to be parsed as a compilation unit. */
		parser.setKind(ASTParser.K_COMPILATION_UNIT);

		/* Set the options for Java parsing. */
		Map pOptions = JavaCore.getOptions();
		pOptions.put(JavaCore.COMPILER_COMPLIANCE, JavaCore.VERSION_1_6);
		pOptions.put(JavaCore.COMPILER_CODEGEN_TARGET_PLATFORM, JavaCore.VERSION_1_6);
		pOptions.put(JavaCore.COMPILER_SOURCE, JavaCore.VERSION_1_6);
		parser.setCompilerOptions(pOptions);

		/* Generate the AST for the file. */
		CompilationUnit cu = (CompilationUnit)parser.createAST(null);

		CFGFactory cfgFactory = new JavaCFGFactory();
		CFG actual = cfgFactory.createCFGs(cu).get(0);

		/* Check equivalence. */
		Assert.assertTrue(expected.equals(actual));

	}

	/**
	 * Reads the contents of a source code file into a string.
	 * @param path The path to the source code file.
	 * @return A string containing the source code.
	 * @throws IOException
	 */
	private static String readFile(String path) throws IOException {
		return FileUtils.readFileToString(new File(path));
	}

}
