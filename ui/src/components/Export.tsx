import { Box, Toolbar, IconButton, Typography, Button, CircularProgress, TextareaAutosize } from "@material-ui/core";
import React, { useContext, useState, useEffect } from "react";
import { IParameters, IOperationOutcome, OperationOutcome_IssueSeverityKind } from "@ahryman40k/ts-fhir-types/lib/R4";
import { Config } from "./App";
import TextField from '@material-ui/core/TextField';
import http from "axios";
import { ApiError } from "./ApiError";

interface Props {
  projectId: string;
}

export default function Export(props: Props) {
  const { projectId } = props;
  const { redmatchUrl } = useContext(Config);
  const { pathlingUrl } = useContext(Config);
  const [value, setValue] = useState('');
  const [status, setStatus] = useState('');
  const [error, setError] = useState<Error | null>(null);

  //useEffect(() =>{
  //
  //});

  const onExport = (projectId: string) => {
    fetchData(projectId)
      .then(() => {
        setStatus('');
      });
  };

  interface FetchDataResponse {
    result: IOperationOutcome,
    error: IOperationOutcome
  }

  const fetchData = async (projectId: string) => {
    let res : FetchDataResponse = { 
      result: { resourceType: 'OperationOutcome', issue: [{ severity: undefined, code: undefined }] }, 
      error: { resourceType: 'OperationOutcome', issue: [{ severity: undefined, code: undefined }] }
    };
    setStatus('loading');
    setValue('Transforming project in Redmatch...');
    try {
      const { data } = await http.post<IParameters>(
        `${redmatchUrl}/project/${projectId}/$transform`,
        null,
        {
          headers: {
            "Accept": "application/json",
            "Content-Type": "application/json"
          }
        }
      );
      setValue(prev => prev + '\nTransformation was successful.\nDownloading file.');
    } catch (error) {
      const e : Error = { 
        name: 'Transformation error', 
        message: 'There was a problem with the transformation. Please check all mappings have been completed.'
      };
      setError(e);
      return;
    }

    // Download ZIP
    try {
      const { data: blobData } = await http({
        method: 'post',
        url: `${redmatchUrl}/project/${projectId}/$export`,
        responseType: 'blob'
      });

      const url = window.URL.createObjectURL(new Blob([blobData]));
      const link = document.createElement('a');
      link.href = url;
      link.setAttribute('download', `${projectId}.zip`);
      document.body.appendChild(link);
      link.click();
      if (link.parentNode) {
        link.parentNode.removeChild(link);
      }
    } catch (error) {
      const e : Error = { 
        name: 'Download error', 
        message: 'There was a problem downloading the file.'
      };
      setError(e);
    }
  }

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
      <ApiError error={error} />
    </Box>
  );
}
