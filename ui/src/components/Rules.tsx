import MonacoEditor from "react-monaco-editor";
import * as monacoEditor from "monaco-editor/esm/vs/editor/editor.api";
import { makeStyles } from "@material-ui/core/styles";
import { RedmatchTokensProvider } from "../editor/RedmatchTokensProvider";
import { Box, Toolbar, Typography } from "@material-ui/core";
import { Button, CircularProgress } from "@material-ui/core";
import React, { useState } from "react";
import { RedmatchProject } from "../api/RedmatchApi";

const useStyles = makeStyles({
  editor: {
    flexGrow: 1
  }
});

interface Props {
  project: RedmatchProject;
  status: string;
  updateStatus: string;
  onSave: (newRules: string) => void;
  onSaveNeeded: (saveNeeded: boolean) => void;
}

export default function Rules(props: Props) {
  const classes = useStyles();
  const { project, status, updateStatus, onSave, onSaveNeeded } = props;
  const [rules, setRules] = useState<string>(project.rulesDocument);
  const [model, setModel] = useState<monacoEditor.editor.ITextModel | null>(null);
  // Used to determine if saving is required
  const [saveNeeded, setSaveNeeded] = useState<boolean>(false);

  const onChangeRules = (value: string) => {
    setRules(value);
    let required = false;
    if (project.rulesDocument !== value) {
      required = true;
    }
    setSaveNeeded(required);
    onSaveNeeded(required);
  }

  /*
   * Runs every time the project is updated and process any error markers.
   */
  React.useEffect(() => {
    if (model) {
      let markers = getMarkers(project);
      monaco.editor.setModelMarkers(model, "owner", markers);
    }
  }, [model, project]);

  /**
   * Sets ups syntax highlighting.
   * 
   * @param monaco The Monaco editor instance.
   */
  function editorWillMount(monaco: any) {
    monaco.languages.register({ id: "redmatch" });
    monaco.languages.setTokensProvider("redmatch", new RedmatchTokensProvider());
    monaco.editor.defineTheme('redmatchTheme', {
        base: 'vs',
        inherit: false,
        rules: [
          { token: 'comment.rdm',          foreground: '3bb517' },
          { token: 'line_comment.rdm',     foreground: '3bb517' },
          { token: 'attribute_start.rdm',  foreground: 'b52217' },
          { token: 'index.rdm',            foreground: 'dc8426' },
          { token: 'true.rdm',             foreground: '7707a4' },
          { token: 'false.rdm',            foreground: '7707a4' },
          { token: 'value.rdm',            foreground: '7707a4' },
          { token: 'not.rdm',              foreground: '7707a4' },
          { token: 'and.rdm',              foreground: '7707a4' },
          { token: 'or.rdm',               foreground: '7707a4' },
          { token: 'null.rdm',             foreground: '7707a4' },
          { token: 'notnull.rdm',          foreground: '7707a4' },
          { token: 'ref.rdm',              foreground: '7707a4' },
          { token: 'concept.rdm',          foreground: '7707a4' },
          { token: 'concept_selected.rdm', foreground: '7707a4' },
          { token: 'code_selected.rdm',    foreground: '7707a4'},
          { token: 'concept_literal.rdm',  foreground: 'c9bc19', fontStyle: 'italic' },
          { token: 'code_literal.rdm',     foreground: 'c9bc19', fontStyle: 'italic' },
          { token: 'string.rdm',           foreground: 'dc8426'},
          { token: 'number.rdm',           foreground: 'dc8426'},
          { token: 'code_value.rdm',       foreground: 'dc8426'},
          { token: 'id.rdm',               foreground: '2432e8' },
          { token: 'then.rdm',             foreground: '33bc48'},
          { token: 'unrecognized.rdm',     foreground: 'e83f24' }
        ],
        colors: {
          'literalFg': '#3b8737'
      }
    });
  }

  /*
   * Creates the Monaco editor model.
   */
  function editorDidMount(editor: any, _monaco: any) {
    var model = monaco.editor.createModel(rules, "redmatch");
    editor.setModel(model);
    setModel(model);
    let markers = getMarkers(project);
    monaco.editor.setModelMarkers(model, "owner", markers);
  }

  /*
   * Transforms the errors in a Redmatch project into the format required by Monaco.
   *  
   * @param project 
   */
  function getMarkers (project: RedmatchProject) {
    return project.issues.map((item) => {
      return {
        startLineNumber: item.rowStart,
        startColumn: item.colStart + 1,
        endLineNumber: item.rowEnd,
        endColumn: item.colEnd + 1,
        message: item.text,
        severity: item.annotationType === 'ERROR' ? monaco.MarkerSeverity.Error : monaco.MarkerSeverity.Warning
      };
    });
  }

  function renderContent() {
    if (status === 'loading') {
      return <Typography variant="body1">Loading metadata...</Typography>;
    } else {
      return (
        <Box>
          <Toolbar>
            <Button
              type="submit"
              disabled={!saveNeeded}
              onClick={() => {
                setSaveNeeded(false);
                onSaveNeeded(false);
                onSave(rules);
              }}
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
            height="800px"
            language="redmatch"
            theme="redmatchTheme"
            value={rules}
            editorWillMount={editorWillMount}
            editorDidMount={editorDidMount}
            options={{ extraEditorClassName: classes.editor }}
            onChange={onChangeRules}
          />
        </Box>
      );
    }
  }

  return renderContent();
}
