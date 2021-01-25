/*
 * Copyright © 2018-2021, Commonwealth Scientific and Industrial Research
 * Organisation (CSIRO) ABN 41 687 119 230. Licensed under the CSIRO Open Source
 * Software Licence Agreement.
 */
import React, { useState } from "react";
import Snackbar from '@material-ui/core/Snackbar';
import MuiAlert, { AlertProps } from '@material-ui/lab/Alert';

function Alert(props: AlertProps) {
  return <MuiAlert elevation={6} variant="filled" {...props} />;
}

interface Props {
  error: Error | null | undefined;
}

export interface Error {
  message: string
}

export function ApiError(props: Props) {
  const { error } = props;
  const [open, setOpen] = useState(true);

  const handleClose = (event: React.SyntheticEvent | React.MouseEvent, reason?: string) => {
    if (reason === 'clickaway') {
      return;
    }
    setOpen(false);
  };

  if (!error) {
    return null;
  }

  return (
    <Snackbar open={open} onClose={handleClose}>
      <Alert onClose={handleClose} severity="error">
        {error.message}
      </Alert>
    </Snackbar>
  );
}
