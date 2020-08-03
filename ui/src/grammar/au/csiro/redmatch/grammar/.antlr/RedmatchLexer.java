// Generated from /Users/met045/CSIRO/workspaces/projects/redmatch/ui/src/grammar/au/csiro/redmatch/grammar/RedmatchLexer.g4 by ANTLR 4.8
import org.antlr.v4.runtime.Lexer;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.TokenStream;
import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.atn.*;
import org.antlr.v4.runtime.dfa.DFA;
import org.antlr.v4.runtime.misc.*;

@SuppressWarnings({"all", "warnings", "unchecked", "unused", "cast"})
public class RedmatchLexer extends Lexer {
	static { RuntimeMetaData.checkVersion("4.8", RuntimeMetaData.VERSION); }

	protected static final DFA[] _decisionToDFA;
	protected static final PredictionContextCache _sharedContextCache =
		new PredictionContextCache();
	public static final int
		ELSE=1, REPEAT=2, OPEN=3, CLOSE=4, NOT=5, AND=6, OR=7, TRUE=8, FALSE=9, 
		NULL=10, NOTNULL=11, VALUE=12, EQ=13, NEQ=14, LT=15, GT=16, LTE=17, GTE=18, 
		THEN=19, COMMA=20, END=21, OPEN_SQ=22, CLOSE_SQ=23, DOT=24, CONCEPT=25, 
		CONCEPT_SELECTED=26, CODE_SELECTED=27, REF=28, OPEN_CURLY_DOLLAR=29, CLOSE_CURLY=30, 
		OPEN_CURLY=31, DOTDOT=32, COLON=33, CONCEPT_LITERAL=34, CODE_LITERAL=35, 
		IDENTIFIER=36, STRING=37, NUMBER=38, COMMENT=39, LINE_COMMENT=40, WS=41, 
		DATE=42, DATETIME=43, TIME=44, CONCEPT_VALUE=45, CODE_VALUE=46;
	public static final int
		FHIR_CONCEPT=1, FHIR_CODE=2;
	public static String[] channelNames = {
		"DEFAULT_TOKEN_CHANNEL", "HIDDEN"
	};

	public static String[] modeNames = {
		"DEFAULT_MODE", "FHIR_CONCEPT", "FHIR_CODE"
	};

	private static String[] makeRuleNames() {
		return new String[] {
			"ELSE", "REPEAT", "OPEN", "CLOSE", "NOT", "AND", "OR", "TRUE", "FALSE", 
			"NULL", "NOTNULL", "VALUE", "EQ", "NEQ", "LT", "GT", "LTE", "GTE", "THEN", 
			"COMMA", "END", "OPEN_SQ", "CLOSE_SQ", "DOT", "CONCEPT", "CONCEPT_SELECTED", 
			"CODE_SELECTED", "REF", "OPEN_CURLY_DOLLAR", "CLOSE_CURLY", "OPEN_CURLY", 
			"DOTDOT", "COLON", "CONCEPT_LITERAL", "CODE_LITERAL", "IDENTIFIER", "STRING", 
			"NUMBER", "COMMENT", "LINE_COMMENT", "WS", "DATE", "DATETIME", "TIME", 
			"DATEFORMAT", "TIMEFORMAT", "TIMEZONEOFFSETFORMAT", "ESC", "UNICODE", 
			"HEX", "CONCEPT_VALUE", "CODE_VALUE"
		};
	}
	public static final String[] ruleNames = makeRuleNames();

	private static String[] makeLiteralNames() {
		return new String[] {
			null, "'ELSE'", "'REPEAT'", "'('", "')'", "'^'", "'&'", "'|'", "'TRUE'", 
			"'FALSE'", "'NULL'", "'NOTNULL'", "'VALUE'", "'='", "'!='", "'<'", "'>'", 
			"'<='", "'>='", "'->'", "','", "';'", "'['", "']'", "'.'", "'CONCEPT'", 
			"'CONCEPT_SELECTED'", "'CODE_SELECTED'", "'REF'", "'${'", "'}'", "'{'", 
			"'..'", "':'", "'CONCEPT_LITERAL'", "'CODE_LITERAL'"
		};
	}
	private static final String[] _LITERAL_NAMES = makeLiteralNames();
	private static String[] makeSymbolicNames() {
		return new String[] {
			null, "ELSE", "REPEAT", "OPEN", "CLOSE", "NOT", "AND", "OR", "TRUE", 
			"FALSE", "NULL", "NOTNULL", "VALUE", "EQ", "NEQ", "LT", "GT", "LTE", 
			"GTE", "THEN", "COMMA", "END", "OPEN_SQ", "CLOSE_SQ", "DOT", "CONCEPT", 
			"CONCEPT_SELECTED", "CODE_SELECTED", "REF", "OPEN_CURLY_DOLLAR", "CLOSE_CURLY", 
			"OPEN_CURLY", "DOTDOT", "COLON", "CONCEPT_LITERAL", "CODE_LITERAL", "IDENTIFIER", 
			"STRING", "NUMBER", "COMMENT", "LINE_COMMENT", "WS", "DATE", "DATETIME", 
			"TIME", "CONCEPT_VALUE", "CODE_VALUE"
		};
	}
	private static final String[] _SYMBOLIC_NAMES = makeSymbolicNames();
	public static final Vocabulary VOCABULARY = new VocabularyImpl(_LITERAL_NAMES, _SYMBOLIC_NAMES);

	/**
	 * @deprecated Use {@link #VOCABULARY} instead.
	 */
	@Deprecated
	public static final String[] tokenNames;
	static {
		tokenNames = new String[_SYMBOLIC_NAMES.length];
		for (int i = 0; i < tokenNames.length; i++) {
			tokenNames[i] = VOCABULARY.getLiteralName(i);
			if (tokenNames[i] == null) {
				tokenNames[i] = VOCABULARY.getSymbolicName(i);
			}

			if (tokenNames[i] == null) {
				tokenNames[i] = "<INVALID>";
			}
		}
	}

	@Override
	@Deprecated
	public String[] getTokenNames() {
		return tokenNames;
	}

	@Override

	public Vocabulary getVocabulary() {
		return VOCABULARY;
	}


	public RedmatchLexer(CharStream input) {
		super(input);
		_interp = new LexerATNSimulator(this,_ATN,_decisionToDFA,_sharedContextCache);
	}

	@Override
	public String getGrammarFileName() { return "RedmatchLexer.g4"; }

	@Override
	public String[] getRuleNames() { return ruleNames; }

	@Override
	public String getSerializedATN() { return _serializedATN; }

	@Override
	public String[] getChannelNames() { return channelNames; }

	@Override
	public String[] getModeNames() { return modeNames; }

	@Override
	public ATN getATN() { return _ATN; }

	public static final String _serializedATN =
		"\3\u608b\ua72a\u8133\ub9ed\u417c\u3be7\u7786\u5964\2\60\u01cf\b\1\b\1"+
		"\b\1\4\2\t\2\4\3\t\3\4\4\t\4\4\5\t\5\4\6\t\6\4\7\t\7\4\b\t\b\4\t\t\t\4"+
		"\n\t\n\4\13\t\13\4\f\t\f\4\r\t\r\4\16\t\16\4\17\t\17\4\20\t\20\4\21\t"+
		"\21\4\22\t\22\4\23\t\23\4\24\t\24\4\25\t\25\4\26\t\26\4\27\t\27\4\30\t"+
		"\30\4\31\t\31\4\32\t\32\4\33\t\33\4\34\t\34\4\35\t\35\4\36\t\36\4\37\t"+
		"\37\4 \t \4!\t!\4\"\t\"\4#\t#\4$\t$\4%\t%\4&\t&\4\'\t\'\4(\t(\4)\t)\4"+
		"*\t*\4+\t+\4,\t,\4-\t-\4.\t.\4/\t/\4\60\t\60\4\61\t\61\4\62\t\62\4\63"+
		"\t\63\4\64\t\64\4\65\t\65\3\2\3\2\3\2\3\2\3\2\3\3\3\3\3\3\3\3\3\3\3\3"+
		"\3\3\3\4\3\4\3\5\3\5\3\6\3\6\3\7\3\7\3\b\3\b\3\t\3\t\3\t\3\t\3\t\3\n\3"+
		"\n\3\n\3\n\3\n\3\n\3\13\3\13\3\13\3\13\3\13\3\f\3\f\3\f\3\f\3\f\3\f\3"+
		"\f\3\f\3\r\3\r\3\r\3\r\3\r\3\r\3\16\3\16\3\17\3\17\3\17\3\20\3\20\3\21"+
		"\3\21\3\22\3\22\3\22\3\23\3\23\3\23\3\24\3\24\3\24\3\25\3\25\3\26\3\26"+
		"\3\27\3\27\3\30\3\30\3\31\3\31\3\32\3\32\3\32\3\32\3\32\3\32\3\32\3\32"+
		"\3\33\3\33\3\33\3\33\3\33\3\33\3\33\3\33\3\33\3\33\3\33\3\33\3\33\3\33"+
		"\3\33\3\33\3\33\3\34\3\34\3\34\3\34\3\34\3\34\3\34\3\34\3\34\3\34\3\34"+
		"\3\34\3\34\3\34\3\35\3\35\3\35\3\35\3\36\3\36\3\36\3\37\3\37\3 \3 \3!"+
		"\3!\3!\3\"\3\"\3#\3#\3#\3#\3#\3#\3#\3#\3#\3#\3#\3#\3#\3#\3#\3#\3#\3#\3"+
		"$\3$\3$\3$\3$\3$\3$\3$\3$\3$\3$\3$\3$\3$\3$\3%\3%\7%\u0118\n%\f%\16%\u011b"+
		"\13%\3&\3&\3&\7&\u0120\n&\f&\16&\u0123\13&\3&\3&\3\'\6\'\u0128\n\'\r\'"+
		"\16\'\u0129\3\'\3\'\6\'\u012e\n\'\r\'\16\'\u012f\5\'\u0132\n\'\3(\3(\3"+
		"(\3(\7(\u0138\n(\f(\16(\u013b\13(\3(\3(\3(\3(\3(\3)\3)\3)\3)\7)\u0146"+
		"\n)\f)\16)\u0149\13)\3)\3)\3*\6*\u014e\n*\r*\16*\u014f\3*\3*\3+\3+\3+"+
		"\3,\3,\3,\3,\3,\5,\u015c\n,\5,\u015e\n,\3-\3-\3-\3-\3.\3.\3.\3.\3.\3."+
		"\3.\3.\3.\3.\5.\u016e\n.\5.\u0170\n.\3/\3/\3/\3/\3/\3/\3/\3/\3/\3/\6/"+
		"\u017c\n/\r/\16/\u017d\5/\u0180\n/\5/\u0182\n/\5/\u0184\n/\3\60\3\60\3"+
		"\60\3\60\3\60\3\60\3\60\5\60\u018d\n\60\3\61\3\61\3\61\5\61\u0192\n\61"+
		"\3\62\3\62\3\62\3\62\3\62\3\62\3\63\3\63\3\64\3\64\6\64\u019e\n\64\r\64"+
		"\16\64\u019f\3\64\3\64\6\64\u01a4\n\64\r\64\16\64\u01a5\3\64\3\64\6\64"+
		"\u01aa\n\64\r\64\16\64\u01ab\7\64\u01ae\n\64\f\64\16\64\u01b1\13\64\3"+
		"\64\3\64\5\64\u01b5\n\64\3\64\3\64\3\64\3\64\3\65\3\65\6\65\u01bd\n\65"+
		"\r\65\16\65\u01be\3\65\3\65\6\65\u01c3\n\65\r\65\16\65\u01c4\7\65\u01c7"+
		"\n\65\f\65\16\65\u01ca\13\65\3\65\3\65\3\65\3\65\4\u0121\u0139\2\66\5"+
		"\3\7\4\t\5\13\6\r\7\17\b\21\t\23\n\25\13\27\f\31\r\33\16\35\17\37\20!"+
		"\21#\22%\23\'\24)\25+\26-\27/\30\61\31\63\32\65\33\67\349\35;\36=\37?"+
		" A!C\"E#G$I%K&M\'O(Q)S*U+W,Y-[.]\2_\2a\2c\2e\2g\2i/k\60\5\2\3\4\n\6\2"+
		"//C\\aac|\7\2//\62;C\\aac|\3\2\62;\4\2\f\f\17\17\5\2\13\f\17\17\"\"\4"+
		"\2--//\n\2))\61\61^^bbhhppttvv\5\2\62;CHch\2\u01e1\2\5\3\2\2\2\2\7\3\2"+
		"\2\2\2\t\3\2\2\2\2\13\3\2\2\2\2\r\3\2\2\2\2\17\3\2\2\2\2\21\3\2\2\2\2"+
		"\23\3\2\2\2\2\25\3\2\2\2\2\27\3\2\2\2\2\31\3\2\2\2\2\33\3\2\2\2\2\35\3"+
		"\2\2\2\2\37\3\2\2\2\2!\3\2\2\2\2#\3\2\2\2\2%\3\2\2\2\2\'\3\2\2\2\2)\3"+
		"\2\2\2\2+\3\2\2\2\2-\3\2\2\2\2/\3\2\2\2\2\61\3\2\2\2\2\63\3\2\2\2\2\65"+
		"\3\2\2\2\2\67\3\2\2\2\29\3\2\2\2\2;\3\2\2\2\2=\3\2\2\2\2?\3\2\2\2\2A\3"+
		"\2\2\2\2C\3\2\2\2\2E\3\2\2\2\2G\3\2\2\2\2I\3\2\2\2\2K\3\2\2\2\2M\3\2\2"+
		"\2\2O\3\2\2\2\2Q\3\2\2\2\2S\3\2\2\2\2U\3\2\2\2\2W\3\2\2\2\2Y\3\2\2\2\2"+
		"[\3\2\2\2\3i\3\2\2\2\4k\3\2\2\2\5m\3\2\2\2\7r\3\2\2\2\ty\3\2\2\2\13{\3"+
		"\2\2\2\r}\3\2\2\2\17\177\3\2\2\2\21\u0081\3\2\2\2\23\u0083\3\2\2\2\25"+
		"\u0088\3\2\2\2\27\u008e\3\2\2\2\31\u0093\3\2\2\2\33\u009b\3\2\2\2\35\u00a1"+
		"\3\2\2\2\37\u00a3\3\2\2\2!\u00a6\3\2\2\2#\u00a8\3\2\2\2%\u00aa\3\2\2\2"+
		"\'\u00ad\3\2\2\2)\u00b0\3\2\2\2+\u00b3\3\2\2\2-\u00b5\3\2\2\2/\u00b7\3"+
		"\2\2\2\61\u00b9\3\2\2\2\63\u00bb\3\2\2\2\65\u00bd\3\2\2\2\67\u00c5\3\2"+
		"\2\29\u00d6\3\2\2\2;\u00e4\3\2\2\2=\u00e8\3\2\2\2?\u00eb\3\2\2\2A\u00ed"+
		"\3\2\2\2C\u00ef\3\2\2\2E\u00f2\3\2\2\2G\u00f4\3\2\2\2I\u0106\3\2\2\2K"+
		"\u0115\3\2\2\2M\u011c\3\2\2\2O\u0127\3\2\2\2Q\u0133\3\2\2\2S\u0141\3\2"+
		"\2\2U\u014d\3\2\2\2W\u0153\3\2\2\2Y\u0156\3\2\2\2[\u015f\3\2\2\2]\u0163"+
		"\3\2\2\2_\u0171\3\2\2\2a\u018c\3\2\2\2c\u018e\3\2\2\2e\u0193\3\2\2\2g"+
		"\u0199\3\2\2\2i\u019b\3\2\2\2k\u01ba\3\2\2\2mn\7G\2\2no\7N\2\2op\7U\2"+
		"\2pq\7G\2\2q\6\3\2\2\2rs\7T\2\2st\7G\2\2tu\7R\2\2uv\7G\2\2vw\7C\2\2wx"+
		"\7V\2\2x\b\3\2\2\2yz\7*\2\2z\n\3\2\2\2{|\7+\2\2|\f\3\2\2\2}~\7`\2\2~\16"+
		"\3\2\2\2\177\u0080\7(\2\2\u0080\20\3\2\2\2\u0081\u0082\7~\2\2\u0082\22"+
		"\3\2\2\2\u0083\u0084\7V\2\2\u0084\u0085\7T\2\2\u0085\u0086\7W\2\2\u0086"+
		"\u0087\7G\2\2\u0087\24\3\2\2\2\u0088\u0089\7H\2\2\u0089\u008a\7C\2\2\u008a"+
		"\u008b\7N\2\2\u008b\u008c\7U\2\2\u008c\u008d\7G\2\2\u008d\26\3\2\2\2\u008e"+
		"\u008f\7P\2\2\u008f\u0090\7W\2\2\u0090\u0091\7N\2\2\u0091\u0092\7N\2\2"+
		"\u0092\30\3\2\2\2\u0093\u0094\7P\2\2\u0094\u0095\7Q\2\2\u0095\u0096\7"+
		"V\2\2\u0096\u0097\7P\2\2\u0097\u0098\7W\2\2\u0098\u0099\7N\2\2\u0099\u009a"+
		"\7N\2\2\u009a\32\3\2\2\2\u009b\u009c\7X\2\2\u009c\u009d\7C\2\2\u009d\u009e"+
		"\7N\2\2\u009e\u009f\7W\2\2\u009f\u00a0\7G\2\2\u00a0\34\3\2\2\2\u00a1\u00a2"+
		"\7?\2\2\u00a2\36\3\2\2\2\u00a3\u00a4\7#\2\2\u00a4\u00a5\7?\2\2\u00a5 "+
		"\3\2\2\2\u00a6\u00a7\7>\2\2\u00a7\"\3\2\2\2\u00a8\u00a9\7@\2\2\u00a9$"+
		"\3\2\2\2\u00aa\u00ab\7>\2\2\u00ab\u00ac\7?\2\2\u00ac&\3\2\2\2\u00ad\u00ae"+
		"\7@\2\2\u00ae\u00af\7?\2\2\u00af(\3\2\2\2\u00b0\u00b1\7/\2\2\u00b1\u00b2"+
		"\7@\2\2\u00b2*\3\2\2\2\u00b3\u00b4\7.\2\2\u00b4,\3\2\2\2\u00b5\u00b6\7"+
		"=\2\2\u00b6.\3\2\2\2\u00b7\u00b8\7]\2\2\u00b8\60\3\2\2\2\u00b9\u00ba\7"+
		"_\2\2\u00ba\62\3\2\2\2\u00bb\u00bc\7\60\2\2\u00bc\64\3\2\2\2\u00bd\u00be"+
		"\7E\2\2\u00be\u00bf\7Q\2\2\u00bf\u00c0\7P\2\2\u00c0\u00c1\7E\2\2\u00c1"+
		"\u00c2\7G\2\2\u00c2\u00c3\7R\2\2\u00c3\u00c4\7V\2\2\u00c4\66\3\2\2\2\u00c5"+
		"\u00c6\7E\2\2\u00c6\u00c7\7Q\2\2\u00c7\u00c8\7P\2\2\u00c8\u00c9\7E\2\2"+
		"\u00c9\u00ca\7G\2\2\u00ca\u00cb\7R\2\2\u00cb\u00cc\7V\2\2\u00cc\u00cd"+
		"\7a\2\2\u00cd\u00ce\7U\2\2\u00ce\u00cf\7G\2\2\u00cf\u00d0\7N\2\2\u00d0"+
		"\u00d1\7G\2\2\u00d1\u00d2\7E\2\2\u00d2\u00d3\7V\2\2\u00d3\u00d4\7G\2\2"+
		"\u00d4\u00d5\7F\2\2\u00d58\3\2\2\2\u00d6\u00d7\7E\2\2\u00d7\u00d8\7Q\2"+
		"\2\u00d8\u00d9\7F\2\2\u00d9\u00da\7G\2\2\u00da\u00db\7a\2\2\u00db\u00dc"+
		"\7U\2\2\u00dc\u00dd\7G\2\2\u00dd\u00de\7N\2\2\u00de\u00df\7G\2\2\u00df"+
		"\u00e0\7E\2\2\u00e0\u00e1\7V\2\2\u00e1\u00e2\7G\2\2\u00e2\u00e3\7F\2\2"+
		"\u00e3:\3\2\2\2\u00e4\u00e5\7T\2\2\u00e5\u00e6\7G\2\2\u00e6\u00e7\7H\2"+
		"\2\u00e7<\3\2\2\2\u00e8\u00e9\7&\2\2\u00e9\u00ea\7}\2\2\u00ea>\3\2\2\2"+
		"\u00eb\u00ec\7\177\2\2\u00ec@\3\2\2\2\u00ed\u00ee\7}\2\2\u00eeB\3\2\2"+
		"\2\u00ef\u00f0\7\60\2\2\u00f0\u00f1\7\60\2\2\u00f1D\3\2\2\2\u00f2\u00f3"+
		"\7<\2\2\u00f3F\3\2\2\2\u00f4\u00f5\7E\2\2\u00f5\u00f6\7Q\2\2\u00f6\u00f7"+
		"\7P\2\2\u00f7\u00f8\7E\2\2\u00f8\u00f9\7G\2\2\u00f9\u00fa\7R\2\2\u00fa"+
		"\u00fb\7V\2\2\u00fb\u00fc\7a\2\2\u00fc\u00fd\7N\2\2\u00fd\u00fe\7K\2\2"+
		"\u00fe\u00ff\7V\2\2\u00ff\u0100\7G\2\2\u0100\u0101\7T\2\2\u0101\u0102"+
		"\7C\2\2\u0102\u0103\7N\2\2\u0103\u0104\3\2\2\2\u0104\u0105\b#\2\2\u0105"+
		"H\3\2\2\2\u0106\u0107\7E\2\2\u0107\u0108\7Q\2\2\u0108\u0109\7F\2\2\u0109"+
		"\u010a\7G\2\2\u010a\u010b\7a\2\2\u010b\u010c\7N\2\2\u010c\u010d\7K\2\2"+
		"\u010d\u010e\7V\2\2\u010e\u010f\7G\2\2\u010f\u0110\7T\2\2\u0110\u0111"+
		"\7C\2\2\u0111\u0112\7N\2\2\u0112\u0113\3\2\2\2\u0113\u0114\b$\3\2\u0114"+
		"J\3\2\2\2\u0115\u0119\t\2\2\2\u0116\u0118\t\3\2\2\u0117\u0116\3\2\2\2"+
		"\u0118\u011b\3\2\2\2\u0119\u0117\3\2\2\2\u0119\u011a\3\2\2\2\u011aL\3"+
		"\2\2\2\u011b\u0119\3\2\2\2\u011c\u0121\7)\2\2\u011d\u0120\5c\61\2\u011e"+
		"\u0120\13\2\2\2\u011f\u011d\3\2\2\2\u011f\u011e\3\2\2\2\u0120\u0123\3"+
		"\2\2\2\u0121\u0122\3\2\2\2\u0121\u011f\3\2\2\2\u0122\u0124\3\2\2\2\u0123"+
		"\u0121\3\2\2\2\u0124\u0125\7)\2\2\u0125N\3\2\2\2\u0126\u0128\t\4\2\2\u0127"+
		"\u0126\3\2\2\2\u0128\u0129\3\2\2\2\u0129\u0127\3\2\2\2\u0129\u012a\3\2"+
		"\2\2\u012a\u0131\3\2\2\2\u012b\u012d\7\60\2\2\u012c\u012e\t\4\2\2\u012d"+
		"\u012c\3\2\2\2\u012e\u012f\3\2\2\2\u012f\u012d\3\2\2\2\u012f\u0130\3\2"+
		"\2\2\u0130\u0132\3\2\2\2\u0131\u012b\3\2\2\2\u0131\u0132\3\2\2\2\u0132"+
		"P\3\2\2\2\u0133\u0134\7\61\2\2\u0134\u0135\7,\2\2\u0135\u0139\3\2\2\2"+
		"\u0136\u0138\13\2\2\2\u0137\u0136\3\2\2\2\u0138\u013b\3\2\2\2\u0139\u013a"+
		"\3\2\2\2\u0139\u0137\3\2\2\2\u013a\u013c\3\2\2\2\u013b\u0139\3\2\2\2\u013c"+
		"\u013d\7,\2\2\u013d\u013e\7\61\2\2\u013e\u013f\3\2\2\2\u013f\u0140\b("+
		"\4\2\u0140R\3\2\2\2\u0141\u0142\7\61\2\2\u0142\u0143\7\61\2\2\u0143\u0147"+
		"\3\2\2\2\u0144\u0146\n\5\2\2\u0145\u0144\3\2\2\2\u0146\u0149\3\2\2\2\u0147"+
		"\u0145\3\2\2\2\u0147\u0148\3\2\2\2\u0148\u014a\3\2\2\2\u0149\u0147\3\2"+
		"\2\2\u014a\u014b\b)\4\2\u014bT\3\2\2\2\u014c\u014e\t\6\2\2\u014d\u014c"+
		"\3\2\2\2\u014e\u014f\3\2\2\2\u014f\u014d\3\2\2\2\u014f\u0150\3\2\2\2\u0150"+
		"\u0151\3\2\2\2\u0151\u0152\b*\4\2\u0152V\3\2\2\2\u0153\u0154\7B\2\2\u0154"+
		"\u0155\5].\2\u0155X\3\2\2\2\u0156\u0157\7B\2\2\u0157\u0158\5].\2\u0158"+
		"\u015d\7V\2\2\u0159\u015b\5_/\2\u015a\u015c\5a\60\2\u015b\u015a\3\2\2"+
		"\2\u015b\u015c\3\2\2\2\u015c\u015e\3\2\2\2\u015d\u0159\3\2\2\2\u015d\u015e"+
		"\3\2\2\2\u015eZ\3\2\2\2\u015f\u0160\7B\2\2\u0160\u0161\7V\2\2\u0161\u0162"+
		"\5_/\2\u0162\\\3\2\2\2\u0163\u0164\t\4\2\2\u0164\u0165\t\4\2\2\u0165\u0166"+
		"\t\4\2\2\u0166\u016f\t\4\2\2\u0167\u0168\7/\2\2\u0168\u0169\t\4\2\2\u0169"+
		"\u016d\t\4\2\2\u016a\u016b\7/\2\2\u016b\u016c\t\4\2\2\u016c\u016e\t\4"+
		"\2\2\u016d\u016a\3\2\2\2\u016d\u016e\3\2\2\2\u016e\u0170\3\2\2\2\u016f"+
		"\u0167\3\2\2\2\u016f\u0170\3\2\2\2\u0170^\3\2\2\2\u0171\u0172\t\4\2\2"+
		"\u0172\u0183\t\4\2\2\u0173\u0174\7<\2\2\u0174\u0175\t\4\2\2\u0175\u0181"+
		"\t\4\2\2\u0176\u0177\7<\2\2\u0177\u0178\t\4\2\2\u0178\u017f\t\4\2\2\u0179"+
		"\u017b\7\60\2\2\u017a\u017c\t\4\2\2\u017b\u017a\3\2\2\2\u017c\u017d\3"+
		"\2\2\2\u017d\u017b\3\2\2\2\u017d\u017e\3\2\2\2\u017e\u0180\3\2\2\2\u017f"+
		"\u0179\3\2\2\2\u017f\u0180\3\2\2\2\u0180\u0182\3\2\2\2\u0181\u0176\3\2"+
		"\2\2\u0181\u0182\3\2\2\2\u0182\u0184\3\2\2\2\u0183\u0173\3\2\2\2\u0183"+
		"\u0184\3\2\2\2\u0184`\3\2\2\2\u0185\u018d\7\\\2\2\u0186\u0187\t\7\2\2"+
		"\u0187\u0188\t\4\2\2\u0188\u0189\t\4\2\2\u0189\u018a\7<\2\2\u018a\u018b"+
		"\t\4\2\2\u018b\u018d\t\4\2\2\u018c\u0185\3\2\2\2\u018c\u0186\3\2\2\2\u018d"+
		"b\3\2\2\2\u018e\u0191\7^\2\2\u018f\u0192\t\b\2\2\u0190\u0192\5e\62\2\u0191"+
		"\u018f\3\2\2\2\u0191\u0190\3\2\2\2\u0192d\3\2\2\2\u0193\u0194\7w\2\2\u0194"+
		"\u0195\5g\63\2\u0195\u0196\5g\63\2\u0196\u0197\5g\63\2\u0197\u0198\5g"+
		"\63\2\u0198f\3\2\2\2\u0199\u019a\t\t\2\2\u019ah\3\2\2\2\u019b\u019d\7"+
		"*\2\2\u019c\u019e\n\6\2\2\u019d\u019c\3\2\2\2\u019e\u019f\3\2\2\2\u019f"+
		"\u019d\3\2\2\2\u019f\u01a0\3\2\2\2\u01a0\u01a1\3\2\2\2\u01a1\u01a3\7~"+
		"\2\2\u01a2\u01a4\n\6\2\2\u01a3\u01a2\3\2\2\2\u01a4\u01a5\3\2\2\2\u01a5"+
		"\u01a3\3\2\2\2\u01a5\u01a6\3\2\2\2\u01a6\u01af\3\2\2\2\u01a7\u01a9\t\6"+
		"\2\2\u01a8\u01aa\n\6\2\2\u01a9\u01a8\3\2\2\2\u01aa\u01ab\3\2\2\2\u01ab"+
		"\u01a9\3\2\2\2\u01ab\u01ac\3\2\2\2\u01ac\u01ae\3\2\2\2\u01ad\u01a7\3\2"+
		"\2\2\u01ae\u01b1\3\2\2\2\u01af\u01ad\3\2\2\2\u01af\u01b0\3\2\2\2\u01b0"+
		"\u01b4\3\2\2\2\u01b1\u01af\3\2\2\2\u01b2\u01b3\7~\2\2\u01b3\u01b5\5M&"+
		"\2\u01b4\u01b2\3\2\2\2\u01b4\u01b5\3\2\2\2\u01b5\u01b6\3\2\2\2\u01b6\u01b7"+
		"\7+\2\2\u01b7\u01b8\3\2\2\2\u01b8\u01b9\b\64\5\2\u01b9j\3\2\2\2\u01ba"+
		"\u01bc\7*\2\2\u01bb\u01bd\n\6\2\2\u01bc\u01bb\3\2\2\2\u01bd\u01be\3\2"+
		"\2\2\u01be\u01bc\3\2\2\2\u01be\u01bf\3\2\2\2\u01bf\u01c8\3\2\2\2\u01c0"+
		"\u01c2\t\6\2\2\u01c1\u01c3\n\6\2\2\u01c2\u01c1\3\2\2\2\u01c3\u01c4\3\2"+
		"\2\2\u01c4\u01c2\3\2\2\2\u01c4\u01c5\3\2\2\2\u01c5\u01c7\3\2\2\2\u01c6"+
		"\u01c0\3\2\2\2\u01c7\u01ca\3\2\2\2\u01c8\u01c6\3\2\2\2\u01c8\u01c9\3\2"+
		"\2\2\u01c9\u01cb\3\2\2\2\u01ca\u01c8\3\2\2\2\u01cb\u01cc\7+\2\2\u01cc"+
		"\u01cd\3\2\2\2\u01cd\u01ce\b\65\5\2\u01cel\3\2\2\2 \2\3\4\u0119\u011f"+
		"\u0121\u0129\u012f\u0131\u0139\u0147\u014f\u015b\u015d\u016d\u016f\u017d"+
		"\u017f\u0181\u0183\u018c\u0191\u019f\u01a5\u01ab\u01af\u01b4\u01be\u01c4"+
		"\u01c8\6\7\3\2\7\4\2\b\2\2\6\2\2";
	public static final ATN _ATN =
		new ATNDeserializer().deserialize(_serializedATN.toCharArray());
	static {
		_decisionToDFA = new DFA[_ATN.getNumberOfDecisions()];
		for (int i = 0; i < _ATN.getNumberOfDecisions(); i++) {
			_decisionToDFA[i] = new DFA(_ATN.getDecisionState(i), i);
		}
	}
}