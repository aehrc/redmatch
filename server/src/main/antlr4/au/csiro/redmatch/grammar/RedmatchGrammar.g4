/*
 * Copyright © 2018-2021, Commonwealth Scientific and Industrial Research
 * Organisation (CSIRO) ABN 41 687 119 230. Licensed under the CSIRO Open Source
 * Software Licence Agreement.
 */
parser grammar RedmatchGrammar;

options { tokenVocab=RedmatchLexer; }

document
    : fcRule*
    ;

fcRule
    : repeatsClause? condition fcBody (ELSE fcBody)?
    ;

fcBody
    : OPEN_CURLY (resource | fcRule)* CLOSE_CURLY
    ;
    
repeatsClause
    : REPEAT OPEN NUMBER DOTDOT NUMBER COLON ID CLOSE
    ;

condition
    : NOT condition
    | condition AND condition
    | condition OR condition
    | (TRUE | FALSE)
    | (NULL | NOTNULL) OPEN ID CLOSE
    | VALUE OPEN ID CLOSE(EQ | NEQ | LT | GT | LTE | GTE) (STRING | NUMBER)
    | OPEN condition CLOSE
    ;

resource
    : RESOURCE LT ID GT COLON attribute value (attribute value)*
    ;
  
attribute
    : ATTRIBUTE_START attributePath (DOT attributePath)* ATTRIBUTE_END
    ;

attributePath
    : PATH (OPEN_SQ INDEX CLOSE_SQ)?
    ;

value
    : (TRUE | FALSE)
    | STRING
    | NUMBER
    | reference
    | CONCEPT_LITERAL
    | CODE_LITERAL
    | (CONCEPT | CONCEPT_SELECTED | CODE_SELECTED) OPEN ID CLOSE
    | VALUE OPEN ID (COMMA STRING)? CLOSE
    ;

reference
    : REF OPEN RESOURCE LT ID GT CLOSE
    ;


