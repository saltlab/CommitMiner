package commitminer.js.analysis;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;

import org.deri.iris.api.basics.IPredicate;
import org.deri.iris.storage.IRelation;
import org.mozilla.javascript.ast.AstNode;
import org.mozilla.javascript.ast.AstRoot;
import org.mozilla.javascript.ast.FunctionNode;
import org.mozilla.javascript.ast.ScriptNode;

import commitminer.analysis.SourceCodeFileAnalysis;
import commitminer.analysis.SourceCodeFileChange;
import commitminer.analysis.flow.FunctionTreeVisitor;
import commitminer.cfg.CFG;
import commitminer.js.analysis.scope.JavaScriptScope;
import commitminer.js.analysis.scope.Scope;
import commitminer.js.analysis.scope.ScopeVisitor;
import commitminer.js.api.APIModelVisitor;
import commitminer.js.api.JSAPIFactory;
import commitminer.pointsto.PointsToPrediction;
import ca.ubc.ece.salt.gumtree.ast.ClassifiedASTNode;

/**
 * An analysis of a JavaScript file.
 *
 * An analysis of each of the functions in the class will be triggered from here,
 * including the top level script (i.e., instructions outside of a function).
 *
 * NOTES:
 * 	1. This class only works with the Rhino AST.
 * 	2. This class is thread-safe.
 */
public class ScriptAnalysis extends SourceCodeFileAnalysis {

	/** The analyses that inspects individual functions (and their CFGs). **/
	protected List<FunctionAnalysis> functionAnalyses;

	/** Used to generate unique IDs for anonymous functions. **/
	private static int anonymousIDGen = 0;

	public ScriptAnalysis(List<FunctionAnalysis> functionAnalyses) {
		this.functionAnalyses = functionAnalyses;
	}

	@Override
	public void analyze(SourceCodeFileChange sourceCodeFileChange,
						Map<IPredicate, IRelation> facts,
						ClassifiedASTNode root,
						List<CFG> cfgs) throws Exception {

		/* Check we are working with the correct AST type. */
		if(!(root instanceof AstRoot)) throw new IllegalArgumentException("The AST must be parsed from Eclipse JDT.");
		AstRoot script = (AstRoot) root;

		/* Build the scope tree for the script. */
		Map<ScriptNode, JavaScriptScope> scopeMap = new HashMap<ScriptNode, JavaScriptScope>();
		JavaScriptScope scope = this.buildScopeTree(script, null, scopeMap, null);

		/* Build the points-to prediction model. */
		PointsToPrediction model = new PointsToPrediction(
				JSAPIFactory.buildTopLevelAPI(),
				APIModelVisitor.getScriptFeatureVector(script));

		/* Analyze each of the functions in this class. */
		Stack<JavaScriptScope> stack = new Stack<JavaScriptScope>();
		stack.push(scope);
		while(!stack.isEmpty()) {
			JavaScriptScope currentScope = stack.pop();
			ScriptNode functionDeclaration = currentScope.scope;
			for(FunctionAnalysis functionAnalysis : this.functionAnalyses) {
				functionAnalysis.analyze(sourceCodeFileChange, facts,
						getFunctionCFG(functionDeclaration, cfgs),
						currentScope, model);
			}
			for(Scope<AstNode> child : currentScope.children) {
				stack.push((JavaScriptScope)child);
			}
		}

	}

	/**
	 * Builds the scope tree.
	 * @return the root of the scope tree.
	 * @throws Exception
	 */
	private JavaScriptScope buildScopeTree(ScriptNode function, JavaScriptScope parent, Map<ScriptNode, JavaScriptScope> scopeMap, String parentIdentity) throws Exception {

		/* Create a unique identity for the function. */
		String identity = function.getIdentity();
		if(identity == null) {
			identity = String.valueOf(getAnonymousFunctionID());
			function.setIdentity(identity);
			if(function.getMapping() != null) ((ScriptNode)function.getMapping()).setIdentity(identity);
		}

		/* Create a unique name for the function. */
		String name = "Script";
		if(parentIdentity != null) {
			assert(function instanceof FunctionNode);
			String functionName = ((FunctionNode)function).getName();
			name = parentIdentity + "." + functionName;
		}

        /* Create a new scope for this script or function and add it to the
         * scope tree. */
		JavaScriptScope scope = new JavaScriptScope(parent, function, name, identity);
		if(parent != null) parent.children.add(scope);
		ScopeVisitor.getLocalScope(scope);

		/* Put the scope in the scope map. */
		scopeMap.put(function, scope);

        /* Analyze the methods of the function. */
        List<FunctionNode> methods = FunctionTreeVisitor.getFunctions(function);
        for(FunctionNode method : methods) {
        	buildScopeTree(method, scope, scopeMap, identity);
        }

        return scope;

	}

	/**
	 * Find the CFG for the method declaration.
	 * @param node a {@code MethodDeclaration} node.
	 * @return the CFG for the script or function.
	 */
	private CFG getFunctionCFG(ClassifiedASTNode node, List<CFG> cfgs) {

		for(CFG cfg : cfgs) {
			if(cfg.getEntryNode().getStatement() == node) return cfg;
		}

		return null;

	}

	/**
	 * @return A unique ID for anonymous functions.
	 */
	private static synchronized int getAnonymousFunctionID() {
		int id = ScriptAnalysis.anonymousIDGen;
		ScriptAnalysis.anonymousIDGen++;
		return id;
	}

}