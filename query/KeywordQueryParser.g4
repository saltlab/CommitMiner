/**
 * Define a grammar called Hello
 */
parser grammar KeywordQueryParser;

options
   { tokenVocab = KeywordQueryLexer; }
   
query
	: expression
	;
	
expression
	: andExpression (OR andExpression)*
	;
	
andExpression
	: notExpression (AND notExpression)*
	; 
	
notExpression
	: NOT? atom 
	;

atom
	: comparison
	| LPAREN expression RPAREN
	;
	
comparison
	: field EQ LITERAL
	;
   
field : TYPE | CONTEXT | CHANGE | PACKAGE | KEYWORD ;