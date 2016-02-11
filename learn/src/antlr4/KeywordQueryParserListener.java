package antlr4;

// Generated from KeywordQueryParser.g4 by ANTLR 4.4
import org.antlr.v4.runtime.tree.ParseTreeListener;

/**
 * This interface defines a complete listener for a parse tree produced by
 * {@link KeywordQueryParser}.
 */
public interface KeywordQueryParserListener extends ParseTreeListener {
	/**
	 * Enter a parse tree produced by {@link KeywordQueryParser#andExpression}.
	 * @param ctx the parse tree
	 */
	void enterAndExpression(KeywordQueryParser.AndExpressionContext ctx);
	/**
	 * Exit a parse tree produced by {@link KeywordQueryParser#andExpression}.
	 * @param ctx the parse tree
	 */
	void exitAndExpression(KeywordQueryParser.AndExpressionContext ctx);
	/**
	 * Enter a parse tree produced by {@link KeywordQueryParser#expression}.
	 * @param ctx the parse tree
	 */
	void enterExpression(KeywordQueryParser.ExpressionContext ctx);
	/**
	 * Exit a parse tree produced by {@link KeywordQueryParser#expression}.
	 * @param ctx the parse tree
	 */
	void exitExpression(KeywordQueryParser.ExpressionContext ctx);
	/**
	 * Enter a parse tree produced by {@link KeywordQueryParser#comparison}.
	 * @param ctx the parse tree
	 */
	void enterComparison(KeywordQueryParser.ComparisonContext ctx);
	/**
	 * Exit a parse tree produced by {@link KeywordQueryParser#comparison}.
	 * @param ctx the parse tree
	 */
	void exitComparison(KeywordQueryParser.ComparisonContext ctx);
	/**
	 * Enter a parse tree produced by {@link KeywordQueryParser#field}.
	 * @param ctx the parse tree
	 */
	void enterField(KeywordQueryParser.FieldContext ctx);
	/**
	 * Exit a parse tree produced by {@link KeywordQueryParser#field}.
	 * @param ctx the parse tree
	 */
	void exitField(KeywordQueryParser.FieldContext ctx);
	/**
	 * Enter a parse tree produced by {@link KeywordQueryParser#query}.
	 * @param ctx the parse tree
	 */
	void enterQuery(KeywordQueryParser.QueryContext ctx);
	/**
	 * Exit a parse tree produced by {@link KeywordQueryParser#query}.
	 * @param ctx the parse tree
	 */
	void exitQuery(KeywordQueryParser.QueryContext ctx);
	/**
	 * Enter a parse tree produced by {@link KeywordQueryParser#notExpression}.
	 * @param ctx the parse tree
	 */
	void enterNotExpression(KeywordQueryParser.NotExpressionContext ctx);
	/**
	 * Exit a parse tree produced by {@link KeywordQueryParser#notExpression}.
	 * @param ctx the parse tree
	 */
	void exitNotExpression(KeywordQueryParser.NotExpressionContext ctx);
	/**
	 * Enter a parse tree produced by {@link KeywordQueryParser#atom}.
	 * @param ctx the parse tree
	 */
	void enterAtom(KeywordQueryParser.AtomContext ctx);
	/**
	 * Exit a parse tree produced by {@link KeywordQueryParser#atom}.
	 * @param ctx the parse tree
	 */
	void exitAtom(KeywordQueryParser.AtomContext ctx);
}