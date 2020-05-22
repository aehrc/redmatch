import { InputStream } from "antlr4";
import { languages } from "monaco-editor";
import FhircapLexer from "../grammar/FhircapLexer";
import FhircapTokenizerState from "./FhircapTokenizerState";
import TokensProvider = languages.TokensProvider;

export default class FhircapTokensProvider implements TokensProvider {
  getInitialState(): languages.IState {
    return new FhircapTokenizerState();
  }

  tokenize(line: string, _: languages.IState): languages.ILineTokens {
    const inputStream = new InputStream(line);
    const lexer = new FhircapLexer(inputStream);
    const tokens = lexer.getAllTokens();
    return {
      tokens: tokens.map(token => ({
        startIndex: token.column,
        scopes: FhircapLexer.symbolicNames[token.type]
      })),
      endState: new FhircapTokenizerState()
    };
  }
}
