/*
 * Copyright © 2018-2021, Commonwealth Scientific and Industrial Research
 * Organisation (CSIRO) ABN 41 687 119 230. Licensed under the CSIRO Open Source
 * Software Licence Agreement.
 */
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
CONCEPT           : 'CONCEPT';
CONCEPT_SELECTED  : 'CONCEPT_SELECTED';
CODE_SELECTED     : 'CODE_SELECTED';
REF               : 'REF';
CLOSE_CURLY       : '}';
OPEN_CURLY        : '{';
OPEN_CURLY_DOLLAR : '${';
DOTDOT            : '..';
COLON             : ':';
ATTRIBUTE_START   : '*' -> pushMode(ATTRIBUTES) ;

fragment LOWERCASE  : [a-z] ;
fragment UPPERCASE  : [A-Z] ;
fragment DIGIT      : [0-9];

// A FHIR resource identifier.
RESOURCE
    : UPPERCASE (LOWERCASE | UPPERCASE)*
    ;

// An identifier of a REDCap form, a FHIR resource created in the rules or a Redmatch variable. Can 
// include a reference to a Redmatch variable anywhere, except on the first character and when a
// Redmatch variable is being defined.
ID
    : LOWERCASE (REDMATCH_ID | LOWERCASE | UPPERCASE | DIGIT | '_'  | '-')*
    ;

// A reference to a Redmatch variable
REDMATCH_ID
    : OPEN_CURLY_DOLLAR (LOWERCASE | UPPERCASE | DIGIT | '_')+ CLOSE_CURLY
    ;

STRING
    : '\'' (ESC | .)*? '\''
    ;

NUMBER
    : DIGIT+('.' DIGIT+)?
    ;

COMMENT
    : '/*' .*? '*/' -> channel(HIDDEN)
    ;

LINE_COMMENT
    : '//' ~[\r\n]* -> channel(HIDDEN)
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

CONCEPT_LITERAL
    :  'CONCEPT_LITERAL' OPEN .*? '|' .*? ('|' STRING)? CLOSE
    ;

CODE_LITERAL
    :  'CODE_LITERAL' OPEN .*? CLOSE
    ;
    
mode ATTRIBUTES;

OPEN_SQ           : '[';
CLOSE_SQ          : ']';
DOT               : '.';
INDEX             : [0-9]+;
PATH              : LOWERCASE (LOWERCASE | UPPERCASE)*;
WHITE_SPACE       : [ \r\n\t]+ -> skip;
ATTRIBUTE_END     : '=' -> popMode;
COMMA             : ',';
