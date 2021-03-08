/*
 * Copyright © 2018-2021, Commonwealth Scientific and Industrial Research
 * Organisation (CSIRO) ABN 41 687 119 230. Licensed under the CSIRO Open Source
 * Software Licence Agreement.
 */
import React, { Fragment, useState } from "react";
import { useMutation } from "react-query";
import {
  Button,
  CircularProgress,
  Dialog,
  DialogActions,
  DialogContent,
  DialogContentText,
  DialogTitle,
  TextField
} from "@material-ui/core";
import RedmatchApi, {
  RedmatchProject,
  UnsavedRedmatchProject
} from "../api/RedmatchApi";
import { ApiError } from "./ApiError";

interface Props {
  open: boolean;
  onSuccess: () => void;
  onCancel: () => void;
}

export default function NewRedmatchProject(props: Props) {
  const { open, onSuccess, onCancel } = props;

  const initialRequest = {
    name: "",
    reportId: "",
    redcapUrl: "",
    token: ""
  };
  const [request, setRequest] = useState<UnsavedRedmatchProject>(initialRequest);

  const {mutate: register, status, error } = useMutation<RedmatchProject, Error, UnsavedRedmatchProject>(
    RedmatchApi().createProject, {
      onError: (error) => {
        console.log('Unable to create new project: ' + error);
      },
      onSuccess: () => {
        setRequest(initialRequest);
        return onSuccess();
      }
    }
  );

  function renderTextField(
    name: string,
    field: keyof UnsavedRedmatchProject,
    autoFocus: boolean = false
  ) {
    return (
      <TextField
        required
        autoFocus={autoFocus}
        margin="dense"
        label={name}
        inputProps={{ "aria-label": name }}
        fullWidth
        value={request[field]}
        onChange={event =>
          setRequest({ ...request, [field]: event.target.value })
        }
      />
    );
  }

  return (
    <Fragment>
      <Dialog open={open}>
        <DialogTitle>Create new Redmatch project</DialogTitle>
        <DialogContent>
          <DialogContentText>
            Enter the details of your Redmatch project.
          </DialogContentText>
          {renderTextField("Name", "name", true)}
          {renderTextField("Report ID", "reportId")}
          {renderTextField("REDCap URL", "redcapUrl")}
          {renderTextField("REDCap Token", "token")}
        </DialogContent>
        <DialogActions>
          <Button onClick={() => onCancel()}>Cancel</Button>
          <Button
            type="submit"
            onClick={() => register(request)}
            color="primary"
            endIcon={
              status === "loading" ? (
                <CircularProgress size={20} color="inherit" />
              ) : null
            }
          >
            Save
          </Button>
        </DialogActions>
      </Dialog>
      <ApiError error={error} />
    </Fragment>
  );
}
