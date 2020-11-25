import { Snackbar } from "@material-ui/core";
import { Alert } from "@material-ui/lab";
import React from "react";

interface Props {
  error: Error | null | undefined;
}

export interface Error {
  message: string
}

export function ApiError(props: Props) {
  const { error } = props;

  if (!error) return null;
  return (
    <Snackbar open={true}>
      <Alert severity="error" variant="filled">
        {error.message}
      </Alert>
    </Snackbar>
  );
}
