/*
 * Copyright © 2018-2021, Commonwealth Scientific and Industrial Research
 * Organisation (CSIRO) ABN 41 687 119 230. Licensed under the CSIRO Open Source
 * Software Licence Agreement.
 */
import { Box, Toolbar, Button, CircularProgress } from "@material-ui/core";
import React, { useState } from "react";
import TextField from '@material-ui/core/TextField';
import { ApiError } from "./ApiError";
import { useAxios } from "../utils/hooks";
import { IParameters } from "@ahryman40k/ts-fhir-types/lib/R4";

interface Props {
  projectId: string;
  onExportStarted: () => void;
  onExportFinished: () => void;
}

export default function Export(props: Props) {
  const { projectId, onExportStarted, onExportFinished } = props;
  const [value, setValue] = useState('');
  const [status, setStatus] = useState('');
  const [error, setError] = useState<Error | null>(null);
  const axiosInstance = useAxios();

  const onExport = (projectId: string) => {
    fetchData(projectId)
      .then(() => {
        setStatus('');
      });
  };

  const fetchData = async (projectId: string) => {
    setStatus('loading');
    setValue('Transforming project in Redmatch...');

    let http = undefined;
    if (axiosInstance.current) {
      http = axiosInstance.current;
    } else {
      throw new Error('Undefined Axios current instance.');
    }
        
    try {
      await http.post<IParameters>(
        `/project/${projectId}/$transform`,
        null,
        {
          headers: {
            "Accept": "application/json",
            "Content-Type": "application/json"
          }
        }
      );
      setValue(prev => prev + '\nTransformation was successful.\nDownloading file.');

      // Download ZIP
      const { data: blobData } = await http({
        method: 'post',
        url: `/project/${projectId}/$export`,
        responseType: 'blob',
        headers: {
          "Accept": "application/zip"
        }
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
      console.log(error);
      const e : Error = { 
        name: 'EXPORTING error', 
        message: 'There was a problem exporting the FHIR resources.'
      };
      setError(e);
    } finally {
      onExportFinished();
    }
  }

  return (
    <Box>
      <Toolbar>
        <Button
          type="submit"
          onClick={() => {
            onExportStarted();
            onExport(projectId);
          }}
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
