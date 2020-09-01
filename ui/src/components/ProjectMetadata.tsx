import { Metadata } from "../api/RedmatchApi";
import { Table, TableBody, TableCell, TableRow, TableHead, Box, Toolbar, Button, CircularProgress } from "@material-ui/core";
import React from "react";

interface Props {
  metadata: Metadata;
  updateStatus: string;
  onUpdate: () => void;
}

export default function ProjectMetadata(props: Props) {
  const { metadata, updateStatus, onUpdate } = props;

  return (
    <Box>
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
          {metadata.fields.map((field, i) => {
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
