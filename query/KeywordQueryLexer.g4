/**
 * Define a grammar called Hello
 */
lexer grammar KeywordQueryLexer;

/* Paranthesis */
LPAREN	: '(';
RPAREN	: ')';

/* Operators */
AND 	: '&';
OR 		: '|';
NOT		: '!';
EQ		: '=';

/* Fields */
TYPE	: 'type';
CONTEXT	: 'context';
CHANGE	: 'change';
PACKAGE	: 'package';
KEYWORD	: 'keyword';

/* String Literals */
LITERAL	: '"' [a-zA-Z] '"';

/* Skip spaces, tabs and newlines. */
WS : [ \t\r\n]+ -> skip ;