import { RedmatchProject } from "../api/RedmatchApi";
import { Table, TableBody, TableCell, TableRow, TableHead } from "@material-ui/core";
import React from "react";

interface Props {
  project: RedmatchProject;
}

export default function ProjectMetadata(props: Props) {
  const { project } = props;

  return (
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
        {project.metadata.fields.map((field, i) => {
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
  );
}
