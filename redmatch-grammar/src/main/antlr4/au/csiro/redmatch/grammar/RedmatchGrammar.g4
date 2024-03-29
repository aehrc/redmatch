/*
 * Copyright © 2018-2021, Commonwealth Scientific and Industrial Research
 * Organisation (CSIRO) ABN 41 687 119 230. Licensed under the CSIRO Open Source
 * Software Licence Agreement.
 */
parser grammar RedmatchGrammar;

options { tokenVocab=RedmatchLexer; }

document
    : schema target? server? aliases? rules mappings?
    ;

schema
    : SCHEMA COLON STRING OPEN SCHEMA_TYPE CLOSE
    ;

target
    : TARGET COLON STRING
    ;

server
    : SERVER COLON STRING
    ;

aliases
    : ALIASES COLON OPEN_CURLY alias* CLOSE_CURLY
    ;

alias
    : ALIAS EQ STRING
    ;

rules
    : RULES COLON OPEN_CURLY fcRule* CLOSE_CURLY
    ;

fcRule
    : repeatsClause? condition fcBody (ELSE fcBody)?
    ;

fcBody
    : OPEN_CURLY (resource | fcRule)* CLOSE_CURLY
    ;
    
repeatsClause
    : REPEAT R_OPEN R_NUMBER DOTDOT R_NUMBER R_COLON ID CLOSE
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
    : ID LT ID GT COLON attribute value (attribute value)*
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
    | CONCEPT_LITERAL CL_OPEN (CL_ALIAS | CL_PART) CL_PIPE CL_PART (CL_PIPE CL_STRING)? CL_CLOSE
    | (CONCEPT | CONCEPT_SELECTED | CODE_SELECTED) OPEN ID CLOSE
    | CODE C_OPEN C_ID CLOSE
    | VALUE OPEN ID (COMMA STRING)? CLOSE
    ;

reference
    : REF OPEN ID LT ID GT CLOSE
    ;

mappings
    : MAPPINGS COLON OPEN_CURLY mapping* CLOSE_CURLY
    ;

mapping
    : ID (OR STRING)? MAP (CL_ALIAS | CL_PART) CL_PIPE CL_PART (CL_PIPE CL_STRING)? CL_SEMICOLON
    ;