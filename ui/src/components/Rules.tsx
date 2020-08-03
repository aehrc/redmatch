import MonacoEditor from "react-monaco-editor";
import * as monacoEditor from "monaco-editor/esm/vs/editor/editor.api";
import { makeStyles } from "@material-ui/core/styles";
import { RedmatchTokensProvider } from "../editor/RedmatchTokensProvider";
import { Box, Toolbar, IconButton } from "@material-ui/core";
import { Button, CircularProgress } from "@material-ui/core";
import React, { Fragment, useContext, useState } from "react";
import { Config } from "./App";
import { ApiError } from "./ApiError";
import { useMutation } from "react-query";
import RedmatchApi, { RedmatchProject } from "../api/RedmatchApi";

const useStyles = makeStyles({
  editor: {
    flexGrow: 1
  }
});

interface Props {
  project: RedmatchProject;
  onSuccess: () => void;
}

export default function Rules(props: Props) {
  const classes = useStyles(),
    { project, onSuccess } = props,
    { redmatchUrl } = useContext(Config),
    [request, setRequest] = useState<RedmatchProject>(project);
    

  const handleSuccess = () => {
    //setRequest(initialRequest);
    return onSuccess();
  };

  const onChangeRules = (value: string) => {
    request.rulesDocument = value;
    setRequest(request);
  }

  const [updateRules, { status: updateStatus, error: updateError }] = 
    useMutation<RedmatchProject,RedmatchProject>(
      RedmatchApi(redmatchUrl).updateRules, {
        onSuccess: handleSuccess
      }
    );

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

  return (
    <Box>
      <Toolbar>
        <Button
          type="submit"
          onClick={() => updateRules(request)}
          color="primary"
          endIcon={
            status === "loading" ? (
              <CircularProgress size={20} color="inherit" />
            ) : null
          }
        >
          Save
        </Button>
      </Toolbar>
      <MonacoEditor
        language="redmatch"
        value={project.rulesDocument}
        editorWillMount={editorWillMount}
        options={{ extraEditorClassName: classes.editor }}
        onChange={onChangeRules}
      />
      <ApiError error={updateError} />
    </Box>
  );
}
