// Generated from au/csiro/fhircap/grammar/FhircapGrammar.g4 by ANTLR 4.7.2
// jshint ignore: start
var antlr4 = require('antlr4/index');
var grammarFileName = "FhircapGrammar.g4";


var serializedATN = ["\u0003\u608b\ua72a\u8133\ub9ed\u417c\u3be7\u7786\u5964",
    "\u00032\u0098\u0004\u0002\t\u0002\u0004\u0003\t\u0003\u0004\u0004\t",
    "\u0004\u0004\u0005\t\u0005\u0004\u0006\t\u0006\u0004\u0007\t\u0007\u0004",
    "\b\t\b\u0004\t\t\t\u0004\n\t\n\u0004\u000b\t\u000b\u0003\u0002\u0007",
    "\u0002\u0018\n\u0002\f\u0002\u000e\u0002\u001b\u000b\u0002\u0003\u0003",
    "\u0005\u0003\u001e\n\u0003\u0003\u0003\u0003\u0003\u0003\u0003\u0003",
    "\u0003\u0005\u0003$\n\u0003\u0003\u0004\u0003\u0004\u0003\u0004\u0007",
    "\u0004)\n\u0004\f\u0004\u000e\u0004,\u000b\u0004\u0003\u0004\u0003\u0004",
    "\u0003\u0005\u0003\u0005\u0003\u0005\u0003\u0005\u0003\u0005\u0003\u0005",
    "\u0003\u0005\u0003\u0005\u0003\u0005\u0003\u0006\u0003\u0006\u0003\u0006",
    "\u0003\u0006\u0003\u0006\u0003\u0006\u0003\u0006\u0003\u0006\u0003\u0006",
    "\u0003\u0006\u0003\u0006\u0003\u0006\u0003\u0006\u0003\u0006\u0003\u0006",
    "\u0003\u0006\u0003\u0006\u0003\u0006\u0003\u0006\u0003\u0006\u0005\u0006",
    "M\n\u0006\u0003\u0006\u0003\u0006\u0003\u0006\u0003\u0006\u0003\u0006",
    "\u0007\u0006T\n\u0006\f\u0006\u000e\u0006W\u000b\u0006\u0003\u0007\u0003",
    "\u0007\u0003\u0007\u0003\u0007\u0003\u0007\u0003\u0007\u0003\u0007\u0003",
    "\u0007\u0003\u0007\u0003\u0007\u0003\u0007\u0003\u0007\u0003\u0007\u0007",
    "\u0007f\n\u0007\f\u0007\u000e\u0007i\u000b\u0007\u0003\u0007\u0003\u0007",
    "\u0003\b\u0003\b\u0003\b\u0003\b\u0005\bq\n\b\u0003\b\u0003\b\u0005",
    "\bu\n\b\u0003\t\u0003\t\u0003\t\u0003\t\u0003\t\u0003\t\u0003\t\u0003",
    "\t\u0003\t\u0003\t\u0003\t\u0003\t\u0003\t\u0003\t\u0003\t\u0003\t\u0003",
    "\t\u0005\t\u0088\n\t\u0003\n\u0003\n\u0003\n\u0003\n\u0003\n\u0003\n",
    "\u0003\n\u0003\n\u0003\u000b\u0003\u000b\u0003\u000b\u0003\u000b\u0005",
    "\u000b\u0096\n\u000b\u0003\u000b\u0002\u0003\n\f\u0002\u0004\u0006\b",
    "\n\f\u000e\u0010\u0012\u0014\u0002\u0007\u0003\u0002\n\u000b\u0003\u0002",
    "\f\r\u0003\u0002\u000f\u0014\u0003\u0002\'(\u0004\u0002\u000e\u000e",
    "\u001b\u001d\u0002\u00a2\u0002\u0019\u0003\u0002\u0002\u0002\u0004\u001d",
    "\u0003\u0002\u0002\u0002\u0006%\u0003\u0002\u0002\u0002\b/\u0003\u0002",
    "\u0002\u0002\nL\u0003\u0002\u0002\u0002\fX\u0003\u0002\u0002\u0002\u000e",
    "l\u0003\u0002\u0002\u0002\u0010\u0087\u0003\u0002\u0002\u0002\u0012",
    "\u0089\u0003\u0002\u0002\u0002\u0014\u0091\u0003\u0002\u0002\u0002\u0016",
    "\u0018\u0005\u0004\u0003\u0002\u0017\u0016\u0003\u0002\u0002\u0002\u0018",
    "\u001b\u0003\u0002\u0002\u0002\u0019\u0017\u0003\u0002\u0002\u0002\u0019",
    "\u001a\u0003\u0002\u0002\u0002\u001a\u0003\u0003\u0002\u0002\u0002\u001b",
    "\u0019\u0003\u0002\u0002\u0002\u001c\u001e\u0005\b\u0005\u0002\u001d",
    "\u001c\u0003\u0002\u0002\u0002\u001d\u001e\u0003\u0002\u0002\u0002\u001e",
    "\u001f\u0003\u0002\u0002\u0002\u001f \u0005\n\u0006\u0002 #\u0005\u0006",
    "\u0004\u0002!\"\u0007\u0003\u0002\u0002\"$\u0005\u0006\u0004\u0002#",
    "!\u0003\u0002\u0002\u0002#$\u0003\u0002\u0002\u0002$\u0005\u0003\u0002",
    "\u0002\u0002%*\u0007!\u0002\u0002&)\u0005\f\u0007\u0002\')\u0005\u0004",
    "\u0003\u0002(&\u0003\u0002\u0002\u0002(\'\u0003\u0002\u0002\u0002),",
    "\u0003\u0002\u0002\u0002*(\u0003\u0002\u0002\u0002*+\u0003\u0002\u0002",
    "\u0002+-\u0003\u0002\u0002\u0002,*\u0003\u0002\u0002\u0002-.\u0007 ",
    "\u0002\u0002.\u0007\u0003\u0002\u0002\u0002/0\u0007\u0004\u0002\u0002",
    "01\u0007\u0005\u0002\u000212\u0007(\u0002\u000223\u0007\"\u0002\u0002",
    "34\u0007(\u0002\u000245\u0007#\u0002\u000256\u0007&\u0002\u000267\u0007",
    "\u0006\u0002\u00027\t\u0003\u0002\u0002\u000289\b\u0006\u0001\u0002",
    "9:\u0007\u0007\u0002\u0002:M\u0005\n\u0006\t;M\t\u0002\u0002\u0002<",
    "=\t\u0003\u0002\u0002=>\u0007\u0005\u0002\u0002>?\u0005\u0014\u000b",
    "\u0002?@\u0007\u0006\u0002\u0002@M\u0003\u0002\u0002\u0002AB\u0007\u000e",
    "\u0002\u0002BC\u0007\u0005\u0002\u0002CD\u0005\u0014\u000b\u0002DE\u0007",
    "\u0006\u0002\u0002EF\t\u0004\u0002\u0002FG\t\u0005\u0002\u0002GM\u0003",
    "\u0002\u0002\u0002HI\u0007\u0005\u0002\u0002IJ\u0005\n\u0006\u0002J",
    "K\u0007\u0006\u0002\u0002KM\u0003\u0002\u0002\u0002L8\u0003\u0002\u0002",
    "\u0002L;\u0003\u0002\u0002\u0002L<\u0003\u0002\u0002\u0002LA\u0003\u0002",
    "\u0002\u0002LH\u0003\u0002\u0002\u0002MU\u0003\u0002\u0002\u0002NO\f",
    "\b\u0002\u0002OT\u0005\n\u0006\tPQ\f\u0007\u0002\u0002QR\u0007\t\u0002",
    "\u0002RT\u0005\n\u0006\bSN\u0003\u0002\u0002\u0002SP\u0003\u0002\u0002",
    "\u0002TW\u0003\u0002\u0002\u0002US\u0003\u0002\u0002\u0002UV\u0003\u0002",
    "\u0002\u0002V\u000b\u0003\u0002\u0002\u0002WU\u0003\u0002\u0002\u0002",
    "XY\u0007&\u0002\u0002YZ\u0007\u0011\u0002\u0002Z[\u0005\u0014\u000b",
    "\u0002[\\\u0007\u0012\u0002\u0002\\]\u0007\u0015\u0002\u0002]^\u0005",
    "\u000e\b\u0002^_\u0007\u000f\u0002\u0002_g\u0005\u0010\t\u0002`a\u0007",
    "\u0016\u0002\u0002ab\u0005\u000e\b\u0002bc\u0007\u000f\u0002\u0002c",
    "d\u0005\u0010\t\u0002df\u0003\u0002\u0002\u0002e`\u0003\u0002\u0002",
    "\u0002fi\u0003\u0002\u0002\u0002ge\u0003\u0002\u0002\u0002gh\u0003\u0002",
    "\u0002\u0002hj\u0003\u0002\u0002\u0002ig\u0003\u0002\u0002\u0002jk\u0007",
    "\u0017\u0002\u0002k\r\u0003\u0002\u0002\u0002lp\u0007&\u0002\u0002m",
    "n\u0007\u0018\u0002\u0002no\u0007(\u0002\u0002oq\u0007\u0019\u0002\u0002",
    "pm\u0003\u0002\u0002\u0002pq\u0003\u0002\u0002\u0002qt\u0003\u0002\u0002",
    "\u0002rs\u0007\u001a\u0002\u0002su\u0005\u000e\b\u0002tr\u0003\u0002",
    "\u0002\u0002tu\u0003\u0002\u0002\u0002u\u000f\u0003\u0002\u0002\u0002",
    "v\u0088\t\u0002\u0002\u0002w\u0088\u0007\'\u0002\u0002x\u0088\u0007",
    "(\u0002\u0002y\u0088\u0005\u0012\n\u0002z{\u0007$\u0002\u0002{|\u0007",
    "1\u0002\u0002|}\u00070\u0002\u0002}\u0088\u00072\u0002\u0002~\u007f",
    "\u0007%\u0002\u0002\u007f\u0080\u00071\u0002\u0002\u0080\u0081\u0007",
    "/\u0002\u0002\u0081\u0088\u00072\u0002\u0002\u0082\u0083\t\u0006\u0002",
    "\u0002\u0083\u0084\u0007\u0005\u0002\u0002\u0084\u0085\u0005\u0014\u000b",
    "\u0002\u0085\u0086\u0007\u0006\u0002\u0002\u0086\u0088\u0003\u0002\u0002",
    "\u0002\u0087v\u0003\u0002\u0002\u0002\u0087w\u0003\u0002\u0002\u0002",
    "\u0087x\u0003\u0002\u0002\u0002\u0087y\u0003\u0002\u0002\u0002\u0087",
    "z\u0003\u0002\u0002\u0002\u0087~\u0003\u0002\u0002\u0002\u0087\u0082",
    "\u0003\u0002\u0002\u0002\u0088\u0011\u0003\u0002\u0002\u0002\u0089\u008a",
    "\u0007\u001e\u0002\u0002\u008a\u008b\u0007\u0005\u0002\u0002\u008b\u008c",
    "\u0007&\u0002\u0002\u008c\u008d\u0007\u0011\u0002\u0002\u008d\u008e",
    "\u0005\u0014\u000b\u0002\u008e\u008f\u0007\u0012\u0002\u0002\u008f\u0090",
    "\u0007\u0006\u0002\u0002\u0090\u0013\u0003\u0002\u0002\u0002\u0091\u0095",
    "\u0007&\u0002\u0002\u0092\u0093\u0007\u001f\u0002\u0002\u0093\u0094",
    "\u0007&\u0002\u0002\u0094\u0096\u0007 \u0002\u0002\u0095\u0092\u0003",
    "\u0002\u0002\u0002\u0095\u0096\u0003\u0002\u0002\u0002\u0096\u0015\u0003",
    "\u0002\u0002\u0002\u000f\u0019\u001d#(*LSUgpt\u0087\u0095"].join("");


var atn = new antlr4.atn.ATNDeserializer().deserialize(serializedATN);

var decisionsToDFA = atn.decisionToState.map( function(ds, index) { return new antlr4.dfa.DFA(ds, index); });

var sharedContextCache = new antlr4.PredictionContextCache();

var literalNames = [ null, "'ELSE'", "'REPEAT'", null, null, "'^'", "'&'", 
                     "'|'", "'TRUE'", "'FALSE'", "'NULL'", "'NOTNULL'", 
                     "'VALUE'", "'='", "'!='", "'<'", "'>'", "'<='", "'>='", 
                     "'->'", "','", "';'", "'['", "']'", "'.'", "'CONCEPT'", 
                     "'CONCEPT_SELECTED'", "'CODE_SELECTED'", "'REF'", "'${'", 
                     "'}'", "'{'", "'..'", "':'", "'CONCEPT_LITERAL'", "'CODE_LITERAL'" ];

var symbolicNames = [ null, "ELSE", "REPEAT", "OPEN", "CLOSE", "NOT", "AND", 
                      "OR", "TRUE", "FALSE", "NULL", "NOTNULL", "VALUE", 
                      "EQ", "NEQ", "LT", "GT", "LTE", "GTE", "THEN", "COMMA", 
                      "END", "OPEN_SQ", "CLOSE_SQ", "DOT", "CONCEPT", "CONCEPT_SELECTED", 
                      "CODE_SELECTED", "REF", "OPEN_CURLY_DOLLAR", "CLOSE_CURLY", 
                      "OPEN_CURLY", "DOTDOT", "COLON", "CONCEPT_LITERAL", 
                      "CODE_LITERAL", "IDENTIFIER", "STRING", "NUMBER", 
                      "COMMENT", "LINE_COMMENT", "WS", "DATE", "DATETIME", 
                      "TIME", "CODE_VALUE", "CONCEPT_VALUE", "OPEN_CODE", 
                      "CLOSE_CODE" ];

var ruleNames =  [ "document", "fcRule", "fcBody", "repeatsClause", "condition", 
                   "resource", "attribute", "value", "reference", "variableIdentifier" ];

function FhircapGrammar (input) {
	antlr4.Parser.call(this, input);
    this._interp = new antlr4.atn.ParserATNSimulator(this, atn, decisionsToDFA, sharedContextCache);
    this.ruleNames = ruleNames;
    this.literalNames = literalNames;
    this.symbolicNames = symbolicNames;
    return this;
}

FhircapGrammar.prototype = Object.create(antlr4.Parser.prototype);
FhircapGrammar.prototype.constructor = FhircapGrammar;

Object.defineProperty(FhircapGrammar.prototype, "atn", {
	get : function() {
		return atn;
	}
});

FhircapGrammar.EOF = antlr4.Token.EOF;
FhircapGrammar.ELSE = 1;
FhircapGrammar.REPEAT = 2;
FhircapGrammar.OPEN = 3;
FhircapGrammar.CLOSE = 4;
FhircapGrammar.NOT = 5;
FhircapGrammar.AND = 6;
FhircapGrammar.OR = 7;
FhircapGrammar.TRUE = 8;
FhircapGrammar.FALSE = 9;
FhircapGrammar.NULL = 10;
FhircapGrammar.NOTNULL = 11;
FhircapGrammar.VALUE = 12;
FhircapGrammar.EQ = 13;
FhircapGrammar.NEQ = 14;
FhircapGrammar.LT = 15;
FhircapGrammar.GT = 16;
FhircapGrammar.LTE = 17;
FhircapGrammar.GTE = 18;
FhircapGrammar.THEN = 19;
FhircapGrammar.COMMA = 20;
FhircapGrammar.END = 21;
FhircapGrammar.OPEN_SQ = 22;
FhircapGrammar.CLOSE_SQ = 23;
FhircapGrammar.DOT = 24;
FhircapGrammar.CONCEPT = 25;
FhircapGrammar.CONCEPT_SELECTED = 26;
FhircapGrammar.CODE_SELECTED = 27;
FhircapGrammar.REF = 28;
FhircapGrammar.OPEN_CURLY_DOLLAR = 29;
FhircapGrammar.CLOSE_CURLY = 30;
FhircapGrammar.OPEN_CURLY = 31;
FhircapGrammar.DOTDOT = 32;
FhircapGrammar.COLON = 33;
FhircapGrammar.CONCEPT_LITERAL = 34;
FhircapGrammar.CODE_LITERAL = 35;
FhircapGrammar.IDENTIFIER = 36;
FhircapGrammar.STRING = 37;
FhircapGrammar.NUMBER = 38;
FhircapGrammar.COMMENT = 39;
FhircapGrammar.LINE_COMMENT = 40;
FhircapGrammar.WS = 41;
FhircapGrammar.DATE = 42;
FhircapGrammar.DATETIME = 43;
FhircapGrammar.TIME = 44;
FhircapGrammar.CODE_VALUE = 45;
FhircapGrammar.CONCEPT_VALUE = 46;
FhircapGrammar.OPEN_CODE = 47;
FhircapGrammar.CLOSE_CODE = 48;

FhircapGrammar.RULE_document = 0;
FhircapGrammar.RULE_fcRule = 1;
FhircapGrammar.RULE_fcBody = 2;
FhircapGrammar.RULE_repeatsClause = 3;
FhircapGrammar.RULE_condition = 4;
FhircapGrammar.RULE_resource = 5;
FhircapGrammar.RULE_attribute = 6;
FhircapGrammar.RULE_value = 7;
FhircapGrammar.RULE_reference = 8;
FhircapGrammar.RULE_variableIdentifier = 9;


function DocumentContext(parser, parent, invokingState) {
	if(parent===undefined) {
	    parent = null;
	}
	if(invokingState===undefined || invokingState===null) {
		invokingState = -1;
	}
	antlr4.ParserRuleContext.call(this, parent, invokingState);
    this.parser = parser;
    this.ruleIndex = FhircapGrammar.RULE_document;
    return this;
}

DocumentContext.prototype = Object.create(antlr4.ParserRuleContext.prototype);
DocumentContext.prototype.constructor = DocumentContext;

DocumentContext.prototype.fcRule = function(i) {
    if(i===undefined) {
        i = null;
    }
    if(i===null) {
        return this.getTypedRuleContexts(FcRuleContext);
    } else {
        return this.getTypedRuleContext(FcRuleContext,i);
    }
};




FhircapGrammar.DocumentContext = DocumentContext;

FhircapGrammar.prototype.document = function() {

    var localctx = new DocumentContext(this, this._ctx, this.state);
    this.enterRule(localctx, 0, FhircapGrammar.RULE_document);
    var _la = 0; // Token type
    try {
        this.enterOuterAlt(localctx, 1);
        this.state = 23;
        this._errHandler.sync(this);
        _la = this._input.LA(1);
        while((((_la) & ~0x1f) == 0 && ((1 << _la) & ((1 << FhircapGrammar.REPEAT) | (1 << FhircapGrammar.OPEN) | (1 << FhircapGrammar.NOT) | (1 << FhircapGrammar.TRUE) | (1 << FhircapGrammar.FALSE) | (1 << FhircapGrammar.NULL) | (1 << FhircapGrammar.NOTNULL) | (1 << FhircapGrammar.VALUE))) !== 0)) {
            this.state = 20;
            this.fcRule();
            this.state = 25;
            this._errHandler.sync(this);
            _la = this._input.LA(1);
        }
    } catch (re) {
    	if(re instanceof antlr4.error.RecognitionException) {
	        localctx.exception = re;
	        this._errHandler.reportError(this, re);
	        this._errHandler.recover(this, re);
	    } else {
	    	throw re;
	    }
    } finally {
        this.exitRule();
    }
    return localctx;
};


function FcRuleContext(parser, parent, invokingState) {
	if(parent===undefined) {
	    parent = null;
	}
	if(invokingState===undefined || invokingState===null) {
		invokingState = -1;
	}
	antlr4.ParserRuleContext.call(this, parent, invokingState);
    this.parser = parser;
    this.ruleIndex = FhircapGrammar.RULE_fcRule;
    return this;
}

FcRuleContext.prototype = Object.create(antlr4.ParserRuleContext.prototype);
FcRuleContext.prototype.constructor = FcRuleContext;

FcRuleContext.prototype.condition = function() {
    return this.getTypedRuleContext(ConditionContext,0);
};

FcRuleContext.prototype.fcBody = function(i) {
    if(i===undefined) {
        i = null;
    }
    if(i===null) {
        return this.getTypedRuleContexts(FcBodyContext);
    } else {
        return this.getTypedRuleContext(FcBodyContext,i);
    }
};

FcRuleContext.prototype.repeatsClause = function() {
    return this.getTypedRuleContext(RepeatsClauseContext,0);
};

FcRuleContext.prototype.ELSE = function() {
    return this.getToken(FhircapGrammar.ELSE, 0);
};




FhircapGrammar.FcRuleContext = FcRuleContext;

FhircapGrammar.prototype.fcRule = function() {

    var localctx = new FcRuleContext(this, this._ctx, this.state);
    this.enterRule(localctx, 2, FhircapGrammar.RULE_fcRule);
    var _la = 0; // Token type
    try {
        this.enterOuterAlt(localctx, 1);
        this.state = 27;
        this._errHandler.sync(this);
        _la = this._input.LA(1);
        if(_la===FhircapGrammar.REPEAT) {
            this.state = 26;
            this.repeatsClause();
        }

        this.state = 29;
        this.condition(0);
        this.state = 30;
        this.fcBody();
        this.state = 33;
        this._errHandler.sync(this);
        _la = this._input.LA(1);
        if(_la===FhircapGrammar.ELSE) {
            this.state = 31;
            this.match(FhircapGrammar.ELSE);
            this.state = 32;
            this.fcBody();
        }

    } catch (re) {
    	if(re instanceof antlr4.error.RecognitionException) {
	        localctx.exception = re;
	        this._errHandler.reportError(this, re);
	        this._errHandler.recover(this, re);
	    } else {
	    	throw re;
	    }
    } finally {
        this.exitRule();
    }
    return localctx;
};


function FcBodyContext(parser, parent, invokingState) {
	if(parent===undefined) {
	    parent = null;
	}
	if(invokingState===undefined || invokingState===null) {
		invokingState = -1;
	}
	antlr4.ParserRuleContext.call(this, parent, invokingState);
    this.parser = parser;
    this.ruleIndex = FhircapGrammar.RULE_fcBody;
    return this;
}

FcBodyContext.prototype = Object.create(antlr4.ParserRuleContext.prototype);
FcBodyContext.prototype.constructor = FcBodyContext;

FcBodyContext.prototype.OPEN_CURLY = function() {
    return this.getToken(FhircapGrammar.OPEN_CURLY, 0);
};

FcBodyContext.prototype.CLOSE_CURLY = function() {
    return this.getToken(FhircapGrammar.CLOSE_CURLY, 0);
};

FcBodyContext.prototype.resource = function(i) {
    if(i===undefined) {
        i = null;
    }
    if(i===null) {
        return this.getTypedRuleContexts(ResourceContext);
    } else {
        return this.getTypedRuleContext(ResourceContext,i);
    }
};

FcBodyContext.prototype.fcRule = function(i) {
    if(i===undefined) {
        i = null;
    }
    if(i===null) {
        return this.getTypedRuleContexts(FcRuleContext);
    } else {
        return this.getTypedRuleContext(FcRuleContext,i);
    }
};




FhircapGrammar.FcBodyContext = FcBodyContext;

FhircapGrammar.prototype.fcBody = function() {

    var localctx = new FcBodyContext(this, this._ctx, this.state);
    this.enterRule(localctx, 4, FhircapGrammar.RULE_fcBody);
    var _la = 0; // Token type
    try {
        this.enterOuterAlt(localctx, 1);
        this.state = 35;
        this.match(FhircapGrammar.OPEN_CURLY);
        this.state = 40;
        this._errHandler.sync(this);
        _la = this._input.LA(1);
        while((((_la) & ~0x1f) == 0 && ((1 << _la) & ((1 << FhircapGrammar.REPEAT) | (1 << FhircapGrammar.OPEN) | (1 << FhircapGrammar.NOT) | (1 << FhircapGrammar.TRUE) | (1 << FhircapGrammar.FALSE) | (1 << FhircapGrammar.NULL) | (1 << FhircapGrammar.NOTNULL) | (1 << FhircapGrammar.VALUE))) !== 0) || _la===FhircapGrammar.IDENTIFIER) {
            this.state = 38;
            this._errHandler.sync(this);
            switch(this._input.LA(1)) {
            case FhircapGrammar.IDENTIFIER:
                this.state = 36;
                this.resource();
                break;
            case FhircapGrammar.REPEAT:
            case FhircapGrammar.OPEN:
            case FhircapGrammar.NOT:
            case FhircapGrammar.TRUE:
            case FhircapGrammar.FALSE:
            case FhircapGrammar.NULL:
            case FhircapGrammar.NOTNULL:
            case FhircapGrammar.VALUE:
                this.state = 37;
                this.fcRule();
                break;
            default:
                throw new antlr4.error.NoViableAltException(this);
            }
            this.state = 42;
            this._errHandler.sync(this);
            _la = this._input.LA(1);
        }
        this.state = 43;
        this.match(FhircapGrammar.CLOSE_CURLY);
    } catch (re) {
    	if(re instanceof antlr4.error.RecognitionException) {
	        localctx.exception = re;
	        this._errHandler.reportError(this, re);
	        this._errHandler.recover(this, re);
	    } else {
	    	throw re;
	    }
    } finally {
        this.exitRule();
    }
    return localctx;
};


function RepeatsClauseContext(parser, parent, invokingState) {
	if(parent===undefined) {
	    parent = null;
	}
	if(invokingState===undefined || invokingState===null) {
		invokingState = -1;
	}
	antlr4.ParserRuleContext.call(this, parent, invokingState);
    this.parser = parser;
    this.ruleIndex = FhircapGrammar.RULE_repeatsClause;
    return this;
}

RepeatsClauseContext.prototype = Object.create(antlr4.ParserRuleContext.prototype);
RepeatsClauseContext.prototype.constructor = RepeatsClauseContext;

RepeatsClauseContext.prototype.REPEAT = function() {
    return this.getToken(FhircapGrammar.REPEAT, 0);
};

RepeatsClauseContext.prototype.OPEN = function() {
    return this.getToken(FhircapGrammar.OPEN, 0);
};

RepeatsClauseContext.prototype.NUMBER = function(i) {
	if(i===undefined) {
		i = null;
	}
    if(i===null) {
        return this.getTokens(FhircapGrammar.NUMBER);
    } else {
        return this.getToken(FhircapGrammar.NUMBER, i);
    }
};


RepeatsClauseContext.prototype.DOTDOT = function() {
    return this.getToken(FhircapGrammar.DOTDOT, 0);
};

RepeatsClauseContext.prototype.COLON = function() {
    return this.getToken(FhircapGrammar.COLON, 0);
};

RepeatsClauseContext.prototype.IDENTIFIER = function() {
    return this.getToken(FhircapGrammar.IDENTIFIER, 0);
};

RepeatsClauseContext.prototype.CLOSE = function() {
    return this.getToken(FhircapGrammar.CLOSE, 0);
};




FhircapGrammar.RepeatsClauseContext = RepeatsClauseContext;

FhircapGrammar.prototype.repeatsClause = function() {

    var localctx = new RepeatsClauseContext(this, this._ctx, this.state);
    this.enterRule(localctx, 6, FhircapGrammar.RULE_repeatsClause);
    try {
        this.enterOuterAlt(localctx, 1);
        this.state = 45;
        this.match(FhircapGrammar.REPEAT);
        this.state = 46;
        this.match(FhircapGrammar.OPEN);
        this.state = 47;
        this.match(FhircapGrammar.NUMBER);
        this.state = 48;
        this.match(FhircapGrammar.DOTDOT);
        this.state = 49;
        this.match(FhircapGrammar.NUMBER);
        this.state = 50;
        this.match(FhircapGrammar.COLON);
        this.state = 51;
        this.match(FhircapGrammar.IDENTIFIER);
        this.state = 52;
        this.match(FhircapGrammar.CLOSE);
    } catch (re) {
    	if(re instanceof antlr4.error.RecognitionException) {
	        localctx.exception = re;
	        this._errHandler.reportError(this, re);
	        this._errHandler.recover(this, re);
	    } else {
	    	throw re;
	    }
    } finally {
        this.exitRule();
    }
    return localctx;
};


function ConditionContext(parser, parent, invokingState) {
	if(parent===undefined) {
	    parent = null;
	}
	if(invokingState===undefined || invokingState===null) {
		invokingState = -1;
	}
	antlr4.ParserRuleContext.call(this, parent, invokingState);
    this.parser = parser;
    this.ruleIndex = FhircapGrammar.RULE_condition;
    return this;
}

ConditionContext.prototype = Object.create(antlr4.ParserRuleContext.prototype);
ConditionContext.prototype.constructor = ConditionContext;

ConditionContext.prototype.NOT = function() {
    return this.getToken(FhircapGrammar.NOT, 0);
};

ConditionContext.prototype.condition = function(i) {
    if(i===undefined) {
        i = null;
    }
    if(i===null) {
        return this.getTypedRuleContexts(ConditionContext);
    } else {
        return this.getTypedRuleContext(ConditionContext,i);
    }
};

ConditionContext.prototype.TRUE = function() {
    return this.getToken(FhircapGrammar.TRUE, 0);
};

ConditionContext.prototype.FALSE = function() {
    return this.getToken(FhircapGrammar.FALSE, 0);
};

ConditionContext.prototype.OPEN = function() {
    return this.getToken(FhircapGrammar.OPEN, 0);
};

ConditionContext.prototype.variableIdentifier = function() {
    return this.getTypedRuleContext(VariableIdentifierContext,0);
};

ConditionContext.prototype.CLOSE = function() {
    return this.getToken(FhircapGrammar.CLOSE, 0);
};

ConditionContext.prototype.NULL = function() {
    return this.getToken(FhircapGrammar.NULL, 0);
};

ConditionContext.prototype.NOTNULL = function() {
    return this.getToken(FhircapGrammar.NOTNULL, 0);
};

ConditionContext.prototype.VALUE = function() {
    return this.getToken(FhircapGrammar.VALUE, 0);
};

ConditionContext.prototype.EQ = function() {
    return this.getToken(FhircapGrammar.EQ, 0);
};

ConditionContext.prototype.NEQ = function() {
    return this.getToken(FhircapGrammar.NEQ, 0);
};

ConditionContext.prototype.LT = function() {
    return this.getToken(FhircapGrammar.LT, 0);
};

ConditionContext.prototype.GT = function() {
    return this.getToken(FhircapGrammar.GT, 0);
};

ConditionContext.prototype.LTE = function() {
    return this.getToken(FhircapGrammar.LTE, 0);
};

ConditionContext.prototype.GTE = function() {
    return this.getToken(FhircapGrammar.GTE, 0);
};

ConditionContext.prototype.STRING = function() {
    return this.getToken(FhircapGrammar.STRING, 0);
};

ConditionContext.prototype.NUMBER = function() {
    return this.getToken(FhircapGrammar.NUMBER, 0);
};

ConditionContext.prototype.OR = function() {
    return this.getToken(FhircapGrammar.OR, 0);
};



FhircapGrammar.prototype.condition = function(_p) {
	if(_p===undefined) {
	    _p = 0;
	}
    var _parentctx = this._ctx;
    var _parentState = this.state;
    var localctx = new ConditionContext(this, this._ctx, _parentState);
    var _prevctx = localctx;
    var _startState = 8;
    this.enterRecursionRule(localctx, 8, FhircapGrammar.RULE_condition, _p);
    var _la = 0; // Token type
    try {
        this.enterOuterAlt(localctx, 1);
        this.state = 74;
        this._errHandler.sync(this);
        switch(this._input.LA(1)) {
        case FhircapGrammar.NOT:
            this.state = 55;
            this.match(FhircapGrammar.NOT);
            this.state = 56;
            this.condition(7);
            break;
        case FhircapGrammar.TRUE:
        case FhircapGrammar.FALSE:
            this.state = 57;
            _la = this._input.LA(1);
            if(!(_la===FhircapGrammar.TRUE || _la===FhircapGrammar.FALSE)) {
            this._errHandler.recoverInline(this);
            }
            else {
            	this._errHandler.reportMatch(this);
                this.consume();
            }
            break;
        case FhircapGrammar.NULL:
        case FhircapGrammar.NOTNULL:
            this.state = 58;
            _la = this._input.LA(1);
            if(!(_la===FhircapGrammar.NULL || _la===FhircapGrammar.NOTNULL)) {
            this._errHandler.recoverInline(this);
            }
            else {
            	this._errHandler.reportMatch(this);
                this.consume();
            }
            this.state = 59;
            this.match(FhircapGrammar.OPEN);
            this.state = 60;
            this.variableIdentifier();
            this.state = 61;
            this.match(FhircapGrammar.CLOSE);
            break;
        case FhircapGrammar.VALUE:
            this.state = 63;
            this.match(FhircapGrammar.VALUE);
            this.state = 64;
            this.match(FhircapGrammar.OPEN);
            this.state = 65;
            this.variableIdentifier();
            this.state = 66;
            this.match(FhircapGrammar.CLOSE);
            this.state = 67;
            _la = this._input.LA(1);
            if(!((((_la) & ~0x1f) == 0 && ((1 << _la) & ((1 << FhircapGrammar.EQ) | (1 << FhircapGrammar.NEQ) | (1 << FhircapGrammar.LT) | (1 << FhircapGrammar.GT) | (1 << FhircapGrammar.LTE) | (1 << FhircapGrammar.GTE))) !== 0))) {
            this._errHandler.recoverInline(this);
            }
            else {
            	this._errHandler.reportMatch(this);
                this.consume();
            }
            this.state = 68;
            _la = this._input.LA(1);
            if(!(_la===FhircapGrammar.STRING || _la===FhircapGrammar.NUMBER)) {
            this._errHandler.recoverInline(this);
            }
            else {
            	this._errHandler.reportMatch(this);
                this.consume();
            }
            break;
        case FhircapGrammar.OPEN:
            this.state = 70;
            this.match(FhircapGrammar.OPEN);
            this.state = 71;
            this.condition(0);
            this.state = 72;
            this.match(FhircapGrammar.CLOSE);
            break;
        default:
            throw new antlr4.error.NoViableAltException(this);
        }
        this._ctx.stop = this._input.LT(-1);
        this.state = 83;
        this._errHandler.sync(this);
        var _alt = this._interp.adaptivePredict(this._input,7,this._ctx)
        while(_alt!=2 && _alt!=antlr4.atn.ATN.INVALID_ALT_NUMBER) {
            if(_alt===1) {
                if(this._parseListeners!==null) {
                    this.triggerExitRuleEvent();
                }
                _prevctx = localctx;
                this.state = 81;
                this._errHandler.sync(this);
                var la_ = this._interp.adaptivePredict(this._input,6,this._ctx);
                switch(la_) {
                case 1:
                    localctx = new ConditionContext(this, _parentctx, _parentState);
                    this.pushNewRecursionContext(localctx, _startState, FhircapGrammar.RULE_condition);
                    this.state = 76;
                    if (!( this.precpred(this._ctx, 6))) {
                        throw new antlr4.error.FailedPredicateException(this, "this.precpred(this._ctx, 6)");
                    }
                    this.state = 77;
                    this.condition(7);
                    break;

                case 2:
                    localctx = new ConditionContext(this, _parentctx, _parentState);
                    this.pushNewRecursionContext(localctx, _startState, FhircapGrammar.RULE_condition);
                    this.state = 78;
                    if (!( this.precpred(this._ctx, 5))) {
                        throw new antlr4.error.FailedPredicateException(this, "this.precpred(this._ctx, 5)");
                    }
                    this.state = 79;
                    this.match(FhircapGrammar.OR);
                    this.state = 80;
                    this.condition(6);
                    break;

                } 
            }
            this.state = 85;
            this._errHandler.sync(this);
            _alt = this._interp.adaptivePredict(this._input,7,this._ctx);
        }

    } catch( error) {
        if(error instanceof antlr4.error.RecognitionException) {
	        localctx.exception = error;
	        this._errHandler.reportError(this, error);
	        this._errHandler.recover(this, error);
	    } else {
	    	throw error;
	    }
    } finally {
        this.unrollRecursionContexts(_parentctx)
    }
    return localctx;
};


function ResourceContext(parser, parent, invokingState) {
	if(parent===undefined) {
	    parent = null;
	}
	if(invokingState===undefined || invokingState===null) {
		invokingState = -1;
	}
	antlr4.ParserRuleContext.call(this, parent, invokingState);
    this.parser = parser;
    this.ruleIndex = FhircapGrammar.RULE_resource;
    return this;
}

ResourceContext.prototype = Object.create(antlr4.ParserRuleContext.prototype);
ResourceContext.prototype.constructor = ResourceContext;

ResourceContext.prototype.IDENTIFIER = function() {
    return this.getToken(FhircapGrammar.IDENTIFIER, 0);
};

ResourceContext.prototype.LT = function() {
    return this.getToken(FhircapGrammar.LT, 0);
};

ResourceContext.prototype.variableIdentifier = function() {
    return this.getTypedRuleContext(VariableIdentifierContext,0);
};

ResourceContext.prototype.GT = function() {
    return this.getToken(FhircapGrammar.GT, 0);
};

ResourceContext.prototype.THEN = function() {
    return this.getToken(FhircapGrammar.THEN, 0);
};

ResourceContext.prototype.attribute = function(i) {
    if(i===undefined) {
        i = null;
    }
    if(i===null) {
        return this.getTypedRuleContexts(AttributeContext);
    } else {
        return this.getTypedRuleContext(AttributeContext,i);
    }
};

ResourceContext.prototype.EQ = function(i) {
	if(i===undefined) {
		i = null;
	}
    if(i===null) {
        return this.getTokens(FhircapGrammar.EQ);
    } else {
        return this.getToken(FhircapGrammar.EQ, i);
    }
};


ResourceContext.prototype.value = function(i) {
    if(i===undefined) {
        i = null;
    }
    if(i===null) {
        return this.getTypedRuleContexts(ValueContext);
    } else {
        return this.getTypedRuleContext(ValueContext,i);
    }
};

ResourceContext.prototype.END = function() {
    return this.getToken(FhircapGrammar.END, 0);
};

ResourceContext.prototype.COMMA = function(i) {
	if(i===undefined) {
		i = null;
	}
    if(i===null) {
        return this.getTokens(FhircapGrammar.COMMA);
    } else {
        return this.getToken(FhircapGrammar.COMMA, i);
    }
};





FhircapGrammar.ResourceContext = ResourceContext;

FhircapGrammar.prototype.resource = function() {

    var localctx = new ResourceContext(this, this._ctx, this.state);
    this.enterRule(localctx, 10, FhircapGrammar.RULE_resource);
    var _la = 0; // Token type
    try {
        this.enterOuterAlt(localctx, 1);
        this.state = 86;
        this.match(FhircapGrammar.IDENTIFIER);
        this.state = 87;
        this.match(FhircapGrammar.LT);
        this.state = 88;
        this.variableIdentifier();
        this.state = 89;
        this.match(FhircapGrammar.GT);
        this.state = 90;
        this.match(FhircapGrammar.THEN);
        this.state = 91;
        this.attribute();
        this.state = 92;
        this.match(FhircapGrammar.EQ);
        this.state = 93;
        this.value();
        this.state = 101;
        this._errHandler.sync(this);
        _la = this._input.LA(1);
        while(_la===FhircapGrammar.COMMA) {
            this.state = 94;
            this.match(FhircapGrammar.COMMA);
            this.state = 95;
            this.attribute();
            this.state = 96;
            this.match(FhircapGrammar.EQ);
            this.state = 97;
            this.value();
            this.state = 103;
            this._errHandler.sync(this);
            _la = this._input.LA(1);
        }
        this.state = 104;
        this.match(FhircapGrammar.END);
    } catch (re) {
    	if(re instanceof antlr4.error.RecognitionException) {
	        localctx.exception = re;
	        this._errHandler.reportError(this, re);
	        this._errHandler.recover(this, re);
	    } else {
	    	throw re;
	    }
    } finally {
        this.exitRule();
    }
    return localctx;
};


function AttributeContext(parser, parent, invokingState) {
	if(parent===undefined) {
	    parent = null;
	}
	if(invokingState===undefined || invokingState===null) {
		invokingState = -1;
	}
	antlr4.ParserRuleContext.call(this, parent, invokingState);
    this.parser = parser;
    this.ruleIndex = FhircapGrammar.RULE_attribute;
    return this;
}

AttributeContext.prototype = Object.create(antlr4.ParserRuleContext.prototype);
AttributeContext.prototype.constructor = AttributeContext;

AttributeContext.prototype.IDENTIFIER = function() {
    return this.getToken(FhircapGrammar.IDENTIFIER, 0);
};

AttributeContext.prototype.OPEN_SQ = function() {
    return this.getToken(FhircapGrammar.OPEN_SQ, 0);
};

AttributeContext.prototype.NUMBER = function() {
    return this.getToken(FhircapGrammar.NUMBER, 0);
};

AttributeContext.prototype.CLOSE_SQ = function() {
    return this.getToken(FhircapGrammar.CLOSE_SQ, 0);
};

AttributeContext.prototype.DOT = function() {
    return this.getToken(FhircapGrammar.DOT, 0);
};

AttributeContext.prototype.attribute = function() {
    return this.getTypedRuleContext(AttributeContext,0);
};




FhircapGrammar.AttributeContext = AttributeContext;

FhircapGrammar.prototype.attribute = function() {

    var localctx = new AttributeContext(this, this._ctx, this.state);
    this.enterRule(localctx, 12, FhircapGrammar.RULE_attribute);
    var _la = 0; // Token type
    try {
        this.enterOuterAlt(localctx, 1);
        this.state = 106;
        this.match(FhircapGrammar.IDENTIFIER);
        this.state = 110;
        this._errHandler.sync(this);
        _la = this._input.LA(1);
        if(_la===FhircapGrammar.OPEN_SQ) {
            this.state = 107;
            this.match(FhircapGrammar.OPEN_SQ);
            this.state = 108;
            this.match(FhircapGrammar.NUMBER);
            this.state = 109;
            this.match(FhircapGrammar.CLOSE_SQ);
        }

        this.state = 114;
        this._errHandler.sync(this);
        _la = this._input.LA(1);
        if(_la===FhircapGrammar.DOT) {
            this.state = 112;
            this.match(FhircapGrammar.DOT);
            this.state = 113;
            this.attribute();
        }

    } catch (re) {
    	if(re instanceof antlr4.error.RecognitionException) {
	        localctx.exception = re;
	        this._errHandler.reportError(this, re);
	        this._errHandler.recover(this, re);
	    } else {
	    	throw re;
	    }
    } finally {
        this.exitRule();
    }
    return localctx;
};


function ValueContext(parser, parent, invokingState) {
	if(parent===undefined) {
	    parent = null;
	}
	if(invokingState===undefined || invokingState===null) {
		invokingState = -1;
	}
	antlr4.ParserRuleContext.call(this, parent, invokingState);
    this.parser = parser;
    this.ruleIndex = FhircapGrammar.RULE_value;
    return this;
}

ValueContext.prototype = Object.create(antlr4.ParserRuleContext.prototype);
ValueContext.prototype.constructor = ValueContext;

ValueContext.prototype.TRUE = function() {
    return this.getToken(FhircapGrammar.TRUE, 0);
};

ValueContext.prototype.FALSE = function() {
    return this.getToken(FhircapGrammar.FALSE, 0);
};

ValueContext.prototype.STRING = function() {
    return this.getToken(FhircapGrammar.STRING, 0);
};

ValueContext.prototype.NUMBER = function() {
    return this.getToken(FhircapGrammar.NUMBER, 0);
};

ValueContext.prototype.reference = function() {
    return this.getTypedRuleContext(ReferenceContext,0);
};

ValueContext.prototype.CONCEPT_LITERAL = function() {
    return this.getToken(FhircapGrammar.CONCEPT_LITERAL, 0);
};

ValueContext.prototype.OPEN_CODE = function() {
    return this.getToken(FhircapGrammar.OPEN_CODE, 0);
};

ValueContext.prototype.CONCEPT_VALUE = function() {
    return this.getToken(FhircapGrammar.CONCEPT_VALUE, 0);
};

ValueContext.prototype.CLOSE_CODE = function() {
    return this.getToken(FhircapGrammar.CLOSE_CODE, 0);
};

ValueContext.prototype.CODE_LITERAL = function() {
    return this.getToken(FhircapGrammar.CODE_LITERAL, 0);
};

ValueContext.prototype.CODE_VALUE = function() {
    return this.getToken(FhircapGrammar.CODE_VALUE, 0);
};

ValueContext.prototype.OPEN = function() {
    return this.getToken(FhircapGrammar.OPEN, 0);
};

ValueContext.prototype.variableIdentifier = function() {
    return this.getTypedRuleContext(VariableIdentifierContext,0);
};

ValueContext.prototype.CLOSE = function() {
    return this.getToken(FhircapGrammar.CLOSE, 0);
};

ValueContext.prototype.CONCEPT = function() {
    return this.getToken(FhircapGrammar.CONCEPT, 0);
};

ValueContext.prototype.CONCEPT_SELECTED = function() {
    return this.getToken(FhircapGrammar.CONCEPT_SELECTED, 0);
};

ValueContext.prototype.CODE_SELECTED = function() {
    return this.getToken(FhircapGrammar.CODE_SELECTED, 0);
};

ValueContext.prototype.VALUE = function() {
    return this.getToken(FhircapGrammar.VALUE, 0);
};




FhircapGrammar.ValueContext = ValueContext;

FhircapGrammar.prototype.value = function() {

    var localctx = new ValueContext(this, this._ctx, this.state);
    this.enterRule(localctx, 14, FhircapGrammar.RULE_value);
    var _la = 0; // Token type
    try {
        this.state = 133;
        this._errHandler.sync(this);
        switch(this._input.LA(1)) {
        case FhircapGrammar.TRUE:
        case FhircapGrammar.FALSE:
            this.enterOuterAlt(localctx, 1);
            this.state = 116;
            _la = this._input.LA(1);
            if(!(_la===FhircapGrammar.TRUE || _la===FhircapGrammar.FALSE)) {
            this._errHandler.recoverInline(this);
            }
            else {
            	this._errHandler.reportMatch(this);
                this.consume();
            }
            break;
        case FhircapGrammar.STRING:
            this.enterOuterAlt(localctx, 2);
            this.state = 117;
            this.match(FhircapGrammar.STRING);
            break;
        case FhircapGrammar.NUMBER:
            this.enterOuterAlt(localctx, 3);
            this.state = 118;
            this.match(FhircapGrammar.NUMBER);
            break;
        case FhircapGrammar.REF:
            this.enterOuterAlt(localctx, 4);
            this.state = 119;
            this.reference();
            break;
        case FhircapGrammar.CONCEPT_LITERAL:
            this.enterOuterAlt(localctx, 5);
            this.state = 120;
            this.match(FhircapGrammar.CONCEPT_LITERAL);
            this.state = 121;
            this.match(FhircapGrammar.OPEN_CODE);
            this.state = 122;
            this.match(FhircapGrammar.CONCEPT_VALUE);
            this.state = 123;
            this.match(FhircapGrammar.CLOSE_CODE);
            break;
        case FhircapGrammar.CODE_LITERAL:
            this.enterOuterAlt(localctx, 6);
            this.state = 124;
            this.match(FhircapGrammar.CODE_LITERAL);
            this.state = 125;
            this.match(FhircapGrammar.OPEN_CODE);
            this.state = 126;
            this.match(FhircapGrammar.CODE_VALUE);
            this.state = 127;
            this.match(FhircapGrammar.CLOSE_CODE);
            break;
        case FhircapGrammar.VALUE:
        case FhircapGrammar.CONCEPT:
        case FhircapGrammar.CONCEPT_SELECTED:
        case FhircapGrammar.CODE_SELECTED:
            this.enterOuterAlt(localctx, 7);
            this.state = 128;
            _la = this._input.LA(1);
            if(!((((_la) & ~0x1f) == 0 && ((1 << _la) & ((1 << FhircapGrammar.VALUE) | (1 << FhircapGrammar.CONCEPT) | (1 << FhircapGrammar.CONCEPT_SELECTED) | (1 << FhircapGrammar.CODE_SELECTED))) !== 0))) {
            this._errHandler.recoverInline(this);
            }
            else {
            	this._errHandler.reportMatch(this);
                this.consume();
            }
            this.state = 129;
            this.match(FhircapGrammar.OPEN);
            this.state = 130;
            this.variableIdentifier();
            this.state = 131;
            this.match(FhircapGrammar.CLOSE);
            break;
        default:
            throw new antlr4.error.NoViableAltException(this);
        }
    } catch (re) {
    	if(re instanceof antlr4.error.RecognitionException) {
	        localctx.exception = re;
	        this._errHandler.reportError(this, re);
	        this._errHandler.recover(this, re);
	    } else {
	    	throw re;
	    }
    } finally {
        this.exitRule();
    }
    return localctx;
};


function ReferenceContext(parser, parent, invokingState) {
	if(parent===undefined) {
	    parent = null;
	}
	if(invokingState===undefined || invokingState===null) {
		invokingState = -1;
	}
	antlr4.ParserRuleContext.call(this, parent, invokingState);
    this.parser = parser;
    this.ruleIndex = FhircapGrammar.RULE_reference;
    return this;
}

ReferenceContext.prototype = Object.create(antlr4.ParserRuleContext.prototype);
ReferenceContext.prototype.constructor = ReferenceContext;

ReferenceContext.prototype.REF = function() {
    return this.getToken(FhircapGrammar.REF, 0);
};

ReferenceContext.prototype.OPEN = function() {
    return this.getToken(FhircapGrammar.OPEN, 0);
};

ReferenceContext.prototype.IDENTIFIER = function() {
    return this.getToken(FhircapGrammar.IDENTIFIER, 0);
};

ReferenceContext.prototype.LT = function() {
    return this.getToken(FhircapGrammar.LT, 0);
};

ReferenceContext.prototype.variableIdentifier = function() {
    return this.getTypedRuleContext(VariableIdentifierContext,0);
};

ReferenceContext.prototype.GT = function() {
    return this.getToken(FhircapGrammar.GT, 0);
};

ReferenceContext.prototype.CLOSE = function() {
    return this.getToken(FhircapGrammar.CLOSE, 0);
};




FhircapGrammar.ReferenceContext = ReferenceContext;

FhircapGrammar.prototype.reference = function() {

    var localctx = new ReferenceContext(this, this._ctx, this.state);
    this.enterRule(localctx, 16, FhircapGrammar.RULE_reference);
    try {
        this.enterOuterAlt(localctx, 1);
        this.state = 135;
        this.match(FhircapGrammar.REF);
        this.state = 136;
        this.match(FhircapGrammar.OPEN);
        this.state = 137;
        this.match(FhircapGrammar.IDENTIFIER);
        this.state = 138;
        this.match(FhircapGrammar.LT);
        this.state = 139;
        this.variableIdentifier();
        this.state = 140;
        this.match(FhircapGrammar.GT);
        this.state = 141;
        this.match(FhircapGrammar.CLOSE);
    } catch (re) {
    	if(re instanceof antlr4.error.RecognitionException) {
	        localctx.exception = re;
	        this._errHandler.reportError(this, re);
	        this._errHandler.recover(this, re);
	    } else {
	    	throw re;
	    }
    } finally {
        this.exitRule();
    }
    return localctx;
};


function VariableIdentifierContext(parser, parent, invokingState) {
	if(parent===undefined) {
	    parent = null;
	}
	if(invokingState===undefined || invokingState===null) {
		invokingState = -1;
	}
	antlr4.ParserRuleContext.call(this, parent, invokingState);
    this.parser = parser;
    this.ruleIndex = FhircapGrammar.RULE_variableIdentifier;
    return this;
}

VariableIdentifierContext.prototype = Object.create(antlr4.ParserRuleContext.prototype);
VariableIdentifierContext.prototype.constructor = VariableIdentifierContext;

VariableIdentifierContext.prototype.IDENTIFIER = function(i) {
	if(i===undefined) {
		i = null;
	}
    if(i===null) {
        return this.getTokens(FhircapGrammar.IDENTIFIER);
    } else {
        return this.getToken(FhircapGrammar.IDENTIFIER, i);
    }
};


VariableIdentifierContext.prototype.OPEN_CURLY_DOLLAR = function() {
    return this.getToken(FhircapGrammar.OPEN_CURLY_DOLLAR, 0);
};

VariableIdentifierContext.prototype.CLOSE_CURLY = function() {
    return this.getToken(FhircapGrammar.CLOSE_CURLY, 0);
};




FhircapGrammar.VariableIdentifierContext = VariableIdentifierContext;

FhircapGrammar.prototype.variableIdentifier = function() {

    var localctx = new VariableIdentifierContext(this, this._ctx, this.state);
    this.enterRule(localctx, 18, FhircapGrammar.RULE_variableIdentifier);
    var _la = 0; // Token type
    try {
        this.enterOuterAlt(localctx, 1);
        this.state = 143;
        this.match(FhircapGrammar.IDENTIFIER);
        this.state = 147;
        this._errHandler.sync(this);
        _la = this._input.LA(1);
        if(_la===FhircapGrammar.OPEN_CURLY_DOLLAR) {
            this.state = 144;
            this.match(FhircapGrammar.OPEN_CURLY_DOLLAR);
            this.state = 145;
            this.match(FhircapGrammar.IDENTIFIER);
            this.state = 146;
            this.match(FhircapGrammar.CLOSE_CURLY);
        }

    } catch (re) {
    	if(re instanceof antlr4.error.RecognitionException) {
	        localctx.exception = re;
	        this._errHandler.reportError(this, re);
	        this._errHandler.recover(this, re);
	    } else {
	    	throw re;
	    }
    } finally {
        this.exitRule();
    }
    return localctx;
};


FhircapGrammar.prototype.sempred = function(localctx, ruleIndex, predIndex) {
	switch(ruleIndex) {
	case 4:
			return this.condition_sempred(localctx, predIndex);
    default:
        throw "No predicate with index:" + ruleIndex;
   }
};

FhircapGrammar.prototype.condition_sempred = function(localctx, predIndex) {
	switch(predIndex) {
		case 0:
			return this.precpred(this._ctx, 6);
		case 1:
			return this.precpred(this._ctx, 5);
		default:
			throw "No predicate with index:" + predIndex;
	}
};


exports.FhircapGrammar = FhircapGrammar;
