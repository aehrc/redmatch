/*
 * Copyright © 2018-2021, Commonwealth Scientific and Industrial Research
 * Organisation (CSIRO) ABN 41 687 119 230. Licensed under the CSIRO Open Source
 * Software Licence Agreement.
 */
import { RedmatchProject } from "../api/RedmatchApi";
import { Table, TableBody, TableCell, TableRow, TableHead, Box, Toolbar, Button, CircularProgress, Typography } from "@material-ui/core";
import React from "react";

interface Props {
  project: RedmatchProject;
  status: string;
  updateStatus: string;
  onUpdate: () => void;
}

export default function ProjectMetadata(props: Props) {
  const { project, status, updateStatus, onUpdate } = props;

  function renderContent() {
    if (status === 'loading') {
      return <Typography variant="body1">Loading metadata...</Typography>;
    } else {
      return (<Box>
        <Toolbar>
          <Button
            type="submit"
            onClick={() => onUpdate()}
            color="primary"
            endIcon={
              updateStatus === "loading" ? (
                <CircularProgress size={20} color="inherit" />
              ) : null
            }
          >
            Update
          </Button>
        </Toolbar>
        <Table>
          <TableHead>
            <TableRow>
              <TableCell>Id</TableCell>
              <TableCell>Label</TableCell>
              <TableCell>Type</TableCell>
              <TableCell>Validation</TableCell>
            </TableRow>
          </TableHead>
          <TableBody>
            {project.fields.map((field, i) => {
              return (
                <TableRow key={i}>
                  <TableCell>{field.fieldId}</TableCell>
                  <TableCell>{field.fieldLabel}</TableCell>
                  <TableCell>{field.fieldType}</TableCell>
                  <TableCell>{field.textValidationType}</TableCell>
                </TableRow>
              );
            })}
          </TableBody>
        </Table>
      </Box>
      );
    }
  }

  return renderContent();
}
