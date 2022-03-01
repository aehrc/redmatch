/*
 * Copyright © 2018-2021, Commonwealth Scientific and Industrial Research Organisation (CSIRO) ABN 41 687 119 230.
 * Licensed under the CSIRO Open Source Software Licence Agreement.
 */
package au.csiro.redmatch.lsp;

import au.csiro.redmatch.grammar.RedmatchLexer;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.Lexer;
import org.antlr.v4.runtime.Token;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.lsp4j.SemanticTokens;

import java.util.ArrayList;
import java.util.List;

/**
 * Tokenises a Redmatch document and return the corresponding semantic tokens.
 *
 * @author Alejandro Metke Jimenez
 */
public class SemanticTokeniser {

  private static final Log log = LogFactory.getLog(SemanticTokeniser.class);

  /**
   * Represents a map between ANTLR tokens and semantic tokens. The index represents the ANTlR token and the value the
   * semantic token index. The declared semantic token types are:
   *
   * <ul>
   *   <li>keyword</li>
   *   <li>comment</li>
   *   <li>string</li>
   *   <li>number</li>
   *   <li>operator</li>
   *   <li>property</li>
   *   <li>class</li>
   *   <li>variable</li>
   * </ul>
   */
  private static final int[] map = {
    -1,
    0, // ALIASES=1
    0, // SCHEMA=2
    0, // SERVER=3
    0, // RULES=4
    0, // MAPPINGS=5
    0, // ELSE=6
    0, // REPEAT=7
    -1, // OPEN=8
    -1, // CLOSE=9
    4, // NOT=10
    4, // AND=11
    4, // OR=12
    4, // DOLLAR=13
    0, // TRUE=14
    0, // FALSE=15
    0, // NULL=16
    0, // NOTNULL=17
    0, // VALUE=18
    4, // EQ=19
    4, // NEQ=20
    4, // LT=21
    4, // GT=22
    4, // LTE=23
    4, // GTE=24
    0, // CONCEPT=25
    0, // CONCEPT_SELECTED=26
    0, // CONCEPT_LITERAL=27
    0, // CODE=28
    0, // CODE_SELECTED=29
    0, // REF=30
    -1, // CLOSE_CURLY=31
    -1, // OPEN_CURLY=32
    -1, // OPEN_CURLY_DOLLAR=33
    -1, // COLON=34
    -1, // COMMA=35
    -1, // SEMICOLON=36
    4, // MAP=37
    5, // ATTRIBUTE_START=38
    0, // TARGET=39
    2, // SCHEMA_TYPE=40
    7, // ALIAS=41
    3, // NUMBER=42
    7, // ID=43
    7, // REDMATCH_ID=44
    2, // STRING=45
    1, // COMMENT=46
    1, // LINE_COMMENT=47
    -1, // WS=48
    2, // DATE=49
    2, // DATETIME=50
    2, // TIME=51
    -1, // OPEN_SQ=52
    -1, // CLOSE_SQ=53
    -1, // DOT=54
    3, // INDEX=55
    5, // PATH=56
    -1, // ATT_WS=57
    4, // ATTRIBUTE_END=58
    2, // CL_STRING=59
    4, // CL_PIPE=60
    -1, // CL_OPEN=61
    2, // CL_PART=62
    7, // CL_ALIAS=63
    -1, // CL_WS=64
    -1, // CL_CLOSE=65
    -1, // CL_SEMICOLON=66
    -1, // C_OPEN=67,
    7, // C_ID=68
    -1, // R_OPEN=69
    3, // R_NUMBER=70
    -1, // DOTDOT=71
    -1 // R_COLON=72
  };

  public static SemanticTokens tokenise(String text) {
    // First we need to get the tokens from the lexer
    List<SemanticToken> tokens = new ArrayList<>();
    final Lexer lexer = new RedmatchLexer(CharStreams.fromString(text));
    for (Token tok : lexer.getAllTokens()) {
      int line = tok.getLine() - 1; // account for 0-index differences
      int startChar = tok.getCharPositionInLine();
      int length = tok.getText().length();
      int tokenType = mapTokenType(tok.getType());
      if (tokenType != -1) {
        tokens.add(new SemanticToken(line, startChar, length, tokenType));
      }
    }

    // Then we need to transform them into deltas
    // { line: 2, startChar:  5, length: 3, tokenType: 0, tokenModifiers: 3 }
    // { deltaLine: 2, deltaStartChar: 5, length: 3, tokenType: 0, tokenModifiers: 3 },
    if (!tokens.isEmpty()) {
      tokens.get(0).deltaLine = tokens.get(0).line;
      tokens.get(0).deltaStartChar = tokens.get(0).startChar;
    }

    for (int i = 1; i < tokens.size(); i++) {
      SemanticToken prevToken = tokens.get(i -1);
      SemanticToken token = tokens.get(i);

      token.deltaLine = token.line - prevToken.line;

      // Now we need to "deltify" the current token
      if (prevToken.line == token.line) {
        // They are on the same line, so we need to make the start token relative
        token.deltaStartChar = token.startChar - prevToken.startChar;
      } else {
        // There was a change of line so the first token delta start char is the same as the start char
        token.deltaStartChar = token.startChar;
      }
    }

    log.info("Encoding " + tokens.size() + " semantic tokens");

    // Finally, we transform them into the format expected by the language server protocol
    List<Integer> encodedTokens = new ArrayList<>();
    for (SemanticToken token : tokens) {
      encodedTokens.add(token.deltaLine);
      encodedTokens.add(token.deltaStartChar);
      encodedTokens.add(token.length);
      encodedTokens.add(token.tokenType);
      encodedTokens.add(0); // No modifiers for Redmatch
    }

    return new SemanticTokens(encodedTokens);
  }

  /**
   * Maps the Redmatch ANTLR tokens to the semantic tokens declared in the capabilities.
   *
   * @param antlrTokenType The Redmatch ANTLR token type.
   * @return The corresponding semantic token type.
   */
  private static int mapTokenType(int antlrTokenType) {
    if (antlrTokenType >= map.length || antlrTokenType < 0) {
      return -1;
    } else {
      return map[antlrTokenType];
    }
  }

  private static class SemanticToken {
    private final int line;
    private final int startChar;
    private final int length;
    private final int tokenType;
    private int deltaLine;
    private int deltaStartChar;

    public SemanticToken(int line, int startChar, int length, int tokenType) {
      this.line = line;
      this.startChar = startChar;
      this.length = length;
      this.tokenType = tokenType;
    }
  }
}
