// Generated from src/grammar/au/csiro/redmatch/grammar/RedmatchGrammar.g4 by ANTLR 4.7.3-SNAPSHOT


import { ATN } from "antlr4ts/atn/ATN";
import { ATNDeserializer } from "antlr4ts/atn/ATNDeserializer";
import { FailedPredicateException } from "antlr4ts/FailedPredicateException";
//import { NotNull } from "antlr4ts/Decorators";
import { NoViableAltException } from "antlr4ts/NoViableAltException";
//import { Override } from "antlr4ts/Decorators";
import { Parser } from "antlr4ts/Parser";
import { ParserRuleContext } from "antlr4ts/ParserRuleContext";
import { ParserATNSimulator } from "antlr4ts/atn/ParserATNSimulator";
//import { ParseTreeListener } from "antlr4ts/tree/ParseTreeListener";
//import { ParseTreeVisitor } from "antlr4ts/tree/ParseTreeVisitor";
import { RecognitionException } from "antlr4ts/RecognitionException";
import { RuleContext } from "antlr4ts/RuleContext";
//import { RuleVersion } from "antlr4ts/RuleVersion";
import { TerminalNode } from "antlr4ts/tree/TerminalNode";
import { Token } from "antlr4ts/Token";
import { TokenStream } from "antlr4ts/TokenStream";
import { Vocabulary } from "antlr4ts/Vocabulary";
import { VocabularyImpl } from "antlr4ts/VocabularyImpl";

import * as Utils from "antlr4ts/misc/Utils";

import { RedmatchGrammarListener } from "./RedmatchGrammarListener";
import { RedmatchGrammarVisitor } from "./RedmatchGrammarVisitor";


export class RedmatchGrammar extends Parser {
	public static readonly ELSE = 1;
	public static readonly REPEAT = 2;
	public static readonly OPEN = 3;
	public static readonly CLOSE = 4;
	public static readonly NOT = 5;
	public static readonly AND = 6;
	public static readonly OR = 7;
	public static readonly TRUE = 8;
	public static readonly FALSE = 9;
	public static readonly NULL = 10;
	public static readonly NOTNULL = 11;
	public static readonly VALUE = 12;
	public static readonly EQ = 13;
	public static readonly NEQ = 14;
	public static readonly LT = 15;
	public static readonly GT = 16;
	public static readonly LTE = 17;
	public static readonly GTE = 18;
	public static readonly THEN = 19;
	public static readonly COMMA = 20;
	public static readonly END = 21;
	public static readonly OPEN_SQ = 22;
	public static readonly CLOSE_SQ = 23;
	public static readonly DOT = 24;
	public static readonly CONCEPT = 25;
	public static readonly CONCEPT_SELECTED = 26;
	public static readonly CODE_SELECTED = 27;
	public static readonly REF = 28;
	public static readonly OPEN_CURLY_DOLLAR = 29;
	public static readonly CLOSE_CURLY = 30;
	public static readonly OPEN_CURLY = 31;
	public static readonly DOTDOT = 32;
	public static readonly COLON = 33;
	public static readonly CONCEPT_LITERAL = 34;
	public static readonly CODE_LITERAL = 35;
	public static readonly IDENTIFIER = 36;
	public static readonly STRING = 37;
	public static readonly NUMBER = 38;
	public static readonly COMMENT = 39;
	public static readonly LINE_COMMENT = 40;
	public static readonly WS = 41;
	public static readonly DATE = 42;
	public static readonly DATETIME = 43;
	public static readonly TIME = 44;
	public static readonly CONCEPT_VALUE = 45;
	public static readonly CODE_VALUE = 46;
	public static readonly RULE_document = 0;
	public static readonly RULE_fcRule = 1;
	public static readonly RULE_fcBody = 2;
	public static readonly RULE_repeatsClause = 3;
	public static readonly RULE_condition = 4;
	public static readonly RULE_resource = 5;
	public static readonly RULE_attribute = 6;
	public static readonly RULE_value = 7;
	public static readonly RULE_reference = 8;
	public static readonly RULE_variableIdentifier = 9;
	// tslint:disable:no-trailing-whitespace
	public static readonly ruleNames: string[] = [
		"document", "fcRule", "fcBody", "repeatsClause", "condition", "resource", 
		"attribute", "value", "reference", "variableIdentifier",
	];

	private static readonly _LITERAL_NAMES: Array<string | undefined> = [
		undefined, "'ELSE'", "'REPEAT'", "'('", "')'", "'^'", "'&'", "'|'", "'TRUE'", 
		"'FALSE'", "'NULL'", "'NOTNULL'", "'VALUE'", "'='", "'!='", "'<'", "'>'", 
		"'<='", "'>='", "'->'", "','", "';'", "'['", "']'", "'.'", "'CONCEPT'", 
		"'CONCEPT_SELECTED'", "'CODE_SELECTED'", "'REF'", "'${'", "'}'", "'{'", 
		"'..'", "':'", "'CONCEPT_LITERAL'", "'CODE_LITERAL'",
	];
	private static readonly _SYMBOLIC_NAMES: Array<string | undefined> = [
		undefined, "ELSE", "REPEAT", "OPEN", "CLOSE", "NOT", "AND", "OR", "TRUE", 
		"FALSE", "NULL", "NOTNULL", "VALUE", "EQ", "NEQ", "LT", "GT", "LTE", "GTE", 
		"THEN", "COMMA", "END", "OPEN_SQ", "CLOSE_SQ", "DOT", "CONCEPT", "CONCEPT_SELECTED", 
		"CODE_SELECTED", "REF", "OPEN_CURLY_DOLLAR", "CLOSE_CURLY", "OPEN_CURLY", 
		"DOTDOT", "COLON", "CONCEPT_LITERAL", "CODE_LITERAL", "IDENTIFIER", "STRING", 
		"NUMBER", "COMMENT", "LINE_COMMENT", "WS", "DATE", "DATETIME", "TIME", 
		"CONCEPT_VALUE", "CODE_VALUE",
	];
	public static readonly VOCABULARY: Vocabulary = new VocabularyImpl(RedmatchGrammar._LITERAL_NAMES, RedmatchGrammar._SYMBOLIC_NAMES, []);

	// @Override
	// @NotNull
	public get vocabulary(): Vocabulary {
		return RedmatchGrammar.VOCABULARY;
	}
	// tslint:enable:no-trailing-whitespace

	// @Override
	public get grammarFileName(): string { return "RedmatchGrammar.g4"; }

	// @Override
	public get ruleNames(): string[] { return RedmatchGrammar.ruleNames; }

	// @Override
	public get serializedATN(): string { return RedmatchGrammar._serializedATN; }

	constructor(input: TokenStream) {
		super(input);
		this._interp = new ParserATNSimulator(RedmatchGrammar._ATN, this);
	}
	// @RuleVersion(0)
	public document(): DocumentContext {
		let _localctx: DocumentContext = new DocumentContext(this._ctx, this.state);
		this.enterRule(_localctx, 0, RedmatchGrammar.RULE_document);
		let _la: number;
		try {
			this.enterOuterAlt(_localctx, 1);
			{
			this.state = 23;
			this._errHandler.sync(this);
			_la = this._input.LA(1);
			while ((((_la) & ~0x1F) === 0 && ((1 << _la) & ((1 << RedmatchGrammar.REPEAT) | (1 << RedmatchGrammar.OPEN) | (1 << RedmatchGrammar.NOT) | (1 << RedmatchGrammar.TRUE) | (1 << RedmatchGrammar.FALSE) | (1 << RedmatchGrammar.NULL) | (1 << RedmatchGrammar.NOTNULL) | (1 << RedmatchGrammar.VALUE))) !== 0)) {
				{
				{
				this.state = 20;
				this.fcRule();
				}
				}
				this.state = 25;
				this._errHandler.sync(this);
				_la = this._input.LA(1);
			}
			}
		}
		catch (re) {
			if (re instanceof RecognitionException) {
				_localctx.exception = re;
				this._errHandler.reportError(this, re);
				this._errHandler.recover(this, re);
			} else {
				throw re;
			}
		}
		finally {
			this.exitRule();
		}
		return _localctx;
	}
	// @RuleVersion(0)
	public fcRule(): FcRuleContext {
		let _localctx: FcRuleContext = new FcRuleContext(this._ctx, this.state);
		this.enterRule(_localctx, 2, RedmatchGrammar.RULE_fcRule);
		let _la: number;
		try {
			this.enterOuterAlt(_localctx, 1);
			{
			this.state = 27;
			this._errHandler.sync(this);
			_la = this._input.LA(1);
			if (_la === RedmatchGrammar.REPEAT) {
				{
				this.state = 26;
				this.repeatsClause();
				}
			}

			this.state = 29;
			this.condition(0);
			this.state = 30;
			this.fcBody();
			this.state = 33;
			this._errHandler.sync(this);
			_la = this._input.LA(1);
			if (_la === RedmatchGrammar.ELSE) {
				{
				this.state = 31;
				this.match(RedmatchGrammar.ELSE);
				this.state = 32;
				this.fcBody();
				}
			}

			}
		}
		catch (re) {
			if (re instanceof RecognitionException) {
				_localctx.exception = re;
				this._errHandler.reportError(this, re);
				this._errHandler.recover(this, re);
			} else {
				throw re;
			}
		}
		finally {
			this.exitRule();
		}
		return _localctx;
	}
	// @RuleVersion(0)
	public fcBody(): FcBodyContext {
		let _localctx: FcBodyContext = new FcBodyContext(this._ctx, this.state);
		this.enterRule(_localctx, 4, RedmatchGrammar.RULE_fcBody);
		let _la: number;
		try {
			this.enterOuterAlt(_localctx, 1);
			{
			this.state = 35;
			this.match(RedmatchGrammar.OPEN_CURLY);
			this.state = 40;
			this._errHandler.sync(this);
			_la = this._input.LA(1);
			while ((((_la) & ~0x1F) === 0 && ((1 << _la) & ((1 << RedmatchGrammar.REPEAT) | (1 << RedmatchGrammar.OPEN) | (1 << RedmatchGrammar.NOT) | (1 << RedmatchGrammar.TRUE) | (1 << RedmatchGrammar.FALSE) | (1 << RedmatchGrammar.NULL) | (1 << RedmatchGrammar.NOTNULL) | (1 << RedmatchGrammar.VALUE))) !== 0) || _la === RedmatchGrammar.IDENTIFIER) {
				{
				this.state = 38;
				this._errHandler.sync(this);
				switch (this._input.LA(1)) {
				case RedmatchGrammar.IDENTIFIER:
					{
					this.state = 36;
					this.resource();
					}
					break;
				case RedmatchGrammar.REPEAT:
				case RedmatchGrammar.OPEN:
				case RedmatchGrammar.NOT:
				case RedmatchGrammar.TRUE:
				case RedmatchGrammar.FALSE:
				case RedmatchGrammar.NULL:
				case RedmatchGrammar.NOTNULL:
				case RedmatchGrammar.VALUE:
					{
					this.state = 37;
					this.fcRule();
					}
					break;
				default:
					throw new NoViableAltException(this);
				}
				}
				this.state = 42;
				this._errHandler.sync(this);
				_la = this._input.LA(1);
			}
			this.state = 43;
			this.match(RedmatchGrammar.CLOSE_CURLY);
			}
		}
		catch (re) {
			if (re instanceof RecognitionException) {
				_localctx.exception = re;
				this._errHandler.reportError(this, re);
				this._errHandler.recover(this, re);
			} else {
				throw re;
			}
		}
		finally {
			this.exitRule();
		}
		return _localctx;
	}
	// @RuleVersion(0)
	public repeatsClause(): RepeatsClauseContext {
		let _localctx: RepeatsClauseContext = new RepeatsClauseContext(this._ctx, this.state);
		this.enterRule(_localctx, 6, RedmatchGrammar.RULE_repeatsClause);
		try {
			this.enterOuterAlt(_localctx, 1);
			{
			this.state = 45;
			this.match(RedmatchGrammar.REPEAT);
			this.state = 46;
			this.match(RedmatchGrammar.OPEN);
			this.state = 47;
			this.match(RedmatchGrammar.NUMBER);
			this.state = 48;
			this.match(RedmatchGrammar.DOTDOT);
			this.state = 49;
			this.match(RedmatchGrammar.NUMBER);
			this.state = 50;
			this.match(RedmatchGrammar.COLON);
			this.state = 51;
			this.match(RedmatchGrammar.IDENTIFIER);
			this.state = 52;
			this.match(RedmatchGrammar.CLOSE);
			}
		}
		catch (re) {
			if (re instanceof RecognitionException) {
				_localctx.exception = re;
				this._errHandler.reportError(this, re);
				this._errHandler.recover(this, re);
			} else {
				throw re;
			}
		}
		finally {
			this.exitRule();
		}
		return _localctx;
	}

	public condition(): ConditionContext;
	public condition(_p: number): ConditionContext;
	// @RuleVersion(0)
	public condition(_p?: number): ConditionContext {
		if (_p === undefined) {
			_p = 0;
		}

		let _parentctx: ParserRuleContext = this._ctx;
		let _parentState: number = this.state;
		let _localctx: ConditionContext = new ConditionContext(this._ctx, _parentState);
		let _prevctx: ConditionContext = _localctx;
		let _startState: number = 8;
		this.enterRecursionRule(_localctx, 8, RedmatchGrammar.RULE_condition, _p);
		let _la: number;
		try {
			let _alt: number;
			this.enterOuterAlt(_localctx, 1);
			{
			this.state = 74;
			this._errHandler.sync(this);
			switch (this._input.LA(1)) {
			case RedmatchGrammar.NOT:
				{
				this.state = 55;
				this.match(RedmatchGrammar.NOT);
				this.state = 56;
				this.condition(7);
				}
				break;
			case RedmatchGrammar.TRUE:
			case RedmatchGrammar.FALSE:
				{
				this.state = 57;
				_la = this._input.LA(1);
				if (!(_la === RedmatchGrammar.TRUE || _la === RedmatchGrammar.FALSE)) {
				this._errHandler.recoverInline(this);
				} else {
					if (this._input.LA(1) === Token.EOF) {
						this.matchedEOF = true;
					}

					this._errHandler.reportMatch(this);
					this.consume();
				}
				}
				break;
			case RedmatchGrammar.NULL:
			case RedmatchGrammar.NOTNULL:
				{
				this.state = 58;
				_la = this._input.LA(1);
				if (!(_la === RedmatchGrammar.NULL || _la === RedmatchGrammar.NOTNULL)) {
				this._errHandler.recoverInline(this);
				} else {
					if (this._input.LA(1) === Token.EOF) {
						this.matchedEOF = true;
					}

					this._errHandler.reportMatch(this);
					this.consume();
				}
				this.state = 59;
				this.match(RedmatchGrammar.OPEN);
				this.state = 60;
				this.variableIdentifier();
				this.state = 61;
				this.match(RedmatchGrammar.CLOSE);
				}
				break;
			case RedmatchGrammar.VALUE:
				{
				this.state = 63;
				this.match(RedmatchGrammar.VALUE);
				this.state = 64;
				this.match(RedmatchGrammar.OPEN);
				this.state = 65;
				this.variableIdentifier();
				this.state = 66;
				this.match(RedmatchGrammar.CLOSE);
				this.state = 67;
				_la = this._input.LA(1);
				if (!((((_la) & ~0x1F) === 0 && ((1 << _la) & ((1 << RedmatchGrammar.EQ) | (1 << RedmatchGrammar.NEQ) | (1 << RedmatchGrammar.LT) | (1 << RedmatchGrammar.GT) | (1 << RedmatchGrammar.LTE) | (1 << RedmatchGrammar.GTE))) !== 0))) {
				this._errHandler.recoverInline(this);
				} else {
					if (this._input.LA(1) === Token.EOF) {
						this.matchedEOF = true;
					}

					this._errHandler.reportMatch(this);
					this.consume();
				}
				this.state = 68;
				_la = this._input.LA(1);
				if (!(_la === RedmatchGrammar.STRING || _la === RedmatchGrammar.NUMBER)) {
				this._errHandler.recoverInline(this);
				} else {
					if (this._input.LA(1) === Token.EOF) {
						this.matchedEOF = true;
					}

					this._errHandler.reportMatch(this);
					this.consume();
				}
				}
				break;
			case RedmatchGrammar.OPEN:
				{
				this.state = 70;
				this.match(RedmatchGrammar.OPEN);
				this.state = 71;
				this.condition(0);
				this.state = 72;
				this.match(RedmatchGrammar.CLOSE);
				}
				break;
			default:
				throw new NoViableAltException(this);
			}
			this._ctx._stop = this._input.tryLT(-1);
			this.state = 84;
			this._errHandler.sync(this);
			_alt = this.interpreter.adaptivePredict(this._input, 7, this._ctx);
			while (_alt !== 2 && _alt !== ATN.INVALID_ALT_NUMBER) {
				if (_alt === 1) {
					if (this._parseListeners != null) {
						this.triggerExitRuleEvent();
					}
					_prevctx = _localctx;
					{
					this.state = 82;
					this._errHandler.sync(this);
					switch ( this.interpreter.adaptivePredict(this._input, 6, this._ctx) ) {
					case 1:
						{
						_localctx = new ConditionContext(_parentctx, _parentState);
						this.pushNewRecursionContext(_localctx, _startState, RedmatchGrammar.RULE_condition);
						this.state = 76;
						if (!(this.precpred(this._ctx, 6))) {
							throw new FailedPredicateException(this, "this.precpred(this._ctx, 6)");
						}
						this.state = 77;
						this.match(RedmatchGrammar.AND);
						this.state = 78;
						this.condition(7);
						}
						break;

					case 2:
						{
						_localctx = new ConditionContext(_parentctx, _parentState);
						this.pushNewRecursionContext(_localctx, _startState, RedmatchGrammar.RULE_condition);
						this.state = 79;
						if (!(this.precpred(this._ctx, 5))) {
							throw new FailedPredicateException(this, "this.precpred(this._ctx, 5)");
						}
						this.state = 80;
						this.match(RedmatchGrammar.OR);
						this.state = 81;
						this.condition(6);
						}
						break;
					}
					}
				}
				this.state = 86;
				this._errHandler.sync(this);
				_alt = this.interpreter.adaptivePredict(this._input, 7, this._ctx);
			}
			}
		}
		catch (re) {
			if (re instanceof RecognitionException) {
				_localctx.exception = re;
				this._errHandler.reportError(this, re);
				this._errHandler.recover(this, re);
			} else {
				throw re;
			}
		}
		finally {
			this.unrollRecursionContexts(_parentctx);
		}
		return _localctx;
	}
	// @RuleVersion(0)
	public resource(): ResourceContext {
		let _localctx: ResourceContext = new ResourceContext(this._ctx, this.state);
		this.enterRule(_localctx, 10, RedmatchGrammar.RULE_resource);
		let _la: number;
		try {
			this.enterOuterAlt(_localctx, 1);
			{
			this.state = 87;
			this.match(RedmatchGrammar.IDENTIFIER);
			this.state = 88;
			this.match(RedmatchGrammar.LT);
			this.state = 89;
			this.variableIdentifier();
			this.state = 90;
			this.match(RedmatchGrammar.GT);
			this.state = 91;
			this.match(RedmatchGrammar.THEN);
			this.state = 92;
			this.attribute();
			this.state = 93;
			this.match(RedmatchGrammar.EQ);
			this.state = 94;
			this.value();
			this.state = 102;
			this._errHandler.sync(this);
			_la = this._input.LA(1);
			while (_la === RedmatchGrammar.COMMA) {
				{
				{
				this.state = 95;
				this.match(RedmatchGrammar.COMMA);
				this.state = 96;
				this.attribute();
				this.state = 97;
				this.match(RedmatchGrammar.EQ);
				this.state = 98;
				this.value();
				}
				}
				this.state = 104;
				this._errHandler.sync(this);
				_la = this._input.LA(1);
			}
			this.state = 105;
			this.match(RedmatchGrammar.END);
			}
		}
		catch (re) {
			if (re instanceof RecognitionException) {
				_localctx.exception = re;
				this._errHandler.reportError(this, re);
				this._errHandler.recover(this, re);
			} else {
				throw re;
			}
		}
		finally {
			this.exitRule();
		}
		return _localctx;
	}
	// @RuleVersion(0)
	public attribute(): AttributeContext {
		let _localctx: AttributeContext = new AttributeContext(this._ctx, this.state);
		this.enterRule(_localctx, 12, RedmatchGrammar.RULE_attribute);
		let _la: number;
		try {
			this.enterOuterAlt(_localctx, 1);
			{
			this.state = 107;
			this.match(RedmatchGrammar.IDENTIFIER);
			this.state = 111;
			this._errHandler.sync(this);
			_la = this._input.LA(1);
			if (_la === RedmatchGrammar.OPEN_SQ) {
				{
				this.state = 108;
				this.match(RedmatchGrammar.OPEN_SQ);
				this.state = 109;
				this.match(RedmatchGrammar.NUMBER);
				this.state = 110;
				this.match(RedmatchGrammar.CLOSE_SQ);
				}
			}

			this.state = 115;
			this._errHandler.sync(this);
			_la = this._input.LA(1);
			if (_la === RedmatchGrammar.DOT) {
				{
				this.state = 113;
				this.match(RedmatchGrammar.DOT);
				this.state = 114;
				this.attribute();
				}
			}

			}
		}
		catch (re) {
			if (re instanceof RecognitionException) {
				_localctx.exception = re;
				this._errHandler.reportError(this, re);
				this._errHandler.recover(this, re);
			} else {
				throw re;
			}
		}
		finally {
			this.exitRule();
		}
		return _localctx;
	}
	// @RuleVersion(0)
	public value(): ValueContext {
		let _localctx: ValueContext = new ValueContext(this._ctx, this.state);
		this.enterRule(_localctx, 14, RedmatchGrammar.RULE_value);
		let _la: number;
		try {
			this.state = 130;
			this._errHandler.sync(this);
			switch (this._input.LA(1)) {
			case RedmatchGrammar.TRUE:
			case RedmatchGrammar.FALSE:
				this.enterOuterAlt(_localctx, 1);
				{
				this.state = 117;
				_la = this._input.LA(1);
				if (!(_la === RedmatchGrammar.TRUE || _la === RedmatchGrammar.FALSE)) {
				this._errHandler.recoverInline(this);
				} else {
					if (this._input.LA(1) === Token.EOF) {
						this.matchedEOF = true;
					}

					this._errHandler.reportMatch(this);
					this.consume();
				}
				}
				break;
			case RedmatchGrammar.STRING:
				this.enterOuterAlt(_localctx, 2);
				{
				this.state = 118;
				this.match(RedmatchGrammar.STRING);
				}
				break;
			case RedmatchGrammar.NUMBER:
				this.enterOuterAlt(_localctx, 3);
				{
				this.state = 119;
				this.match(RedmatchGrammar.NUMBER);
				}
				break;
			case RedmatchGrammar.REF:
				this.enterOuterAlt(_localctx, 4);
				{
				this.state = 120;
				this.reference();
				}
				break;
			case RedmatchGrammar.CONCEPT_LITERAL:
				this.enterOuterAlt(_localctx, 5);
				{
				this.state = 121;
				this.match(RedmatchGrammar.CONCEPT_LITERAL);
				this.state = 122;
				this.match(RedmatchGrammar.CONCEPT_VALUE);
				}
				break;
			case RedmatchGrammar.CODE_LITERAL:
				this.enterOuterAlt(_localctx, 6);
				{
				this.state = 123;
				this.match(RedmatchGrammar.CODE_LITERAL);
				this.state = 124;
				this.match(RedmatchGrammar.CODE_VALUE);
				}
				break;
			case RedmatchGrammar.VALUE:
			case RedmatchGrammar.CONCEPT:
			case RedmatchGrammar.CONCEPT_SELECTED:
			case RedmatchGrammar.CODE_SELECTED:
				this.enterOuterAlt(_localctx, 7);
				{
				this.state = 125;
				_la = this._input.LA(1);
				if (!((((_la) & ~0x1F) === 0 && ((1 << _la) & ((1 << RedmatchGrammar.VALUE) | (1 << RedmatchGrammar.CONCEPT) | (1 << RedmatchGrammar.CONCEPT_SELECTED) | (1 << RedmatchGrammar.CODE_SELECTED))) !== 0))) {
				this._errHandler.recoverInline(this);
				} else {
					if (this._input.LA(1) === Token.EOF) {
						this.matchedEOF = true;
					}

					this._errHandler.reportMatch(this);
					this.consume();
				}
				this.state = 126;
				this.match(RedmatchGrammar.OPEN);
				this.state = 127;
				this.variableIdentifier();
				this.state = 128;
				this.match(RedmatchGrammar.CLOSE);
				}
				break;
			default:
				throw new NoViableAltException(this);
			}
		}
		catch (re) {
			if (re instanceof RecognitionException) {
				_localctx.exception = re;
				this._errHandler.reportError(this, re);
				this._errHandler.recover(this, re);
			} else {
				throw re;
			}
		}
		finally {
			this.exitRule();
		}
		return _localctx;
	}
	// @RuleVersion(0)
	public reference(): ReferenceContext {
		let _localctx: ReferenceContext = new ReferenceContext(this._ctx, this.state);
		this.enterRule(_localctx, 16, RedmatchGrammar.RULE_reference);
		try {
			this.enterOuterAlt(_localctx, 1);
			{
			this.state = 132;
			this.match(RedmatchGrammar.REF);
			this.state = 133;
			this.match(RedmatchGrammar.OPEN);
			this.state = 134;
			this.match(RedmatchGrammar.IDENTIFIER);
			this.state = 135;
			this.match(RedmatchGrammar.LT);
			this.state = 136;
			this.variableIdentifier();
			this.state = 137;
			this.match(RedmatchGrammar.GT);
			this.state = 138;
			this.match(RedmatchGrammar.CLOSE);
			}
		}
		catch (re) {
			if (re instanceof RecognitionException) {
				_localctx.exception = re;
				this._errHandler.reportError(this, re);
				this._errHandler.recover(this, re);
			} else {
				throw re;
			}
		}
		finally {
			this.exitRule();
		}
		return _localctx;
	}
	// @RuleVersion(0)
	public variableIdentifier(): VariableIdentifierContext {
		let _localctx: VariableIdentifierContext = new VariableIdentifierContext(this._ctx, this.state);
		this.enterRule(_localctx, 18, RedmatchGrammar.RULE_variableIdentifier);
		let _la: number;
		try {
			this.enterOuterAlt(_localctx, 1);
			{
			this.state = 140;
			this.match(RedmatchGrammar.IDENTIFIER);
			this.state = 144;
			this._errHandler.sync(this);
			_la = this._input.LA(1);
			if (_la === RedmatchGrammar.OPEN_CURLY_DOLLAR) {
				{
				this.state = 141;
				this.match(RedmatchGrammar.OPEN_CURLY_DOLLAR);
				this.state = 142;
				this.match(RedmatchGrammar.IDENTIFIER);
				this.state = 143;
				this.match(RedmatchGrammar.CLOSE_CURLY);
				}
			}

			}
		}
		catch (re) {
			if (re instanceof RecognitionException) {
				_localctx.exception = re;
				this._errHandler.reportError(this, re);
				this._errHandler.recover(this, re);
			} else {
				throw re;
			}
		}
		finally {
			this.exitRule();
		}
		return _localctx;
	}

	public sempred(_localctx: RuleContext, ruleIndex: number, predIndex: number): boolean {
		switch (ruleIndex) {
		case 4:
			return this.condition_sempred(_localctx as ConditionContext, predIndex);
		}
		return true;
	}
	private condition_sempred(_localctx: ConditionContext, predIndex: number): boolean {
		switch (predIndex) {
		case 0:
			return this.precpred(this._ctx, 6);

		case 1:
			return this.precpred(this._ctx, 5);
		}
		return true;
	}

	public static readonly _serializedATN: string =
		"\x03\uC91D\uCABA\u058D\uAFBA\u4F53\u0607\uEA8B\uC241\x030\x95\x04\x02" +
		"\t\x02\x04\x03\t\x03\x04\x04\t\x04\x04\x05\t\x05\x04\x06\t\x06\x04\x07" +
		"\t\x07\x04\b\t\b\x04\t\t\t\x04\n\t\n\x04\v\t\v\x03\x02\x07\x02\x18\n\x02" +
		"\f\x02\x0E\x02\x1B\v\x02\x03\x03\x05\x03\x1E\n\x03\x03\x03\x03\x03\x03" +
		"\x03\x03\x03\x05\x03$\n\x03\x03\x04\x03\x04\x03\x04\x07\x04)\n\x04\f\x04" +
		"\x0E\x04,\v\x04\x03\x04\x03\x04\x03\x05\x03\x05\x03\x05\x03\x05\x03\x05" +
		"\x03\x05\x03\x05\x03\x05\x03\x05\x03\x06\x03\x06\x03\x06\x03\x06\x03\x06" +
		"\x03\x06\x03\x06\x03\x06\x03\x06\x03\x06\x03\x06\x03\x06\x03\x06\x03\x06" +
		"\x03\x06\x03\x06\x03\x06\x03\x06\x03\x06\x03\x06\x05\x06M\n\x06\x03\x06" +
		"\x03\x06\x03\x06\x03\x06\x03\x06\x03\x06\x07\x06U\n\x06\f\x06\x0E\x06" +
		"X\v\x06\x03\x07\x03\x07\x03\x07\x03\x07\x03\x07\x03\x07\x03\x07\x03\x07" +
		"\x03\x07\x03\x07\x03\x07\x03\x07\x03\x07\x07\x07g\n\x07\f\x07\x0E\x07" +
		"j\v\x07\x03\x07\x03\x07\x03\b\x03\b\x03\b\x03\b\x05\br\n\b\x03\b\x03\b" +
		"\x05\bv\n\b\x03\t\x03\t\x03\t\x03\t\x03\t\x03\t\x03\t\x03\t\x03\t\x03" +
		"\t\x03\t\x03\t\x03\t\x05\t\x85\n\t\x03\n\x03\n\x03\n\x03\n\x03\n\x03\n" +
		"\x03\n\x03\n\x03\v\x03\v\x03\v\x03\v\x05\v\x93\n\v\x03\v\x02\x02\x03\n" +
		"\f\x02\x02\x04\x02\x06\x02\b\x02\n\x02\f\x02\x0E\x02\x10\x02\x12\x02\x14" +
		"\x02\x02\x07\x03\x02\n\v\x03\x02\f\r\x03\x02\x0F\x14\x03\x02\'(\x04\x02" +
		"\x0E\x0E\x1B\x1D\x02\x9F\x02\x19\x03\x02\x02\x02\x04\x1D\x03\x02\x02\x02" +
		"\x06%\x03\x02\x02\x02\b/\x03\x02\x02\x02\nL\x03\x02\x02\x02\fY\x03\x02" +
		"\x02\x02\x0Em\x03\x02\x02\x02\x10\x84\x03\x02\x02\x02\x12\x86\x03\x02" +
		"\x02\x02\x14\x8E\x03\x02\x02\x02\x16\x18\x05\x04\x03\x02\x17\x16\x03\x02" +
		"\x02\x02\x18\x1B\x03\x02\x02\x02\x19\x17\x03\x02\x02\x02\x19\x1A\x03\x02" +
		"\x02\x02\x1A\x03\x03\x02\x02\x02\x1B\x19\x03\x02\x02\x02\x1C\x1E\x05\b" +
		"\x05\x02\x1D\x1C\x03\x02\x02\x02\x1D\x1E\x03\x02\x02\x02\x1E\x1F\x03\x02" +
		"\x02\x02\x1F \x05\n\x06\x02 #\x05\x06\x04\x02!\"\x07\x03\x02\x02\"$\x05" +
		"\x06\x04\x02#!\x03\x02\x02\x02#$\x03\x02\x02\x02$\x05\x03\x02\x02\x02" +
		"%*\x07!\x02\x02&)\x05\f\x07\x02\')\x05\x04\x03\x02(&\x03\x02\x02\x02(" +
		"\'\x03\x02\x02\x02),\x03\x02\x02\x02*(\x03\x02\x02\x02*+\x03\x02\x02\x02" +
		"+-\x03\x02\x02\x02,*\x03\x02\x02\x02-.\x07 \x02\x02.\x07\x03\x02\x02\x02" +
		"/0\x07\x04\x02\x0201\x07\x05\x02\x0212\x07(\x02\x0223\x07\"\x02\x0234" +
		"\x07(\x02\x0245\x07#\x02\x0256\x07&\x02\x0267\x07\x06\x02\x027\t\x03\x02" +
		"\x02\x0289\b\x06\x01\x029:\x07\x07\x02\x02:M\x05\n\x06\t;M\t\x02\x02\x02" +
		"<=\t\x03\x02\x02=>\x07\x05\x02\x02>?\x05\x14\v\x02?@\x07\x06\x02\x02@" +
		"M\x03\x02\x02\x02AB\x07\x0E\x02\x02BC\x07\x05\x02\x02CD\x05\x14\v\x02" +
		"DE\x07\x06\x02\x02EF\t\x04\x02\x02FG\t\x05\x02\x02GM\x03\x02\x02\x02H" +
		"I\x07\x05\x02\x02IJ\x05\n\x06\x02JK\x07\x06\x02\x02KM\x03\x02\x02\x02" +
		"L8\x03\x02\x02\x02L;\x03\x02\x02\x02L<\x03\x02\x02\x02LA\x03\x02\x02\x02" +
		"LH\x03\x02\x02\x02MV\x03\x02\x02\x02NO\f\b\x02\x02OP\x07\b\x02\x02PU\x05" +
		"\n\x06\tQR\f\x07\x02\x02RS\x07\t\x02\x02SU\x05\n\x06\bTN\x03\x02\x02\x02" +
		"TQ\x03\x02\x02\x02UX\x03\x02\x02\x02VT\x03\x02\x02\x02VW\x03\x02\x02\x02" +
		"W\v\x03\x02\x02\x02XV\x03\x02\x02\x02YZ\x07&\x02\x02Z[\x07\x11\x02\x02" +
		"[\\\x05\x14\v\x02\\]\x07\x12\x02\x02]^\x07\x15\x02\x02^_\x05\x0E\b\x02" +
		"_`\x07\x0F\x02\x02`h\x05\x10\t\x02ab\x07\x16\x02\x02bc\x05\x0E\b\x02c" +
		"d\x07\x0F\x02\x02de\x05\x10\t\x02eg\x03\x02\x02\x02fa\x03\x02\x02\x02" +
		"gj\x03\x02\x02\x02hf\x03\x02\x02\x02hi\x03\x02\x02\x02ik\x03\x02\x02\x02" +
		"jh\x03\x02\x02\x02kl\x07\x17\x02\x02l\r\x03\x02\x02\x02mq\x07&\x02\x02" +
		"no\x07\x18\x02\x02op\x07(\x02\x02pr\x07\x19\x02\x02qn\x03\x02\x02\x02" +
		"qr\x03\x02\x02\x02ru\x03\x02\x02\x02st\x07\x1A\x02\x02tv\x05\x0E\b\x02" +
		"us\x03\x02\x02\x02uv\x03\x02\x02\x02v\x0F\x03\x02\x02\x02w\x85\t\x02\x02" +
		"\x02x\x85\x07\'\x02\x02y\x85\x07(\x02\x02z\x85\x05\x12\n\x02{|\x07$\x02" +
		"\x02|\x85\x07/\x02\x02}~\x07%\x02\x02~\x85\x070\x02\x02\x7F\x80\t\x06" +
		"\x02\x02\x80\x81\x07\x05\x02\x02\x81\x82\x05\x14\v\x02\x82\x83\x07\x06" +
		"\x02\x02\x83\x85\x03\x02\x02\x02\x84w\x03\x02\x02\x02\x84x\x03\x02\x02" +
		"\x02\x84y\x03\x02\x02\x02\x84z\x03\x02\x02\x02\x84{\x03\x02\x02\x02\x84" +
		"}\x03\x02\x02\x02\x84\x7F\x03\x02\x02\x02\x85\x11\x03\x02\x02\x02\x86" +
		"\x87\x07\x1E\x02\x02\x87\x88\x07\x05\x02\x02\x88\x89\x07&\x02\x02\x89" +
		"\x8A\x07\x11\x02\x02\x8A\x8B\x05\x14\v\x02\x8B\x8C\x07\x12\x02\x02\x8C" +
		"\x8D\x07\x06\x02\x02\x8D\x13\x03\x02\x02\x02\x8E\x92\x07&\x02\x02\x8F" +
		"\x90\x07\x1F\x02\x02\x90\x91\x07&\x02\x02\x91\x93\x07 \x02\x02\x92\x8F" +
		"\x03\x02\x02\x02\x92\x93\x03\x02\x02\x02\x93\x15\x03\x02\x02\x02\x0F\x19" +
		"\x1D#(*LTVhqu\x84\x92";
	public static __ATN: ATN;
	public static get _ATN(): ATN {
		if (!RedmatchGrammar.__ATN) {
			RedmatchGrammar.__ATN = new ATNDeserializer().deserialize(Utils.toCharArray(RedmatchGrammar._serializedATN));
		}

		return RedmatchGrammar.__ATN;
	}

}

export class DocumentContext extends ParserRuleContext {
	public fcRule(): FcRuleContext[];
	public fcRule(i: number): FcRuleContext;
	public fcRule(i?: number): FcRuleContext | FcRuleContext[] {
		if (i === undefined) {
			return this.getRuleContexts(FcRuleContext);
		} else {
			return this.getRuleContext(i, FcRuleContext);
		}
	}
	constructor(parent: ParserRuleContext | undefined, invokingState: number) {
		super(parent, invokingState);
	}
	// @Override
	public get ruleIndex(): number { return RedmatchGrammar.RULE_document; }
	// @Override
	public enterRule(listener: RedmatchGrammarListener): void {
		if (listener.enterDocument) {
			listener.enterDocument(this);
		}
	}
	// @Override
	public exitRule(listener: RedmatchGrammarListener): void {
		if (listener.exitDocument) {
			listener.exitDocument(this);
		}
	}
	// @Override
	public accept<Result>(visitor: RedmatchGrammarVisitor<Result>): Result {
		if (visitor.visitDocument) {
			return visitor.visitDocument(this);
		} else {
			return visitor.visitChildren(this);
		}
	}
}


export class FcRuleContext extends ParserRuleContext {
	public condition(): ConditionContext {
		return this.getRuleContext(0, ConditionContext);
	}
	public fcBody(): FcBodyContext[];
	public fcBody(i: number): FcBodyContext;
	public fcBody(i?: number): FcBodyContext | FcBodyContext[] {
		if (i === undefined) {
			return this.getRuleContexts(FcBodyContext);
		} else {
			return this.getRuleContext(i, FcBodyContext);
		}
	}
	public repeatsClause(): RepeatsClauseContext | undefined {
		return this.tryGetRuleContext(0, RepeatsClauseContext);
	}
	public ELSE(): TerminalNode | undefined { return this.tryGetToken(RedmatchGrammar.ELSE, 0); }
	constructor(parent: ParserRuleContext | undefined, invokingState: number) {
		super(parent, invokingState);
	}
	// @Override
	public get ruleIndex(): number { return RedmatchGrammar.RULE_fcRule; }
	// @Override
	public enterRule(listener: RedmatchGrammarListener): void {
		if (listener.enterFcRule) {
			listener.enterFcRule(this);
		}
	}
	// @Override
	public exitRule(listener: RedmatchGrammarListener): void {
		if (listener.exitFcRule) {
			listener.exitFcRule(this);
		}
	}
	// @Override
	public accept<Result>(visitor: RedmatchGrammarVisitor<Result>): Result {
		if (visitor.visitFcRule) {
			return visitor.visitFcRule(this);
		} else {
			return visitor.visitChildren(this);
		}
	}
}


export class FcBodyContext extends ParserRuleContext {
	public OPEN_CURLY(): TerminalNode { return this.getToken(RedmatchGrammar.OPEN_CURLY, 0); }
	public CLOSE_CURLY(): TerminalNode { return this.getToken(RedmatchGrammar.CLOSE_CURLY, 0); }
	public resource(): ResourceContext[];
	public resource(i: number): ResourceContext;
	public resource(i?: number): ResourceContext | ResourceContext[] {
		if (i === undefined) {
			return this.getRuleContexts(ResourceContext);
		} else {
			return this.getRuleContext(i, ResourceContext);
		}
	}
	public fcRule(): FcRuleContext[];
	public fcRule(i: number): FcRuleContext;
	public fcRule(i?: number): FcRuleContext | FcRuleContext[] {
		if (i === undefined) {
			return this.getRuleContexts(FcRuleContext);
		} else {
			return this.getRuleContext(i, FcRuleContext);
		}
	}
	constructor(parent: ParserRuleContext | undefined, invokingState: number) {
		super(parent, invokingState);
	}
	// @Override
	public get ruleIndex(): number { return RedmatchGrammar.RULE_fcBody; }
	// @Override
	public enterRule(listener: RedmatchGrammarListener): void {
		if (listener.enterFcBody) {
			listener.enterFcBody(this);
		}
	}
	// @Override
	public exitRule(listener: RedmatchGrammarListener): void {
		if (listener.exitFcBody) {
			listener.exitFcBody(this);
		}
	}
	// @Override
	public accept<Result>(visitor: RedmatchGrammarVisitor<Result>): Result {
		if (visitor.visitFcBody) {
			return visitor.visitFcBody(this);
		} else {
			return visitor.visitChildren(this);
		}
	}
}


export class RepeatsClauseContext extends ParserRuleContext {
	public REPEAT(): TerminalNode { return this.getToken(RedmatchGrammar.REPEAT, 0); }
	public OPEN(): TerminalNode { return this.getToken(RedmatchGrammar.OPEN, 0); }
	public NUMBER(): TerminalNode[];
	public NUMBER(i: number): TerminalNode;
	public NUMBER(i?: number): TerminalNode | TerminalNode[] {
		if (i === undefined) {
			return this.getTokens(RedmatchGrammar.NUMBER);
		} else {
			return this.getToken(RedmatchGrammar.NUMBER, i);
		}
	}
	public DOTDOT(): TerminalNode { return this.getToken(RedmatchGrammar.DOTDOT, 0); }
	public COLON(): TerminalNode { return this.getToken(RedmatchGrammar.COLON, 0); }
	public IDENTIFIER(): TerminalNode { return this.getToken(RedmatchGrammar.IDENTIFIER, 0); }
	public CLOSE(): TerminalNode { return this.getToken(RedmatchGrammar.CLOSE, 0); }
	constructor(parent: ParserRuleContext | undefined, invokingState: number) {
		super(parent, invokingState);
	}
	// @Override
	public get ruleIndex(): number { return RedmatchGrammar.RULE_repeatsClause; }
	// @Override
	public enterRule(listener: RedmatchGrammarListener): void {
		if (listener.enterRepeatsClause) {
			listener.enterRepeatsClause(this);
		}
	}
	// @Override
	public exitRule(listener: RedmatchGrammarListener): void {
		if (listener.exitRepeatsClause) {
			listener.exitRepeatsClause(this);
		}
	}
	// @Override
	public accept<Result>(visitor: RedmatchGrammarVisitor<Result>): Result {
		if (visitor.visitRepeatsClause) {
			return visitor.visitRepeatsClause(this);
		} else {
			return visitor.visitChildren(this);
		}
	}
}


export class ConditionContext extends ParserRuleContext {
	public NOT(): TerminalNode | undefined { return this.tryGetToken(RedmatchGrammar.NOT, 0); }
	public condition(): ConditionContext[];
	public condition(i: number): ConditionContext;
	public condition(i?: number): ConditionContext | ConditionContext[] {
		if (i === undefined) {
			return this.getRuleContexts(ConditionContext);
		} else {
			return this.getRuleContext(i, ConditionContext);
		}
	}
	public AND(): TerminalNode | undefined { return this.tryGetToken(RedmatchGrammar.AND, 0); }
	public OR(): TerminalNode | undefined { return this.tryGetToken(RedmatchGrammar.OR, 0); }
	public TRUE(): TerminalNode | undefined { return this.tryGetToken(RedmatchGrammar.TRUE, 0); }
	public FALSE(): TerminalNode | undefined { return this.tryGetToken(RedmatchGrammar.FALSE, 0); }
	public OPEN(): TerminalNode | undefined { return this.tryGetToken(RedmatchGrammar.OPEN, 0); }
	public variableIdentifier(): VariableIdentifierContext | undefined {
		return this.tryGetRuleContext(0, VariableIdentifierContext);
	}
	public CLOSE(): TerminalNode | undefined { return this.tryGetToken(RedmatchGrammar.CLOSE, 0); }
	public NULL(): TerminalNode | undefined { return this.tryGetToken(RedmatchGrammar.NULL, 0); }
	public NOTNULL(): TerminalNode | undefined { return this.tryGetToken(RedmatchGrammar.NOTNULL, 0); }
	public VALUE(): TerminalNode | undefined { return this.tryGetToken(RedmatchGrammar.VALUE, 0); }
	public EQ(): TerminalNode | undefined { return this.tryGetToken(RedmatchGrammar.EQ, 0); }
	public NEQ(): TerminalNode | undefined { return this.tryGetToken(RedmatchGrammar.NEQ, 0); }
	public LT(): TerminalNode | undefined { return this.tryGetToken(RedmatchGrammar.LT, 0); }
	public GT(): TerminalNode | undefined { return this.tryGetToken(RedmatchGrammar.GT, 0); }
	public LTE(): TerminalNode | undefined { return this.tryGetToken(RedmatchGrammar.LTE, 0); }
	public GTE(): TerminalNode | undefined { return this.tryGetToken(RedmatchGrammar.GTE, 0); }
	public STRING(): TerminalNode | undefined { return this.tryGetToken(RedmatchGrammar.STRING, 0); }
	public NUMBER(): TerminalNode | undefined { return this.tryGetToken(RedmatchGrammar.NUMBER, 0); }
	constructor(parent: ParserRuleContext | undefined, invokingState: number) {
		super(parent, invokingState);
	}
	// @Override
	public get ruleIndex(): number { return RedmatchGrammar.RULE_condition; }
	// @Override
	public enterRule(listener: RedmatchGrammarListener): void {
		if (listener.enterCondition) {
			listener.enterCondition(this);
		}
	}
	// @Override
	public exitRule(listener: RedmatchGrammarListener): void {
		if (listener.exitCondition) {
			listener.exitCondition(this);
		}
	}
	// @Override
	public accept<Result>(visitor: RedmatchGrammarVisitor<Result>): Result {
		if (visitor.visitCondition) {
			return visitor.visitCondition(this);
		} else {
			return visitor.visitChildren(this);
		}
	}
}


export class ResourceContext extends ParserRuleContext {
	public IDENTIFIER(): TerminalNode { return this.getToken(RedmatchGrammar.IDENTIFIER, 0); }
	public LT(): TerminalNode { return this.getToken(RedmatchGrammar.LT, 0); }
	public variableIdentifier(): VariableIdentifierContext {
		return this.getRuleContext(0, VariableIdentifierContext);
	}
	public GT(): TerminalNode { return this.getToken(RedmatchGrammar.GT, 0); }
	public THEN(): TerminalNode { return this.getToken(RedmatchGrammar.THEN, 0); }
	public attribute(): AttributeContext[];
	public attribute(i: number): AttributeContext;
	public attribute(i?: number): AttributeContext | AttributeContext[] {
		if (i === undefined) {
			return this.getRuleContexts(AttributeContext);
		} else {
			return this.getRuleContext(i, AttributeContext);
		}
	}
	public EQ(): TerminalNode[];
	public EQ(i: number): TerminalNode;
	public EQ(i?: number): TerminalNode | TerminalNode[] {
		if (i === undefined) {
			return this.getTokens(RedmatchGrammar.EQ);
		} else {
			return this.getToken(RedmatchGrammar.EQ, i);
		}
	}
	public value(): ValueContext[];
	public value(i: number): ValueContext;
	public value(i?: number): ValueContext | ValueContext[] {
		if (i === undefined) {
			return this.getRuleContexts(ValueContext);
		} else {
			return this.getRuleContext(i, ValueContext);
		}
	}
	public END(): TerminalNode { return this.getToken(RedmatchGrammar.END, 0); }
	public COMMA(): TerminalNode[];
	public COMMA(i: number): TerminalNode;
	public COMMA(i?: number): TerminalNode | TerminalNode[] {
		if (i === undefined) {
			return this.getTokens(RedmatchGrammar.COMMA);
		} else {
			return this.getToken(RedmatchGrammar.COMMA, i);
		}
	}
	constructor(parent: ParserRuleContext | undefined, invokingState: number) {
		super(parent, invokingState);
	}
	// @Override
	public get ruleIndex(): number { return RedmatchGrammar.RULE_resource; }
	// @Override
	public enterRule(listener: RedmatchGrammarListener): void {
		if (listener.enterResource) {
			listener.enterResource(this);
		}
	}
	// @Override
	public exitRule(listener: RedmatchGrammarListener): void {
		if (listener.exitResource) {
			listener.exitResource(this);
		}
	}
	// @Override
	public accept<Result>(visitor: RedmatchGrammarVisitor<Result>): Result {
		if (visitor.visitResource) {
			return visitor.visitResource(this);
		} else {
			return visitor.visitChildren(this);
		}
	}
}


export class AttributeContext extends ParserRuleContext {
	public IDENTIFIER(): TerminalNode { return this.getToken(RedmatchGrammar.IDENTIFIER, 0); }
	public OPEN_SQ(): TerminalNode | undefined { return this.tryGetToken(RedmatchGrammar.OPEN_SQ, 0); }
	public NUMBER(): TerminalNode | undefined { return this.tryGetToken(RedmatchGrammar.NUMBER, 0); }
	public CLOSE_SQ(): TerminalNode | undefined { return this.tryGetToken(RedmatchGrammar.CLOSE_SQ, 0); }
	public DOT(): TerminalNode | undefined { return this.tryGetToken(RedmatchGrammar.DOT, 0); }
	public attribute(): AttributeContext | undefined {
		return this.tryGetRuleContext(0, AttributeContext);
	}
	constructor(parent: ParserRuleContext | undefined, invokingState: number) {
		super(parent, invokingState);
	}
	// @Override
	public get ruleIndex(): number { return RedmatchGrammar.RULE_attribute; }
	// @Override
	public enterRule(listener: RedmatchGrammarListener): void {
		if (listener.enterAttribute) {
			listener.enterAttribute(this);
		}
	}
	// @Override
	public exitRule(listener: RedmatchGrammarListener): void {
		if (listener.exitAttribute) {
			listener.exitAttribute(this);
		}
	}
	// @Override
	public accept<Result>(visitor: RedmatchGrammarVisitor<Result>): Result {
		if (visitor.visitAttribute) {
			return visitor.visitAttribute(this);
		} else {
			return visitor.visitChildren(this);
		}
	}
}


export class ValueContext extends ParserRuleContext {
	public TRUE(): TerminalNode | undefined { return this.tryGetToken(RedmatchGrammar.TRUE, 0); }
	public FALSE(): TerminalNode | undefined { return this.tryGetToken(RedmatchGrammar.FALSE, 0); }
	public STRING(): TerminalNode | undefined { return this.tryGetToken(RedmatchGrammar.STRING, 0); }
	public NUMBER(): TerminalNode | undefined { return this.tryGetToken(RedmatchGrammar.NUMBER, 0); }
	public reference(): ReferenceContext | undefined {
		return this.tryGetRuleContext(0, ReferenceContext);
	}
	public CONCEPT_LITERAL(): TerminalNode | undefined { return this.tryGetToken(RedmatchGrammar.CONCEPT_LITERAL, 0); }
	public CONCEPT_VALUE(): TerminalNode | undefined { return this.tryGetToken(RedmatchGrammar.CONCEPT_VALUE, 0); }
	public CODE_LITERAL(): TerminalNode | undefined { return this.tryGetToken(RedmatchGrammar.CODE_LITERAL, 0); }
	public CODE_VALUE(): TerminalNode | undefined { return this.tryGetToken(RedmatchGrammar.CODE_VALUE, 0); }
	public OPEN(): TerminalNode | undefined { return this.tryGetToken(RedmatchGrammar.OPEN, 0); }
	public variableIdentifier(): VariableIdentifierContext | undefined {
		return this.tryGetRuleContext(0, VariableIdentifierContext);
	}
	public CLOSE(): TerminalNode | undefined { return this.tryGetToken(RedmatchGrammar.CLOSE, 0); }
	public CONCEPT(): TerminalNode | undefined { return this.tryGetToken(RedmatchGrammar.CONCEPT, 0); }
	public CONCEPT_SELECTED(): TerminalNode | undefined { return this.tryGetToken(RedmatchGrammar.CONCEPT_SELECTED, 0); }
	public CODE_SELECTED(): TerminalNode | undefined { return this.tryGetToken(RedmatchGrammar.CODE_SELECTED, 0); }
	public VALUE(): TerminalNode | undefined { return this.tryGetToken(RedmatchGrammar.VALUE, 0); }
	constructor(parent: ParserRuleContext | undefined, invokingState: number) {
		super(parent, invokingState);
	}
	// @Override
	public get ruleIndex(): number { return RedmatchGrammar.RULE_value; }
	// @Override
	public enterRule(listener: RedmatchGrammarListener): void {
		if (listener.enterValue) {
			listener.enterValue(this);
		}
	}
	// @Override
	public exitRule(listener: RedmatchGrammarListener): void {
		if (listener.exitValue) {
			listener.exitValue(this);
		}
	}
	// @Override
	public accept<Result>(visitor: RedmatchGrammarVisitor<Result>): Result {
		if (visitor.visitValue) {
			return visitor.visitValue(this);
		} else {
			return visitor.visitChildren(this);
		}
	}
}


export class ReferenceContext extends ParserRuleContext {
	public REF(): TerminalNode { return this.getToken(RedmatchGrammar.REF, 0); }
	public OPEN(): TerminalNode { return this.getToken(RedmatchGrammar.OPEN, 0); }
	public IDENTIFIER(): TerminalNode { return this.getToken(RedmatchGrammar.IDENTIFIER, 0); }
	public LT(): TerminalNode { return this.getToken(RedmatchGrammar.LT, 0); }
	public variableIdentifier(): VariableIdentifierContext {
		return this.getRuleContext(0, VariableIdentifierContext);
	}
	public GT(): TerminalNode { return this.getToken(RedmatchGrammar.GT, 0); }
	public CLOSE(): TerminalNode { return this.getToken(RedmatchGrammar.CLOSE, 0); }
	constructor(parent: ParserRuleContext | undefined, invokingState: number) {
		super(parent, invokingState);
	}
	// @Override
	public get ruleIndex(): number { return RedmatchGrammar.RULE_reference; }
	// @Override
	public enterRule(listener: RedmatchGrammarListener): void {
		if (listener.enterReference) {
			listener.enterReference(this);
		}
	}
	// @Override
	public exitRule(listener: RedmatchGrammarListener): void {
		if (listener.exitReference) {
			listener.exitReference(this);
		}
	}
	// @Override
	public accept<Result>(visitor: RedmatchGrammarVisitor<Result>): Result {
		if (visitor.visitReference) {
			return visitor.visitReference(this);
		} else {
			return visitor.visitChildren(this);
		}
	}
}


export class VariableIdentifierContext extends ParserRuleContext {
	public IDENTIFIER(): TerminalNode[];
	public IDENTIFIER(i: number): TerminalNode;
	public IDENTIFIER(i?: number): TerminalNode | TerminalNode[] {
		if (i === undefined) {
			return this.getTokens(RedmatchGrammar.IDENTIFIER);
		} else {
			return this.getToken(RedmatchGrammar.IDENTIFIER, i);
		}
	}
	public OPEN_CURLY_DOLLAR(): TerminalNode | undefined { return this.tryGetToken(RedmatchGrammar.OPEN_CURLY_DOLLAR, 0); }
	public CLOSE_CURLY(): TerminalNode | undefined { return this.tryGetToken(RedmatchGrammar.CLOSE_CURLY, 0); }
	constructor(parent: ParserRuleContext | undefined, invokingState: number) {
		super(parent, invokingState);
	}
	// @Override
	public get ruleIndex(): number { return RedmatchGrammar.RULE_variableIdentifier; }
	// @Override
	public enterRule(listener: RedmatchGrammarListener): void {
		if (listener.enterVariableIdentifier) {
			listener.enterVariableIdentifier(this);
		}
	}
	// @Override
	public exitRule(listener: RedmatchGrammarListener): void {
		if (listener.exitVariableIdentifier) {
			listener.exitVariableIdentifier(this);
		}
	}
	// @Override
	public accept<Result>(visitor: RedmatchGrammarVisitor<Result>): Result {
		if (visitor.visitVariableIdentifier) {
			return visitor.visitVariableIdentifier(this);
		} else {
			return visitor.visitChildren(this);
		}
	}
}


