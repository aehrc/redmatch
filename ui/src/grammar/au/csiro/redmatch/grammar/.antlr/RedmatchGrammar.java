// Generated from /Users/met045/CSIRO/workspaces/projects/redmatch/ui/src/grammar/au/csiro/redmatch/grammar/RedmatchGrammar.g4 by ANTLR 4.8
import org.antlr.v4.runtime.atn.*;
import org.antlr.v4.runtime.dfa.DFA;
import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.misc.*;
import org.antlr.v4.runtime.tree.*;
import java.util.List;
import java.util.Iterator;
import java.util.ArrayList;

@SuppressWarnings({"all", "warnings", "unchecked", "unused", "cast"})
public class RedmatchGrammar extends Parser {
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
		RULE_document = 0, RULE_fcRule = 1, RULE_fcBody = 2, RULE_repeatsClause = 3, 
		RULE_condition = 4, RULE_resource = 5, RULE_attribute = 6, RULE_value = 7, 
		RULE_reference = 8, RULE_variableIdentifier = 9;
	private static String[] makeRuleNames() {
		return new String[] {
			"document", "fcRule", "fcBody", "repeatsClause", "condition", "resource", 
			"attribute", "value", "reference", "variableIdentifier"
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

	@Override
	public String getGrammarFileName() { return "RedmatchGrammar.g4"; }

	@Override
	public String[] getRuleNames() { return ruleNames; }

	@Override
	public String getSerializedATN() { return _serializedATN; }

	@Override
	public ATN getATN() { return _ATN; }

	public RedmatchGrammar(TokenStream input) {
		super(input);
		_interp = new ParserATNSimulator(this,_ATN,_decisionToDFA,_sharedContextCache);
	}

	public static class DocumentContext extends ParserRuleContext {
		public List<FcRuleContext> fcRule() {
			return getRuleContexts(FcRuleContext.class);
		}
		public FcRuleContext fcRule(int i) {
			return getRuleContext(FcRuleContext.class,i);
		}
		public DocumentContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_document; }
	}

	public final DocumentContext document() throws RecognitionException {
		DocumentContext _localctx = new DocumentContext(_ctx, getState());
		enterRule(_localctx, 0, RULE_document);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(23);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while ((((_la) & ~0x3f) == 0 && ((1L << _la) & ((1L << REPEAT) | (1L << OPEN) | (1L << NOT) | (1L << TRUE) | (1L << FALSE) | (1L << NULL) | (1L << NOTNULL) | (1L << VALUE))) != 0)) {
				{
				{
				setState(20);
				fcRule();
				}
				}
				setState(25);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class FcRuleContext extends ParserRuleContext {
		public ConditionContext condition() {
			return getRuleContext(ConditionContext.class,0);
		}
		public List<FcBodyContext> fcBody() {
			return getRuleContexts(FcBodyContext.class);
		}
		public FcBodyContext fcBody(int i) {
			return getRuleContext(FcBodyContext.class,i);
		}
		public RepeatsClauseContext repeatsClause() {
			return getRuleContext(RepeatsClauseContext.class,0);
		}
		public TerminalNode ELSE() { return getToken(RedmatchGrammar.ELSE, 0); }
		public FcRuleContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_fcRule; }
	}

	public final FcRuleContext fcRule() throws RecognitionException {
		FcRuleContext _localctx = new FcRuleContext(_ctx, getState());
		enterRule(_localctx, 2, RULE_fcRule);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(27);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==REPEAT) {
				{
				setState(26);
				repeatsClause();
				}
			}

			setState(29);
			condition(0);
			setState(30);
			fcBody();
			setState(33);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==ELSE) {
				{
				setState(31);
				match(ELSE);
				setState(32);
				fcBody();
				}
			}

			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class FcBodyContext extends ParserRuleContext {
		public TerminalNode OPEN_CURLY() { return getToken(RedmatchGrammar.OPEN_CURLY, 0); }
		public TerminalNode CLOSE_CURLY() { return getToken(RedmatchGrammar.CLOSE_CURLY, 0); }
		public List<ResourceContext> resource() {
			return getRuleContexts(ResourceContext.class);
		}
		public ResourceContext resource(int i) {
			return getRuleContext(ResourceContext.class,i);
		}
		public List<FcRuleContext> fcRule() {
			return getRuleContexts(FcRuleContext.class);
		}
		public FcRuleContext fcRule(int i) {
			return getRuleContext(FcRuleContext.class,i);
		}
		public FcBodyContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_fcBody; }
	}

	public final FcBodyContext fcBody() throws RecognitionException {
		FcBodyContext _localctx = new FcBodyContext(_ctx, getState());
		enterRule(_localctx, 4, RULE_fcBody);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(35);
			match(OPEN_CURLY);
			setState(40);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while ((((_la) & ~0x3f) == 0 && ((1L << _la) & ((1L << REPEAT) | (1L << OPEN) | (1L << NOT) | (1L << TRUE) | (1L << FALSE) | (1L << NULL) | (1L << NOTNULL) | (1L << VALUE) | (1L << IDENTIFIER))) != 0)) {
				{
				setState(38);
				_errHandler.sync(this);
				switch (_input.LA(1)) {
				case IDENTIFIER:
					{
					setState(36);
					resource();
					}
					break;
				case REPEAT:
				case OPEN:
				case NOT:
				case TRUE:
				case FALSE:
				case NULL:
				case NOTNULL:
				case VALUE:
					{
					setState(37);
					fcRule();
					}
					break;
				default:
					throw new NoViableAltException(this);
				}
				}
				setState(42);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			setState(43);
			match(CLOSE_CURLY);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class RepeatsClauseContext extends ParserRuleContext {
		public TerminalNode REPEAT() { return getToken(RedmatchGrammar.REPEAT, 0); }
		public TerminalNode OPEN() { return getToken(RedmatchGrammar.OPEN, 0); }
		public List<TerminalNode> NUMBER() { return getTokens(RedmatchGrammar.NUMBER); }
		public TerminalNode NUMBER(int i) {
			return getToken(RedmatchGrammar.NUMBER, i);
		}
		public TerminalNode DOTDOT() { return getToken(RedmatchGrammar.DOTDOT, 0); }
		public TerminalNode COLON() { return getToken(RedmatchGrammar.COLON, 0); }
		public TerminalNode IDENTIFIER() { return getToken(RedmatchGrammar.IDENTIFIER, 0); }
		public TerminalNode CLOSE() { return getToken(RedmatchGrammar.CLOSE, 0); }
		public RepeatsClauseContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_repeatsClause; }
	}

	public final RepeatsClauseContext repeatsClause() throws RecognitionException {
		RepeatsClauseContext _localctx = new RepeatsClauseContext(_ctx, getState());
		enterRule(_localctx, 6, RULE_repeatsClause);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(45);
			match(REPEAT);
			setState(46);
			match(OPEN);
			setState(47);
			match(NUMBER);
			setState(48);
			match(DOTDOT);
			setState(49);
			match(NUMBER);
			setState(50);
			match(COLON);
			setState(51);
			match(IDENTIFIER);
			setState(52);
			match(CLOSE);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class ConditionContext extends ParserRuleContext {
		public TerminalNode NOT() { return getToken(RedmatchGrammar.NOT, 0); }
		public List<ConditionContext> condition() {
			return getRuleContexts(ConditionContext.class);
		}
		public ConditionContext condition(int i) {
			return getRuleContext(ConditionContext.class,i);
		}
		public TerminalNode TRUE() { return getToken(RedmatchGrammar.TRUE, 0); }
		public TerminalNode FALSE() { return getToken(RedmatchGrammar.FALSE, 0); }
		public TerminalNode OPEN() { return getToken(RedmatchGrammar.OPEN, 0); }
		public VariableIdentifierContext variableIdentifier() {
			return getRuleContext(VariableIdentifierContext.class,0);
		}
		public TerminalNode CLOSE() { return getToken(RedmatchGrammar.CLOSE, 0); }
		public TerminalNode NULL() { return getToken(RedmatchGrammar.NULL, 0); }
		public TerminalNode NOTNULL() { return getToken(RedmatchGrammar.NOTNULL, 0); }
		public TerminalNode VALUE() { return getToken(RedmatchGrammar.VALUE, 0); }
		public TerminalNode EQ() { return getToken(RedmatchGrammar.EQ, 0); }
		public TerminalNode NEQ() { return getToken(RedmatchGrammar.NEQ, 0); }
		public TerminalNode LT() { return getToken(RedmatchGrammar.LT, 0); }
		public TerminalNode GT() { return getToken(RedmatchGrammar.GT, 0); }
		public TerminalNode LTE() { return getToken(RedmatchGrammar.LTE, 0); }
		public TerminalNode GTE() { return getToken(RedmatchGrammar.GTE, 0); }
		public TerminalNode STRING() { return getToken(RedmatchGrammar.STRING, 0); }
		public TerminalNode NUMBER() { return getToken(RedmatchGrammar.NUMBER, 0); }
		public TerminalNode AND() { return getToken(RedmatchGrammar.AND, 0); }
		public TerminalNode OR() { return getToken(RedmatchGrammar.OR, 0); }
		public ConditionContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_condition; }
	}

	public final ConditionContext condition() throws RecognitionException {
		return condition(0);
	}

	private ConditionContext condition(int _p) throws RecognitionException {
		ParserRuleContext _parentctx = _ctx;
		int _parentState = getState();
		ConditionContext _localctx = new ConditionContext(_ctx, _parentState);
		ConditionContext _prevctx = _localctx;
		int _startState = 8;
		enterRecursionRule(_localctx, 8, RULE_condition, _p);
		int _la;
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			setState(74);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case NOT:
				{
				setState(55);
				match(NOT);
				setState(56);
				condition(7);
				}
				break;
			case TRUE:
			case FALSE:
				{
				setState(57);
				_la = _input.LA(1);
				if ( !(_la==TRUE || _la==FALSE) ) {
				_errHandler.recoverInline(this);
				}
				else {
					if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
					_errHandler.reportMatch(this);
					consume();
				}
				}
				break;
			case NULL:
			case NOTNULL:
				{
				setState(58);
				_la = _input.LA(1);
				if ( !(_la==NULL || _la==NOTNULL) ) {
				_errHandler.recoverInline(this);
				}
				else {
					if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
					_errHandler.reportMatch(this);
					consume();
				}
				setState(59);
				match(OPEN);
				setState(60);
				variableIdentifier();
				setState(61);
				match(CLOSE);
				}
				break;
			case VALUE:
				{
				setState(63);
				match(VALUE);
				setState(64);
				match(OPEN);
				setState(65);
				variableIdentifier();
				setState(66);
				match(CLOSE);
				setState(67);
				_la = _input.LA(1);
				if ( !((((_la) & ~0x3f) == 0 && ((1L << _la) & ((1L << EQ) | (1L << NEQ) | (1L << LT) | (1L << GT) | (1L << LTE) | (1L << GTE))) != 0)) ) {
				_errHandler.recoverInline(this);
				}
				else {
					if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
					_errHandler.reportMatch(this);
					consume();
				}
				setState(68);
				_la = _input.LA(1);
				if ( !(_la==STRING || _la==NUMBER) ) {
				_errHandler.recoverInline(this);
				}
				else {
					if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
					_errHandler.reportMatch(this);
					consume();
				}
				}
				break;
			case OPEN:
				{
				setState(70);
				match(OPEN);
				setState(71);
				condition(0);
				setState(72);
				match(CLOSE);
				}
				break;
			default:
				throw new NoViableAltException(this);
			}
			_ctx.stop = _input.LT(-1);
			setState(84);
			_errHandler.sync(this);
			_alt = getInterpreter().adaptivePredict(_input,7,_ctx);
			while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
				if ( _alt==1 ) {
					if ( _parseListeners!=null ) triggerExitRuleEvent();
					_prevctx = _localctx;
					{
					setState(82);
					_errHandler.sync(this);
					switch ( getInterpreter().adaptivePredict(_input,6,_ctx) ) {
					case 1:
						{
						_localctx = new ConditionContext(_parentctx, _parentState);
						pushNewRecursionContext(_localctx, _startState, RULE_condition);
						setState(76);
						if (!(precpred(_ctx, 6))) throw new FailedPredicateException(this, "precpred(_ctx, 6)");
						setState(77);
						match(AND);
						setState(78);
						condition(7);
						}
						break;
					case 2:
						{
						_localctx = new ConditionContext(_parentctx, _parentState);
						pushNewRecursionContext(_localctx, _startState, RULE_condition);
						setState(79);
						if (!(precpred(_ctx, 5))) throw new FailedPredicateException(this, "precpred(_ctx, 5)");
						setState(80);
						match(OR);
						setState(81);
						condition(6);
						}
						break;
					}
					} 
				}
				setState(86);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,7,_ctx);
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			unrollRecursionContexts(_parentctx);
		}
		return _localctx;
	}

	public static class ResourceContext extends ParserRuleContext {
		public TerminalNode IDENTIFIER() { return getToken(RedmatchGrammar.IDENTIFIER, 0); }
		public TerminalNode LT() { return getToken(RedmatchGrammar.LT, 0); }
		public VariableIdentifierContext variableIdentifier() {
			return getRuleContext(VariableIdentifierContext.class,0);
		}
		public TerminalNode GT() { return getToken(RedmatchGrammar.GT, 0); }
		public TerminalNode THEN() { return getToken(RedmatchGrammar.THEN, 0); }
		public List<AttributeContext> attribute() {
			return getRuleContexts(AttributeContext.class);
		}
		public AttributeContext attribute(int i) {
			return getRuleContext(AttributeContext.class,i);
		}
		public List<TerminalNode> EQ() { return getTokens(RedmatchGrammar.EQ); }
		public TerminalNode EQ(int i) {
			return getToken(RedmatchGrammar.EQ, i);
		}
		public List<ValueContext> value() {
			return getRuleContexts(ValueContext.class);
		}
		public ValueContext value(int i) {
			return getRuleContext(ValueContext.class,i);
		}
		public TerminalNode END() { return getToken(RedmatchGrammar.END, 0); }
		public List<TerminalNode> COMMA() { return getTokens(RedmatchGrammar.COMMA); }
		public TerminalNode COMMA(int i) {
			return getToken(RedmatchGrammar.COMMA, i);
		}
		public ResourceContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_resource; }
	}

	public final ResourceContext resource() throws RecognitionException {
		ResourceContext _localctx = new ResourceContext(_ctx, getState());
		enterRule(_localctx, 10, RULE_resource);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(87);
			match(IDENTIFIER);
			setState(88);
			match(LT);
			setState(89);
			variableIdentifier();
			setState(90);
			match(GT);
			setState(91);
			match(THEN);
			setState(92);
			attribute();
			setState(93);
			match(EQ);
			setState(94);
			value();
			setState(102);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==COMMA) {
				{
				{
				setState(95);
				match(COMMA);
				setState(96);
				attribute();
				setState(97);
				match(EQ);
				setState(98);
				value();
				}
				}
				setState(104);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			setState(105);
			match(END);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class AttributeContext extends ParserRuleContext {
		public TerminalNode IDENTIFIER() { return getToken(RedmatchGrammar.IDENTIFIER, 0); }
		public TerminalNode OPEN_SQ() { return getToken(RedmatchGrammar.OPEN_SQ, 0); }
		public TerminalNode NUMBER() { return getToken(RedmatchGrammar.NUMBER, 0); }
		public TerminalNode CLOSE_SQ() { return getToken(RedmatchGrammar.CLOSE_SQ, 0); }
		public TerminalNode DOT() { return getToken(RedmatchGrammar.DOT, 0); }
		public AttributeContext attribute() {
			return getRuleContext(AttributeContext.class,0);
		}
		public AttributeContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_attribute; }
	}

	public final AttributeContext attribute() throws RecognitionException {
		AttributeContext _localctx = new AttributeContext(_ctx, getState());
		enterRule(_localctx, 12, RULE_attribute);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(107);
			match(IDENTIFIER);
			setState(111);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==OPEN_SQ) {
				{
				setState(108);
				match(OPEN_SQ);
				setState(109);
				match(NUMBER);
				setState(110);
				match(CLOSE_SQ);
				}
			}

			setState(115);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==DOT) {
				{
				setState(113);
				match(DOT);
				setState(114);
				attribute();
				}
			}

			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class ValueContext extends ParserRuleContext {
		public TerminalNode TRUE() { return getToken(RedmatchGrammar.TRUE, 0); }
		public TerminalNode FALSE() { return getToken(RedmatchGrammar.FALSE, 0); }
		public TerminalNode STRING() { return getToken(RedmatchGrammar.STRING, 0); }
		public TerminalNode NUMBER() { return getToken(RedmatchGrammar.NUMBER, 0); }
		public ReferenceContext reference() {
			return getRuleContext(ReferenceContext.class,0);
		}
		public TerminalNode CONCEPT_LITERAL() { return getToken(RedmatchGrammar.CONCEPT_LITERAL, 0); }
		public TerminalNode CONCEPT_VALUE() { return getToken(RedmatchGrammar.CONCEPT_VALUE, 0); }
		public TerminalNode CODE_LITERAL() { return getToken(RedmatchGrammar.CODE_LITERAL, 0); }
		public TerminalNode CODE_VALUE() { return getToken(RedmatchGrammar.CODE_VALUE, 0); }
		public TerminalNode OPEN() { return getToken(RedmatchGrammar.OPEN, 0); }
		public VariableIdentifierContext variableIdentifier() {
			return getRuleContext(VariableIdentifierContext.class,0);
		}
		public TerminalNode CLOSE() { return getToken(RedmatchGrammar.CLOSE, 0); }
		public TerminalNode CONCEPT() { return getToken(RedmatchGrammar.CONCEPT, 0); }
		public TerminalNode CONCEPT_SELECTED() { return getToken(RedmatchGrammar.CONCEPT_SELECTED, 0); }
		public TerminalNode CODE_SELECTED() { return getToken(RedmatchGrammar.CODE_SELECTED, 0); }
		public TerminalNode VALUE() { return getToken(RedmatchGrammar.VALUE, 0); }
		public ValueContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_value; }
	}

	public final ValueContext value() throws RecognitionException {
		ValueContext _localctx = new ValueContext(_ctx, getState());
		enterRule(_localctx, 14, RULE_value);
		int _la;
		try {
			setState(130);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case TRUE:
			case FALSE:
				enterOuterAlt(_localctx, 1);
				{
				setState(117);
				_la = _input.LA(1);
				if ( !(_la==TRUE || _la==FALSE) ) {
				_errHandler.recoverInline(this);
				}
				else {
					if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
					_errHandler.reportMatch(this);
					consume();
				}
				}
				break;
			case STRING:
				enterOuterAlt(_localctx, 2);
				{
				setState(118);
				match(STRING);
				}
				break;
			case NUMBER:
				enterOuterAlt(_localctx, 3);
				{
				setState(119);
				match(NUMBER);
				}
				break;
			case REF:
				enterOuterAlt(_localctx, 4);
				{
				setState(120);
				reference();
				}
				break;
			case CONCEPT_LITERAL:
				enterOuterAlt(_localctx, 5);
				{
				setState(121);
				match(CONCEPT_LITERAL);
				setState(122);
				match(CONCEPT_VALUE);
				}
				break;
			case CODE_LITERAL:
				enterOuterAlt(_localctx, 6);
				{
				setState(123);
				match(CODE_LITERAL);
				setState(124);
				match(CODE_VALUE);
				}
				break;
			case VALUE:
			case CONCEPT:
			case CONCEPT_SELECTED:
			case CODE_SELECTED:
				enterOuterAlt(_localctx, 7);
				{
				setState(125);
				_la = _input.LA(1);
				if ( !((((_la) & ~0x3f) == 0 && ((1L << _la) & ((1L << VALUE) | (1L << CONCEPT) | (1L << CONCEPT_SELECTED) | (1L << CODE_SELECTED))) != 0)) ) {
				_errHandler.recoverInline(this);
				}
				else {
					if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
					_errHandler.reportMatch(this);
					consume();
				}
				setState(126);
				match(OPEN);
				setState(127);
				variableIdentifier();
				setState(128);
				match(CLOSE);
				}
				break;
			default:
				throw new NoViableAltException(this);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class ReferenceContext extends ParserRuleContext {
		public TerminalNode REF() { return getToken(RedmatchGrammar.REF, 0); }
		public TerminalNode OPEN() { return getToken(RedmatchGrammar.OPEN, 0); }
		public TerminalNode IDENTIFIER() { return getToken(RedmatchGrammar.IDENTIFIER, 0); }
		public TerminalNode LT() { return getToken(RedmatchGrammar.LT, 0); }
		public VariableIdentifierContext variableIdentifier() {
			return getRuleContext(VariableIdentifierContext.class,0);
		}
		public TerminalNode GT() { return getToken(RedmatchGrammar.GT, 0); }
		public TerminalNode CLOSE() { return getToken(RedmatchGrammar.CLOSE, 0); }
		public ReferenceContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_reference; }
	}

	public final ReferenceContext reference() throws RecognitionException {
		ReferenceContext _localctx = new ReferenceContext(_ctx, getState());
		enterRule(_localctx, 16, RULE_reference);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(132);
			match(REF);
			setState(133);
			match(OPEN);
			setState(134);
			match(IDENTIFIER);
			setState(135);
			match(LT);
			setState(136);
			variableIdentifier();
			setState(137);
			match(GT);
			setState(138);
			match(CLOSE);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class VariableIdentifierContext extends ParserRuleContext {
		public List<TerminalNode> IDENTIFIER() { return getTokens(RedmatchGrammar.IDENTIFIER); }
		public TerminalNode IDENTIFIER(int i) {
			return getToken(RedmatchGrammar.IDENTIFIER, i);
		}
		public TerminalNode OPEN_CURLY_DOLLAR() { return getToken(RedmatchGrammar.OPEN_CURLY_DOLLAR, 0); }
		public TerminalNode CLOSE_CURLY() { return getToken(RedmatchGrammar.CLOSE_CURLY, 0); }
		public VariableIdentifierContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_variableIdentifier; }
	}

	public final VariableIdentifierContext variableIdentifier() throws RecognitionException {
		VariableIdentifierContext _localctx = new VariableIdentifierContext(_ctx, getState());
		enterRule(_localctx, 18, RULE_variableIdentifier);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(140);
			match(IDENTIFIER);
			setState(144);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==OPEN_CURLY_DOLLAR) {
				{
				setState(141);
				match(OPEN_CURLY_DOLLAR);
				setState(142);
				match(IDENTIFIER);
				setState(143);
				match(CLOSE_CURLY);
				}
			}

			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public boolean sempred(RuleContext _localctx, int ruleIndex, int predIndex) {
		switch (ruleIndex) {
		case 4:
			return condition_sempred((ConditionContext)_localctx, predIndex);
		}
		return true;
	}
	private boolean condition_sempred(ConditionContext _localctx, int predIndex) {
		switch (predIndex) {
		case 0:
			return precpred(_ctx, 6);
		case 1:
			return precpred(_ctx, 5);
		}
		return true;
	}

	public static final String _serializedATN =
		"\3\u608b\ua72a\u8133\ub9ed\u417c\u3be7\u7786\u5964\3\60\u0095\4\2\t\2"+
		"\4\3\t\3\4\4\t\4\4\5\t\5\4\6\t\6\4\7\t\7\4\b\t\b\4\t\t\t\4\n\t\n\4\13"+
		"\t\13\3\2\7\2\30\n\2\f\2\16\2\33\13\2\3\3\5\3\36\n\3\3\3\3\3\3\3\3\3\5"+
		"\3$\n\3\3\4\3\4\3\4\7\4)\n\4\f\4\16\4,\13\4\3\4\3\4\3\5\3\5\3\5\3\5\3"+
		"\5\3\5\3\5\3\5\3\5\3\6\3\6\3\6\3\6\3\6\3\6\3\6\3\6\3\6\3\6\3\6\3\6\3\6"+
		"\3\6\3\6\3\6\3\6\3\6\3\6\3\6\5\6M\n\6\3\6\3\6\3\6\3\6\3\6\3\6\7\6U\n\6"+
		"\f\6\16\6X\13\6\3\7\3\7\3\7\3\7\3\7\3\7\3\7\3\7\3\7\3\7\3\7\3\7\3\7\7"+
		"\7g\n\7\f\7\16\7j\13\7\3\7\3\7\3\b\3\b\3\b\3\b\5\br\n\b\3\b\3\b\5\bv\n"+
		"\b\3\t\3\t\3\t\3\t\3\t\3\t\3\t\3\t\3\t\3\t\3\t\3\t\3\t\5\t\u0085\n\t\3"+
		"\n\3\n\3\n\3\n\3\n\3\n\3\n\3\n\3\13\3\13\3\13\3\13\5\13\u0093\n\13\3\13"+
		"\2\3\n\f\2\4\6\b\n\f\16\20\22\24\2\7\3\2\n\13\3\2\f\r\3\2\17\24\3\2\'"+
		"(\4\2\16\16\33\35\2\u009f\2\31\3\2\2\2\4\35\3\2\2\2\6%\3\2\2\2\b/\3\2"+
		"\2\2\nL\3\2\2\2\fY\3\2\2\2\16m\3\2\2\2\20\u0084\3\2\2\2\22\u0086\3\2\2"+
		"\2\24\u008e\3\2\2\2\26\30\5\4\3\2\27\26\3\2\2\2\30\33\3\2\2\2\31\27\3"+
		"\2\2\2\31\32\3\2\2\2\32\3\3\2\2\2\33\31\3\2\2\2\34\36\5\b\5\2\35\34\3"+
		"\2\2\2\35\36\3\2\2\2\36\37\3\2\2\2\37 \5\n\6\2 #\5\6\4\2!\"\7\3\2\2\""+
		"$\5\6\4\2#!\3\2\2\2#$\3\2\2\2$\5\3\2\2\2%*\7!\2\2&)\5\f\7\2\')\5\4\3\2"+
		"(&\3\2\2\2(\'\3\2\2\2),\3\2\2\2*(\3\2\2\2*+\3\2\2\2+-\3\2\2\2,*\3\2\2"+
		"\2-.\7 \2\2.\7\3\2\2\2/\60\7\4\2\2\60\61\7\5\2\2\61\62\7(\2\2\62\63\7"+
		"\"\2\2\63\64\7(\2\2\64\65\7#\2\2\65\66\7&\2\2\66\67\7\6\2\2\67\t\3\2\2"+
		"\289\b\6\1\29:\7\7\2\2:M\5\n\6\t;M\t\2\2\2<=\t\3\2\2=>\7\5\2\2>?\5\24"+
		"\13\2?@\7\6\2\2@M\3\2\2\2AB\7\16\2\2BC\7\5\2\2CD\5\24\13\2DE\7\6\2\2E"+
		"F\t\4\2\2FG\t\5\2\2GM\3\2\2\2HI\7\5\2\2IJ\5\n\6\2JK\7\6\2\2KM\3\2\2\2"+
		"L8\3\2\2\2L;\3\2\2\2L<\3\2\2\2LA\3\2\2\2LH\3\2\2\2MV\3\2\2\2NO\f\b\2\2"+
		"OP\7\b\2\2PU\5\n\6\tQR\f\7\2\2RS\7\t\2\2SU\5\n\6\bTN\3\2\2\2TQ\3\2\2\2"+
		"UX\3\2\2\2VT\3\2\2\2VW\3\2\2\2W\13\3\2\2\2XV\3\2\2\2YZ\7&\2\2Z[\7\21\2"+
		"\2[\\\5\24\13\2\\]\7\22\2\2]^\7\25\2\2^_\5\16\b\2_`\7\17\2\2`h\5\20\t"+
		"\2ab\7\26\2\2bc\5\16\b\2cd\7\17\2\2de\5\20\t\2eg\3\2\2\2fa\3\2\2\2gj\3"+
		"\2\2\2hf\3\2\2\2hi\3\2\2\2ik\3\2\2\2jh\3\2\2\2kl\7\27\2\2l\r\3\2\2\2m"+
		"q\7&\2\2no\7\30\2\2op\7(\2\2pr\7\31\2\2qn\3\2\2\2qr\3\2\2\2ru\3\2\2\2"+
		"st\7\32\2\2tv\5\16\b\2us\3\2\2\2uv\3\2\2\2v\17\3\2\2\2w\u0085\t\2\2\2"+
		"x\u0085\7\'\2\2y\u0085\7(\2\2z\u0085\5\22\n\2{|\7$\2\2|\u0085\7/\2\2}"+
		"~\7%\2\2~\u0085\7\60\2\2\177\u0080\t\6\2\2\u0080\u0081\7\5\2\2\u0081\u0082"+
		"\5\24\13\2\u0082\u0083\7\6\2\2\u0083\u0085\3\2\2\2\u0084w\3\2\2\2\u0084"+
		"x\3\2\2\2\u0084y\3\2\2\2\u0084z\3\2\2\2\u0084{\3\2\2\2\u0084}\3\2\2\2"+
		"\u0084\177\3\2\2\2\u0085\21\3\2\2\2\u0086\u0087\7\36\2\2\u0087\u0088\7"+
		"\5\2\2\u0088\u0089\7&\2\2\u0089\u008a\7\21\2\2\u008a\u008b\5\24\13\2\u008b"+
		"\u008c\7\22\2\2\u008c\u008d\7\6\2\2\u008d\23\3\2\2\2\u008e\u0092\7&\2"+
		"\2\u008f\u0090\7\37\2\2\u0090\u0091\7&\2\2\u0091\u0093\7 \2\2\u0092\u008f"+
		"\3\2\2\2\u0092\u0093\3\2\2\2\u0093\25\3\2\2\2\17\31\35#(*LTVhqu\u0084"+
		"\u0092";
	public static final ATN _ATN =
		new ATNDeserializer().deserialize(_serializedATN.toCharArray());
	static {
		_decisionToDFA = new DFA[_ATN.getNumberOfDecisions()];
		for (int i = 0; i < _ATN.getNumberOfDecisions(); i++) {
			_decisionToDFA[i] = new DFA(_ATN.getDecisionState(i), i);
		}
	}
}