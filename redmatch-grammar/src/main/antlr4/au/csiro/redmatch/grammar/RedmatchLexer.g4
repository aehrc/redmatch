/*
 * Copyright © 2018-2021, Commonwealth Scientific and Industrial Research
 * Organisation (CSIRO) ABN 41 687 119 230. Licensed under the CSIRO Open Source
 * Software Licence Agreement.
 */
lexer grammar RedmatchLexer;

ALIASES              : 'ALIASES';
SCHEMA               : 'SCHEMA';
SERVER               : 'SERVER';
RULES                : 'RULES';
MAPPINGS             : 'MAPPINGS';
ELSE                 : 'ELSE';
REPEAT               : 'REPEAT' ;
OPEN                 : '(';
CLOSE                : ')';
NOT                  : '^';
AND                  : '&';
OR                   : '|';
DOLLAR               : '$';
TRUE                 : 'TRUE';
FALSE                : 'FALSE';
NULL                 : 'NULL';
NOTNULL              : 'NOTNULL';
VALUE                : 'VALUE';
EQ                   : '=';
NEQ                  : '!=';
LT                   : '<';
GT                   : '>';
LTE                  : '<=';
GTE                  : '>=';
CONCEPT              : 'CONCEPT';
CONCEPT_SELECTED     : 'CONCEPT_SELECTED';
CONCEPT_LITERAL      : 'CONCEPT_LITERAL' -> pushMode(CONCEPT_LITERAL_MODE);
CODE                 : 'CODE' -> pushMode(CODE_MODE);
CODE_SELECTED        : 'CODE_SELECTED';
REF                  : 'REF';
CLOSE_CURLY          : '}';
OPEN_CURLY           : '{';
OPEN_CURLY_DOLLAR    : '${';
DOTDOT               : '..';
COLON                : ':';
COMMA                : ',';
SEMICOLON            : ';';
MAP                  : '->' -> pushMode(CONCEPT_LITERAL_MODE) ;
ATTRIBUTE_START      : '*' -> pushMode(ATTRIBUTES) ;

fragment LOWERCASE   : [a-z] ;
fragment UPPERCASE   : [A-Z] ;
fragment DIGIT       : [0-9];

SCHEMA_TYPE
    : UPPERCASE UPPERCASE+
    ;

// A FHIR resource identifier.
RESOURCE
    : UPPERCASE (LOWERCASE | UPPERCASE)*
    ;

ALIAS
    : DOLLAR UPPERCASE*
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
    
mode ATTRIBUTES;

OPEN_SQ           : '[';
CLOSE_SQ          : ']';
DOT               : '.';
INDEX             : [0-9]+;
PATH              : LOWERCASE (LOWERCASE | UPPERCASE)*;
ATT_WS            : [ \r\n\t]+ -> skip;
ATTRIBUTE_END     : '=' -> popMode;

mode CONCEPT_LITERAL_MODE;

CL_STRING : STRING;
CL_PIPE   : OR;
CL_OPEN   : OPEN;
CL_PART   : ~([ \r\n\t]|'\''|'|'|'('|')'|';'|'$') ~('\''|'|'|'('|')'|';'|'$')*;
CL_ALIAS  : DOLLAR UPPERCASE*;
CL_WS     : [ \r\n\t]+ -> skip;
CL_CLOSE  : CLOSE -> popMode;
CL_SEMICOLON : SEMICOLON -> popMode;

mode CODE_MODE;

C_OPEN: OPEN;
C_ID : ~([ \r\n\t] | '(' | ')') ~([ \r\n\t] |'(' | ')')* -> popMode;