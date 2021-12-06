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
   * semantic token index.
   */
  private static final int[] map = {
    -1, 0, 0, 0, 0, 0, 0, 0, -1, -1,
    4, 4, 4, 4, 0, 0, 0, 0, 0, 4,
    4, 4, 4, 4, 4, 0, 0, 0, 0, 0,
    0, -1, -1, -1, -1, -1, -1, -1, 4, 5,
    2, 6, 7, 7, 7, 2, 3, 1, 1, -1,
    2, 2, 2, -1, -1, -1, 3, 5, -1, 4,
    2, 4, -1, 2, 7, -1, -1, -1
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
   * Maps the Redmatch ANTLR tokens to the semantic tokens declared in the capabilities. These are:<br>
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
   *
   * The following table shows all the mappings:
   *
   * <table>
   *   <tr>
   * 	   <td>ANTLR Token Name</td>
   * 	   <td>ANTLR Token Symbol</td>
   * 	   <td>ANTLR Token Id</td>
   * 	   <td>Semantic Token Name</td>
   * 	   <td>Semantic Token Id</td>
   * 	 </tr>
   * 	 <tr><td>ALIASES</td><td>ALIASES</td><td>1</td><td>keyword</td><td>0</td></tr>
   * 	 <tr><td>SCHEMA</td><td>SCHEMA</td><td>2</td><td>keyword</td><td>0</td></tr>
   * 	 <tr><td>SERVER</td><td>SERVER</td><td>3</td><td>keyword</td><td>0</td></tr>
   * 	 <tr><td>RULES</td><td>RULES</td><td>4</td><td>keyword</td><td>0</td></tr>
   * 	 <tr><td>MAPPINGS</td><td>MAPPINGS</td><td>5</td><td>keyword</td><td>0</td></tr>
   * 	 <tr><td>ELSE</td><td>ELSE</td><td>6</td><td>keyword</td><td>0</td></tr>
   * 	 <tr><td>REPEAT</td><td>REPEAT</td><td>7</td><td>keyword</td><td>0</td></tr>
   * 	 <tr><td>OPEN</td><td>(</td><td>8</td><td>-</td><td>-</td></tr>
   * 	 <tr><td>CLOSE</td><td>)</td><td>9</td><td>-</td><td>-</td></tr>
   * 	 <tr><td>NOT</td><td>^</td><td>10</td><td>operator</td><td>4</td></tr>
   * 	 <tr><td>AND</td><td>&amp;</td><td>11</td><td>operator</td><td>4</td></tr>
   * 	 <tr><td>OR</td><td>|</td><td>12</td><td>operator</td><td>4</td></tr>
   * 	 <tr><td>DOLLAR</td><td>$</td><td>13</td><td>operator</td><td>4</td></tr>
   * 	 <tr><td>TRUE</td><td>TRUE</td><td>14</td><td>keyword</td><td>0</td></tr>
   * 	 <tr><td>FALSE</td><td>FALSE</td><td>15</td><td>keyword</td><td>0</td></tr>
   * 	 <tr><td>NULL</td><td>NULL</td><td>16</td><td>keyword</td><td>0</td></tr>
   * 	 <tr><td>NOTNULL</td><td>NOTNULL</td><td>17</td><td>keyword</td><td>0</td></tr>
   * 	 <tr><td>VALUE</td><td>VALUE</td><td>18</td><td>keyword</td><td>0</td></tr>
   * 	 <tr><td>EQ</td><td>=</td><td>19</td><td>operator</td><td>4</td></tr>
   * 	 <tr><td>NEQ</td><td>!=</td><td>20</td><td>operator</td><td>4</td></tr>
   * 	 <tr><td>LT</td><td>&lt;</td><td>21</td><td>operator</td><td>4</td></tr>
   * 	 <tr><td>GT</td><td>&gt;</td><td>22</td><td>operator</td><td>4</td></tr>
   * 	 <tr><td>LTE</td><td>&lt;=</td><td>23</td><td>operator</td><td>4</td></tr>
   * 	 <tr><td>GTE</td><td>&gt;=</td><td>24</td><td>operator</td><td>4</td></tr>
   * 	 <tr><td>CONCEPT</td><td>CONCEPT</td><td>25</td><td>keyword</td><td>0</td></tr>
   * 	 <tr><td>CONCEPT_SELECTED</td><td>CONCEPT_SELECTED</td><td>26</td><td>keyword</td><td>0</td></tr>
   *   <tr><td>CONCEPT_LITERAL</td><td>CONCEPT_LITERAL</td><td>27</td><td>keyword</td><td>0</td></tr>
   *   <tr><td>CODE</td><td>CODE</td><td>28</td><td>keyword</td><td>0</td></tr>
   * 	 <tr><td>CODE_SELECTED</td><td>CODE_SELECTED</td><td>29</td><td>keyword</td><td>0</td></tr>
   * 	 <tr><td>REF</td><td>REF</td><td>30</td><td>keyword</td><td>0</td></tr>
   * 	 <tr><td>CLOSE_CURLY</td><td>}</td><td>31</td><td>-</td><td>-</td></tr>
   * 	 <tr><td>OPEN_CURLY</td><td>{</td><td>32</td><td>-</td><td>-</td></tr>
   * 	 <tr><td>OPEN_CURLY_DOLLAR</td><td>${</td><td>33</td><td>-</td><td>-</td></tr>
   * 	 <tr><td>DOTDOT</td><td>..</td><td>34</td><td>-</td><td>-</td></tr>
   * 	 <tr><td>COLON</td><td>:</td><td>35</td><td>-</td><td>-</td></tr>
   * 	 <tr><td>COMMA</td><td>,</td><td>36</td><td>-</td><td>-</td></tr>
   * 	 <tr><td>SEMICOLON</td><td>;</td><td>37</td><td>-</td><td>-</td></tr>
   * 	 <tr><td>MAP</td><td>-&gt;</td><td>38</td><td>operator</td><td>4</td></tr>
   * 	 <tr><td>ATTRIBUTE_START</td><td>*</td><td>39</td><td>property</td><td>5</td></tr>
   * 	 <tr><td>SCHEMA_TYPE</td><td></td><td>40</td><td>string</td><td>2</td></tr>
   * 	 <tr><td>RESOURCE</td><td></td><td>41</td><td>class</td><td>6</td></tr>
   * 	 <tr><td>ALIAS</td><td></td><td>42</td><td>variable</td><td>7</td></tr>
   * 	 <tr><td>ID</td><td></td><td>43</td><td>variable</td><td>7</td></tr>
   * 	 <tr><td>REDMATCH_ID</td><td></td><td>44</td><td>variable</td><td>7</td></tr>
   * 	 <tr><td>STRING</td><td></td><td>45</td><td>string</td><td>2</td></tr>
   * 	 <tr><td>NUMBER</td><td></td><td>46</td><td>number</td><td>3</td></tr>
   * 	 <tr><td>COMMENT</td><td></td><td>47</td><td>comment</td><td>1</td></tr>
   * 	 <tr><td>LINE_COMMENT</td><td></td><td>48</td><td>comment</td><td>1</td></tr>
   * 	 <tr><td>WS</td><td></td><td>49</td><td>-</td><td>-</td></tr>
   * 	 <tr><td>DATE</td><td></td><td>50</td><td>string</td><td>2</td></tr>
   * 	 <tr><td>DATETIME</td><td></td><td>51</td><td>string</td><td>2</td></tr>
   * 	 <tr><td>TIME</td><td></td><td>52</td><td>string</td><td>2</td></tr>
   * 	 <tr><td>OPEN_SQ/td><td>[</td><td>53</td><td>-</td><td>-</td></tr>
   *   <tr><td>CLOSE_SQ</td><td>]</td><td>54</td><td>-</td><td>-</td></tr>
   *   <tr><td>DOT</td><td>.</td><td>55</td><td>-</td><td>-</td></tr>
   *   <tr><td>INDEX</td><td></td><td>56</td><td>number</td><td>3</td></tr>
   *   <tr><td>PATH</td><td></td><td>57</td><td>property</td><td>5</td></tr>
   *   <tr><td>WHITE_SPACE</td><td></td><td>58</td><td>-</td><td>-</td></tr>
   *   <tr><td>ATTRIBUTE_END</td><td>=</td><td>59</td><td>operator</td><td>4</td></tr>
   *   <tr><td>CL_STRING</td><td></td><td>60</td><td>string</td><td>2</td></tr>
   * 	 <tr><td>CL_PIPE</td><td>|</td><td>61</td><td>operator</td><td>4</td></tr>
   *   <tr><td>CL_OPEN</td><td>(</td><td>62</td><td>-</td><td>-</td></tr>
   *   <tr><td>CL_PART</td><td></td><td>63</td><td>string</td><td>2</td></tr>
   *   <tr><td>CL_ALIAS</td><td></td><td>64</td><td>variable</td><td>7</td></tr>
   *   <tr><td>CL_WS</td><td></td><td>65</td><td>-</td><td>-</td></tr>
   *   <tr><td>CL_CLOSE</td><td>)</td><td>66</td><td>-</td><td>-</td></tr>
   *   <tr><td>CL_SEMICOLON</td><td>;</td><td>67</td><td>-</td><td>-</td></tr>
   * </table>
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
