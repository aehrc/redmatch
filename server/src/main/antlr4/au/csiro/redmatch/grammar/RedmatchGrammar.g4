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
    | (NULL | NOTNULL) OPEN variable CLOSE
    | VALUE OPEN variable CLOSE(EQ | NEQ | LT | GT | LTE | GTE) (STRING | NUMBER)
    | OPEN condition CLOSE
    ;

resource
    : IDENTIFIER LT variable GT THEN attribute EQ value (COMMA attribute EQ value)* END
    ;
  
attribute
    : IDENTIFIER (OPEN_SQ NUMBER CLOSE_SQ)? (DOT attribute)?
    ;

value
    : (TRUE | FALSE)
    | STRING
    | NUMBER
    | reference
    | CONCEPT_LITERAL CONCEPT_VALUE
    | CODE_LITERAL CODE_VALUE
    | (CONCEPT | CONCEPT_SELECTED | CODE_SELECTED | VALUE ) OPEN variable CLOSE
    ;

reference
    : REF OPEN IDENTIFIER LT variable GT CLOSE
    ;

variable
    : IDENTIFIER (OPEN_CURLY_DOLLAR IDENTIFIER CLOSE_CURLY)?
    ;


