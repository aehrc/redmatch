/*
 * Copyright © 2018-2021, Commonwealth Scientific and Industrial Research
 * Organisation (CSIRO) ABN 41 687 119 230. Licensed under the CSIRO Open Source
 * Software Licence Agreement.
 */
import { RedmatchProject } from "../api/RedmatchApi";
import { Table, TableBody, TableCell, TableRow } from "@material-ui/core";
import React from "react";

interface Props {
  project: RedmatchProject;
}

export default function ProjectInfo(props: Props) {
  const { project } = props;

  return (
    <Table>
      <TableBody>
        <TableRow>
          <TableCell>Project ID</TableCell>
          <TableCell>{project.id}</TableCell>
        </TableRow>
        <TableRow>
          <TableCell>Name</TableCell>
          <TableCell>{project.name}</TableCell>
        </TableRow>
        <TableRow>
          <TableCell>REDCap URL</TableCell>
          <TableCell>{project.redcapUrl}</TableCell>
        </TableRow>
        <TableRow>
          <TableCell>Report ID</TableCell>
          <TableCell>{project.reportId}</TableCell>
        </TableRow>
      </TableBody>
    </Table>
  );
}
