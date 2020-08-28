import { Box, Toolbar, IconButton, Typography, Button, CircularProgress, TextareaAutosize } from "@material-ui/core";
import React, { useContext, useState, useEffect } from "react";
import { IBundle } from "@ahryman40k/ts-fhir-types/lib/R4";
import { Config } from "./App";
import TextField from '@material-ui/core/TextField';
import http from "axios";

interface Props {
  projectId: string;
}

export default function Export(props: Props) {
  const { projectId } = props;
  const { redmatchUrl } = useContext(Config);
  const [value, setValue] = useState('');
  const [status, setStatus] = useState('');

  const onExport = (projectId: string) => {
    console.log('Exporting project ' + projectId);
    fetchData(projectId)
      .then((response: FetchDataResponse) => {
        if (response.error) {
          setValue(JSON.stringify(response.error, null, 2));
        } else if(response.result) {
          setValue(JSON.stringify(response.result, null, 2));
        }
      });
  };

  interface FetchDataResponse {
    result: IBundle,
    error: any
  }

  const fetchData = async (projectId: string) => {
    let res : FetchDataResponse = { result: {resourceType: 'Bundle'}, error: null };
    try {
      setStatus('loading');
      const { data } = await http.post<IBundle>(
        `${redmatchUrl}/project/${projectId}/$transform`,
        null,
        {
          headers: {
            "Accept": "application/json",
            "Content-Type": "application/json"
          }
        }
      );
      setStatus('');
      res.result = data;
      return res;
    } catch (err) {
      res.error = err;
      return  res;
    }
  };

  return (
    <Box>
      <Toolbar>
        <Button
          type="submit"
          onClick={() => onExport(projectId)}
          color="primary"
          endIcon={
            status === "loading" ? (
              <CircularProgress size={20} color="inherit" />
            ) : null
          }
        >
          Export
        </Button>
      </Toolbar>
      <TextField
        inputProps={{
          readOnly: true,
          disabled: true,
        }}
        variant="outlined"
        fullWidth={true}
        multiline={true}
        rows={30}
        rowsMax={30}
        aria-label="FHIR"
        value={value}
      />
    </Box>
  );
}
