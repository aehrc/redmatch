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
          startColumn: item.colStart + 1,
          endLineNumber: item.rowEnd,
          endColumn: item.colEnd + 1,
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
    monaco.editor.defineTheme('redmatchTheme', {
        base: 'vs',
        inherit: false,
        rules: [
          { token: 'true.rdm',             foreground: '7707a4' },
          { token: 'false.rdm',            foreground: '7707a4' },
          { token: 'value.rdm',            foreground: '7707a4' },
          { token: 'not.rdm',              foreground: '7707a4' },
          { token: 'and.rdm',              foreground: '7707a4' },
          { token: 'or.rdm',               foreground: '7707a4' },
          { token: 'null.rdm',             foreground: '7707a4' },
          { token: 'notnull.rdm',          foreground: '7707a4' },
          { token: 'ref.rdm',              foreground: '7707a4' },
          { token: 'concept_literal.rdm',  foreground: '7707a4' },
          { token: 'code_literal.rdm',     foreground: '7707a4' },
          { token: 'concept.rdm',          foreground: '7707a4' },
          { token: 'concept_selected.rdm', foreground: '7707a4' },
          { token: 'code_selected.rdm',    foreground: '7707a4' },
          { token: 'string.rdm',           foreground: 'dc8426'},
          { token: 'number.rdm',           foreground: 'dc8426'},
          { token: 'code_value.rdm',       foreground: 'dc8426'},
          { token: 'comment.rdm',          foreground: '2ad121'},
          { token: 'line_comment.rdm',     foreground: '2ad121'},
          { token: 'identifier.rdm',       foreground: '2432e8' },
          { token: 'then.rdm',             foreground: '33bc48'},
          { token: 'unrecognized.rdm',     foreground: 'e83f24' }
        ],
        colors: {
          'literalFg': '#3b8737'
      }
    });
  }

  function editorDidMount(editor: monacoEditor.editor.IStandaloneCodeEditor, _monaco: typeof monacoEditor) {
    let m = editor.getModel();
    console.log('editorDidMount, model: ' + model);
    setModel(m);
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
        theme="redmatchTheme"
        value={request.rulesDocument}
        editorWillMount={editorWillMount}
        editorDidMount={editorDidMount}
        options={{ extraEditorClassName: classes.editor }}
        onChange={onChangeRules}
      />
    </Box>
  );
}
