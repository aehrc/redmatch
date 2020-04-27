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
    : REPEAT OPEN NUMBER DOTDOT NUMBER COLON IDENTIFIER CLOSE
    ;

condition
    : NOT condition
    | condition AND condition
    | condition OR condition
    | (TRUE | FALSE)
    | (NULL | NOTNULL) OPEN variableIdentifier CLOSE
    | VALUE OPEN variableIdentifier CLOSE(EQ | NEQ | LT | GT | LTE | GTE) (STRING | NUMBER)
    | OPEN condition CLOSE
    ;

resource
    : IDENTIFIER LT variableIdentifier GT THEN attribute EQ value (COMMA attribute EQ value)* END
    ;
  
attribute
    : IDENTIFIER (OPEN_SQ NUMBER CLOSE_SQ)? (DOT attribute)?
    ;

value
    : (TRUE | FALSE)
    | STRING
    | NUMBER
    | reference
    | CONCEPT_LITERAL OPEN_CODE CONCEPT_VALUE CLOSE_CODE
    | CODE_LITERAL OPEN_CODE CODE_VALUE CLOSE_CODE
    | (CONCEPT | CONCEPT_SELECTED | CODE_SELECTED | VALUE ) OPEN variableIdentifier CLOSE
    ;

reference
    : REF OPEN IDENTIFIER LT variableIdentifier GT CLOSE
    ;

variableIdentifier
    : IDENTIFIER  (OPEN_CURLY_DOLLAR IDENTIFIER CLOSE_CURLY)?
    ;

