/// <reference path="../../node_modules/monaco-editor/monaco.d.ts" />

import { ANTLRInputStream, ANTLRErrorListener } from 'antlr4ts';
import ILineTokens = monaco.languages.ILineTokens;
import IToken = monaco.languages.IToken;
import { RedmatchLexer } from "../grammar/au/csiro/redmatch/grammar/RedmatchLexer";
import { Token } from 'monaco-editor';

export class RedmatchState implements monaco.languages.IState {
    clone(): monaco.languages.IState {
        return new RedmatchState();
    }
    equals(_other: monaco.languages.IState): boolean {
        return true;
    }
}

export class RedmatchTokensProvider implements monaco.languages.TokensProvider {
    getInitialState(): monaco.languages.IState {
        return new RedmatchState();
    }
    tokenize(line: string, _state: monaco.languages.IState): monaco.languages.ILineTokens {
        // So far we ignore the state, which is not great for performance reasons
        return tokensForLine(line);
    }
}
const EOF = -1;
class RedmatchToken implements IToken {
    scopes: string;
    startIndex: number;
    constructor(ruleName: string, startIndex: number) {
        this.scopes = ruleName.toLowerCase() + ".rdm";
        this.startIndex = startIndex;
    }
}
class RedmtachLineTokens implements ILineTokens {
    endState: monaco.languages.IState;
    tokens: monaco.languages.IToken[];
    constructor(tokens: monaco.languages.IToken[]) {
        this.endState = new RedmatchState();
        this.tokens = tokens;
    }
}
export function tokensForLine(input: string): monaco.languages.ILineTokens {
    var errorStartingPoints : number[] = []
    class ErrorCollectorListener implements ANTLRErrorListener<Token> {
        syntaxError(_recognizer: any, _offendingSymbol: any, _line: any, charPositionInLine: number, _msg: any, _e: any) {
            errorStartingPoints.push(charPositionInLine);
        }
    }
    const inputStream = new ANTLRInputStream(input);
    const lexer = new RedmatchLexer(inputStream);
    lexer.removeErrorListeners();
    let errorListener = new ErrorCollectorListener();
    lexer.addErrorListener(errorListener);
    let done = false;
    let myTokens: monaco.languages.IToken[] = [];
    do {
        let token = lexer.nextToken();
        if (token == null) {
            done = true
        } else {
            // We exclude EOF
            if (token.type == EOF) {
                done = true;
            } else {
                const tokenTypeName = RedmatchLexer.VOCABULARY.getSymbolicName(token.type);
                if (tokenTypeName !== undefined) {
                  let myToken = new RedmatchToken(tokenTypeName, token.charPositionInLine);
                  myTokens.push(myToken);
              }
            }
        }
    } while (!done);
    // Add all errors
    for (let e of errorStartingPoints) {
        myTokens.push(new RedmatchToken("error.rdm", e));
    }
    myTokens.sort((a, b) => (a.startIndex > b.startIndex) ? 1 : -1)
    return new RedmtachLineTokens(myTokens);
  }
