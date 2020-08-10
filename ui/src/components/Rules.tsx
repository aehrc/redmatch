import MonacoEditor from "react-monaco-editor";
import * as monacoEditor from "monaco-editor/esm/vs/editor/editor.api";
import { makeStyles } from "@material-ui/core/styles";
import { RedmatchTokensProvider } from "../editor/RedmatchTokensProvider";
import { Box, Toolbar, IconButton } from "@material-ui/core";
import { Button, CircularProgress } from "@material-ui/core";
import React, { useContext, useState } from "react";
import { RedmatchProject } from "../api/RedmatchApi";

const useStyles = makeStyles({
  editor: {
    flexGrow: 1
  }
});

interface Props {
  project: RedmatchProject;
  updateStatus: string;
  onSave: (newRules: string) => void;
}

export default function Rules(props: Props) {
  const classes = useStyles(),
    { project, updateStatus, onSave } = props,
    [request, setRequest] = useState<RedmatchProject>(project),
    [model, setModel] = useState<monacoEditor.editor.ITextModel | null>(null);

  const onChangeRules = (value: string) => {
    request.rulesDocument = value;
    setRequest(request);
  }

  React.useEffect(() => {
    if (model !== undefined) {
      let markers = project.issues.map((item) => {
        return {
          startLineNumber: item.rowStart,
          startColumn: item.colStart,
          endLineNumber: item.rowEnd,
          endColumn: item.colEnd,
          message: item.text,
          severity: item.annotationType === 'ERROR' ? monaco.MarkerSeverity.Error : monaco.MarkerSeverity.Warning
        };
      });
      if (model !== null) {
        monaco.editor.setModelMarkers(model, "owner", markers);
      }
    }
  }, [project]);

  function editorWillMount(monaco: typeof monacoEditor) {
    monaco.languages.register({ id: "redmatch" });
    monaco.languages.setTokensProvider("redmatch", new RedmatchTokensProvider());
    const literalFg = '3b8737';
    const idFg = '344482';
    //const symbolsFg = '000000';
    //const keywordFg = '7132a8';
    const errorFg = 'ff0000';
    monaco.editor.defineTheme('myCoolTheme', {
        base: 'vs',
        inherit: false,
        rules: [
          { token: 'true.rdm',          foreground: literalFg },
          { token: 'false.rdm',         foreground: literalFg },
          { token: 'identifier.rdm',    foreground: idFg,         fontStyle: 'italic' },
          { token: 'unrecognized.rdm',  foreground: errorFg }
        ],
        colors: {
          'literalFg': '#3b8737'
      }
    });
  }

  function editorDidMount(editor: monacoEditor.editor.IStandaloneCodeEditor, _monaco: typeof monacoEditor) {
    let m = editor.getModel();
    if (m !== null) {
      setModel(m);
      console.log('Model: ' + model);
    }
  }

  return (
    <Box>
      <Toolbar>
        <Button
          type="submit"
          onClick={() => onSave(request.rulesDocument)}
          color="primary"
          endIcon={
            updateStatus === "loading" ? (
              <CircularProgress size={20} color="inherit" />
            ) : null
          }
        >
          Save
        </Button>
      </Toolbar>
      <MonacoEditor
        language="redmatch"
        value={request.rulesDocument}
        editorWillMount={editorWillMount}
        editorDidMount={editorDidMount}
        options={{ extraEditorClassName: classes.editor }}
        onChange={onChangeRules}
      />
    </Box>
  );
}
