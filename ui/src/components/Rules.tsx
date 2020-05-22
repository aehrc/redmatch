import React from "react";
import MonacoEditor from "react-monaco-editor";
import * as monacoEditor from "monaco-editor/esm/vs/editor/editor.api";
import { FhircapProject } from "../api/FhircapApi";
import { makeStyles } from "@material-ui/core/styles";
import FhircapTokensProvider from "../editor/FhircapTokensProvider";

const useStyles = makeStyles({
  editor: {
    flexGrow: 1
  }
});

interface Props {
  project: FhircapProject;
}

export default function Rules(props: Props) {
  const classes = useStyles(),
    { project } = props;

  function editorWillMount(monaco: typeof monacoEditor) {
    monaco.languages.register({ id: "fhircap" });
    monaco.languages.setTokensProvider("fhircap", new FhircapTokensProvider());
  }

  return (
    <MonacoEditor
      language="fhircap"
      value={project.rulesDocument}
      editorWillMount={editorWillMount}
      options={{ extraEditorClassName: classes.editor }}
    />
  );
}
