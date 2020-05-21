import { languages } from "monaco-editor";
import IState = languages.IState;

export default class FhircapTokenizerState implements IState {
  clone(): languages.IState {
    return new FhircapTokenizerState();
  }

  equals(_: languages.IState): boolean {
    return true;
  }
}
