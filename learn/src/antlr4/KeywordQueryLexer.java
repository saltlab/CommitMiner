package antlr4;

// Generated from KeywordQueryLexer.g4 by ANTLR 4.4
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.Lexer;
import org.antlr.v4.runtime.RuntimeMetaData;
import org.antlr.v4.runtime.atn.ATN;
import org.antlr.v4.runtime.atn.ATNDeserializer;
import org.antlr.v4.runtime.atn.LexerATNSimulator;
import org.antlr.v4.runtime.atn.PredictionContextCache;
import org.antlr.v4.runtime.dfa.DFA;

@SuppressWarnings({"all", "warnings", "unchecked", "unused", "cast"})
public class KeywordQueryLexer extends Lexer {
	static { RuntimeMetaData.checkVersion("4.4", RuntimeMetaData.VERSION); }

	protected static final DFA[] _decisionToDFA;
	protected static final PredictionContextCache _sharedContextCache =
		new PredictionContextCache();
	public static final int
		LPAREN=1, RPAREN=2, AND=3, OR=4, NOT=5, EQ=6, TYPE=7, CONTEXT=8, CHANGE=9,
		PACKAGE=10, KEYWORD=11, LITERAL=12, WS=13;
	public static String[] modeNames = {
		"DEFAULT_MODE"
	};

	public static final String[] tokenNames = {
		"'\\u0000'", "'\\u0001'", "'\\u0002'", "'\\u0003'", "'\\u0004'", "'\\u0005'",
		"'\\u0006'", "'\\u0007'", "'\b'", "'\t'", "'\n'", "'\\u000B'", "'\f'",
		"'\r'"
	};
	public static final String[] ruleNames = {
		"LPAREN", "RPAREN", "AND", "OR", "NOT", "EQ", "TYPE", "CONTEXT", "CHANGE",
		"PACKAGE", "KEYWORD", "LITERAL", "WS"
	};


	public KeywordQueryLexer(CharStream input) {
		super(input);
		_interp = new LexerATNSimulator(this,_ATN,_decisionToDFA,_sharedContextCache);
	}

	@Override
	public String getGrammarFileName() { return "KeywordQueryLexer.g4"; }

	@Override
	public String[] getTokenNames() { return tokenNames; }

	@Override
	public String[] getRuleNames() { return ruleNames; }

	@Override
	public String getSerializedATN() { return _serializedATN; }

	@Override
	public String[] getModeNames() { return modeNames; }

	@Override
	public ATN getATN() { return _ATN; }

	public static final String _serializedATN =
		"\3\u0430\ud6d1\u8206\uad2d\u4417\uaef1\u8d80\uaadd\2\17X\b\1\4\2\t\2\4"+
		"\3\t\3\4\4\t\4\4\5\t\5\4\6\t\6\4\7\t\7\4\b\t\b\4\t\t\t\4\n\t\n\4\13\t"+
		"\13\4\f\t\f\4\r\t\r\4\16\t\16\3\2\3\2\3\3\3\3\3\4\3\4\3\5\3\5\3\6\3\6"+
		"\3\7\3\7\3\b\3\b\3\b\3\b\3\b\3\t\3\t\3\t\3\t\3\t\3\t\3\t\3\t\3\n\3\n\3"+
		"\n\3\n\3\n\3\n\3\n\3\13\3\13\3\13\3\13\3\13\3\13\3\13\3\13\3\f\3\f\3\f"+
		"\3\f\3\f\3\f\3\f\3\f\3\r\3\r\3\r\3\r\3\16\6\16S\n\16\r\16\16\16T\3\16"+
		"\3\16\2\2\17\3\3\5\4\7\5\t\6\13\7\r\b\17\t\21\n\23\13\25\f\27\r\31\16"+
		"\33\17\3\2\4\4\2C\\c|\5\2\13\f\17\17\"\"X\2\3\3\2\2\2\2\5\3\2\2\2\2\7"+
		"\3\2\2\2\2\t\3\2\2\2\2\13\3\2\2\2\2\r\3\2\2\2\2\17\3\2\2\2\2\21\3\2\2"+
		"\2\2\23\3\2\2\2\2\25\3\2\2\2\2\27\3\2\2\2\2\31\3\2\2\2\2\33\3\2\2\2\3"+
		"\35\3\2\2\2\5\37\3\2\2\2\7!\3\2\2\2\t#\3\2\2\2\13%\3\2\2\2\r\'\3\2\2\2"+
		"\17)\3\2\2\2\21.\3\2\2\2\23\66\3\2\2\2\25=\3\2\2\2\27E\3\2\2\2\31M\3\2"+
		"\2\2\33R\3\2\2\2\35\36\7*\2\2\36\4\3\2\2\2\37 \7+\2\2 \6\3\2\2\2!\"\7"+
		"(\2\2\"\b\3\2\2\2#$\7~\2\2$\n\3\2\2\2%&\7#\2\2&\f\3\2\2\2\'(\7?\2\2(\16"+
		"\3\2\2\2)*\7v\2\2*+\7{\2\2+,\7r\2\2,-\7g\2\2-\20\3\2\2\2./\7e\2\2/\60"+
		"\7q\2\2\60\61\7p\2\2\61\62\7v\2\2\62\63\7g\2\2\63\64\7z\2\2\64\65\7v\2"+
		"\2\65\22\3\2\2\2\66\67\7e\2\2\678\7j\2\289\7c\2\29:\7p\2\2:;\7i\2\2;<"+
		"\7g\2\2<\24\3\2\2\2=>\7r\2\2>?\7c\2\2?@\7e\2\2@A\7m\2\2AB\7c\2\2BC\7i"+
		"\2\2CD\7g\2\2D\26\3\2\2\2EF\7m\2\2FG\7g\2\2GH\7{\2\2HI\7y\2\2IJ\7q\2\2"+
		"JK\7t\2\2KL\7f\2\2L\30\3\2\2\2MN\7$\2\2NO\t\2\2\2OP\7$\2\2P\32\3\2\2\2"+
		"QS\t\3\2\2RQ\3\2\2\2ST\3\2\2\2TR\3\2\2\2TU\3\2\2\2UV\3\2\2\2VW\b\16\2"+
		"\2W\34\3\2\2\2\4\2T\3\b\2\2";
	public static final ATN _ATN =
		new ATNDeserializer().deserialize(_serializedATN.toCharArray());
	static {
		_decisionToDFA = new DFA[_ATN.getNumberOfDecisions()];
		for (int i = 0; i < _ATN.getNumberOfDecisions(); i++) {
			_decisionToDFA[i] = new DFA(_ATN.getDecisionState(i), i);
		}
	}
}