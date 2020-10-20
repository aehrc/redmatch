/*
 * Copyright © 2020, Commonwealth Scientific and Industrial Research
 * Organisation (CSIRO) ABN 41 687 119 230. All rights reserved.
 */

import React, { Fragment, useState } from "react";
import {
  Button,
  Dialog,
  DialogActions,
  DialogContent,
  DialogContentText,
  DialogTitle,
  TextField
} from "@material-ui/core";
import { Autocomplete } from "@material-ui/lab";
import { IValueSet } from "@ahryman40k/ts-fhir-types/lib/R4";
import { getOptionSelected } from "./Mappings";

interface Props {
  open: boolean;
  options: IValueSet[];
  onSuccess: (valueSet : IValueSet | null) => void;
  onCancel: () => void;
}

export default function NewRedmatchProject(props: Props) {
  const { open, options, onSuccess, onCancel } = props;
  const [valueSet, setValueSet] = useState<IValueSet | null>(null);

  return (
    <Fragment>
      <Dialog open={open}>
        <DialogTitle>Configuration options</DialogTitle>
        <DialogContent>
          <DialogContentText>
            Select the default value set for the mappings in your project.
          </DialogContentText>
          <Autocomplete
            options={options}
            getOptionLabel={(option) => option ? (option.name ? option.name : '') : ''}
            onChange={(_, value: IValueSet | null) => {
              setValueSet(value);
            }} 
            style={{ width: 300 }}
            value={valueSet}
            getOptionSelected={getOptionSelected}
            renderInput={(params) => <TextField {...params} label="Value Set" variant="outlined"/>}
          />
        </DialogContent>
        <DialogActions>
          <Button onClick={() => onCancel()}>Cancel</Button>
          <Button onClick={() => onSuccess(valueSet)} color="primary">Save</Button>
        </DialogActions>
      </Dialog>
    </Fragment>
  );
}
