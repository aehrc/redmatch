lexer grammar RedmatchLexer;

ELSE              : 'ELSE';
REPEAT            : 'REPEAT' ;
OPEN              : '(';
CLOSE             : ')';
NOT               : '^';
AND               : '&';
OR                : '|';
TRUE              : 'TRUE';
FALSE             : 'FALSE';
NULL              : 'NULL';
NOTNULL           : 'NOTNULL';
VALUE             : 'VALUE';
EQ                : '=';
NEQ               : '!=';
LT                : '<';
GT                : '>';
LTE               : '<='; 
GTE               : '>=';
THEN              : '->';
COMMA             : ',';
END               : ';';
OPEN_SQ           : '[';
CLOSE_SQ          : ']';
DOT               : '.';
CONCEPT           : 'CONCEPT';
CONCEPT_SELECTED  : 'CONCEPT_SELECTED';
CODE_SELECTED     : 'CODE_SELECTED';
REF               : 'REF';
CLOSE_CURLY       : '}';
OPEN_CURLY        : '{';
OPEN_CURLY_DOLLAR : '${';
DOTDOT            : '..';
COLON             : ':';

CONCEPT_LITERAL
    : 'CONCEPT_LITERAL' -> pushMode(FHIR_CONCEPT)
    ;
    
CODE_LITERAL
    : 'CODE_LITERAL' -> pushMode(FHIR_CODE)
    ;

fragment LOWERCASE  : [a-z] ;
fragment UPPERCASE  : [A-Z] ;
fragment DIGIT : [0-9];

IDENTIFIER
    : (LOWERCASE | UPPERCASE | '_' | '-')+ (LOWERCASE | UPPERCASE | DIGIT | '_' | '-')*
    ;

STRING
    : '\'' (ESC | .)*? '\''
    ;

NUMBER
    : DIGIT+('.' DIGIT+)?
    ;
        
COMMENT
    : '/*' .*? '*/' -> skip
    ;

LINE_COMMENT
    : '//' ~[\r\n]* -> skip
    ;
        
WS
    : [ \r\n\t]+ -> skip
    ;

DATE
    : '@' DATEFORMAT
    ;

DATETIME
    : '@' DATEFORMAT 'T' (TIMEFORMAT TIMEZONEOFFSETFORMAT?)?
    ;

TIME
    : '@' 'T' TIMEFORMAT
    ;

fragment DATEFORMAT
    : [0-9][0-9][0-9][0-9] ('-'[0-9][0-9] ('-'[0-9][0-9])?)?
    ;

fragment TIMEFORMAT
    : [0-9][0-9] (':'[0-9][0-9] (':'[0-9][0-9] ('.'[0-9]+)?)?)?
    ;

fragment TIMEZONEOFFSETFORMAT
    : ('Z' | ('+' | '-') [0-9][0-9]':'[0-9][0-9])
    ;

fragment ESC
    : '\\' ([`'\\/fnrt] | UNICODE)    // allow \`, \', \\, \/, \f, etc. and \uXXX
    ;

fragment UNICODE
    : 'u' HEX HEX HEX HEX
    ;

fragment HEX
    : [0-9a-fA-F]
    ;

mode FHIR_CONCEPT;

CONCEPT_VALUE
    : '(' .+? '|' .*? ('|' STRING)? ')' -> popMode
    ;

mode FHIR_CODE;

CODE_VALUE
    : '(' .*? ')' -> popMode
    ;