/*
 * Copyright © 2020, Commonwealth Scientific and Industrial Research
 * Organisation (CSIRO) ABN 41 687 119 230. All rights reserved.
 */

import React, { Fragment, useContext, useState } from "react";
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
import { Config } from "./App";
import { ApiError } from "./ApiError";

interface Props {
  open: boolean;
  onSuccess: () => void;
  onCancel: () => void;
}

export default function NewRedmatchProject(props: Props) {
  const { open, onSuccess, onCancel } = props,
  { redmatchUrl } = useContext(Config),
  initialRequest = {
    name: "",
    reportId: "",
    redcapUrl: "",
    token: ""
  },
  [request, setRequest] = useState<UnsavedRedmatchProject>(initialRequest);

  const handleSuccess = () => {
    setRequest(initialRequest);
    return onSuccess();
  };

  const handleError = () => {
    setRequest(initialRequest);
    return onCancel();
  };

  const [register, { status, error }] = useMutation<
    RedmatchProject,
    UnsavedRedmatchProject
  >(RedmatchApi(redmatchUrl).createProject, {
    onSuccess: handleSuccess,
    onError: handleError
  });

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
