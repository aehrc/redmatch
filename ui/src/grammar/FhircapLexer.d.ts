import { InputStream, Lexer } from "antlr4";

export default class FhircapLexer extends Lexer {
  static symbolicNames: string[];
  constructor(inputStream: InputStream);
}
