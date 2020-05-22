/*
 * Copyright © 2020, Commonwealth Scientific and Industrial Research
 * Organisation (CSIRO) ABN 41 687 119 230. All rights reserved.
 */

import React, { Fragment, useContext, useState } from "react";
import {
  Box,
  Button,
  Card,
  CardContent,
  IconButton,
  List,
  ListItem,
  ListItemIcon,
  ListItemSecondaryAction,
  ListItemText,
  Typography
} from "@material-ui/core";
import {
  Add,
  Launch,
  SubdirectoryArrowRight,
  Transform
} from "@material-ui/icons";
import { makeStyles } from "@material-ui/core/styles";
import { useQuery } from "react-query";
import FhircapApi, { FhircapProject } from "../api/FhircapApi";
import { Config } from "./App";
import { ApiError } from "./ApiError";
import NewFhircapProject from "./NewFhircapProject";
import { Link } from "react-router-dom";

const useStyles = makeStyles({
  content: {
    padding: "16px 0 32px"
  },
  list: {
    margin: "-24px -16px"
  }
});

export default function FhircapProjects() {
  const { fhircapUrl } = useContext(Config),
    classes = useStyles(),
    [newProjectOpen, setNewProjectOpen] = useState(false),
    { status, data, error, refetch } = useQuery<FhircapProject[], string>(
      "FhircapProjects",
      FhircapApi(fhircapUrl).getProjects
    );

  function renderSecondaryText(fhircapProject: FhircapProject) {
    return (
      <Fragment>
        <SubdirectoryArrowRight fontSize="inherit" /> {fhircapProject.redcapUrl}
      </Fragment>
    );
  }

  function renderContent() {
    if (status === "loading") {
      return <Typography variant="body1">Loading projects...</Typography>;
    } else if (!data || data.length < 1) {
      return (
        <Typography variant="body1">No projects yet. Create one!</Typography>
      );
    } else {
      return (
        <List className={classes.list}>
          {data.map((fhircapProject, i) => {
            return (
              <ListItem key={i}>
                <ListItemIcon>
                  <Transform />
                </ListItemIcon>
                <ListItemText
                  primary={fhircapProject.name}
                  secondary={renderSecondaryText(fhircapProject)}
                />
                <ListItemSecondaryAction>
                  <Link to={`/project/${fhircapProject.reportId}`}>
                    <IconButton title="Launch this project">
                      <Launch />
                    </IconButton>
                  </Link>
                </ListItemSecondaryAction>
              </ListItem>
            );
          })}
        </List>
      );
    }
  }

  const handleNewProjectOpen = () => setNewProjectOpen(true);

  const handleNewProjectCancel = () => setNewProjectOpen(false);

  const handleNewProjectSuccess = () => {
    handleNewProjectCancel();
    return refetch();
  };

  return (
    <Card>
      <CardContent>
        <Typography variant="h6" component="h2">
          FHIRCap projects
        </Typography>
        <Box className={classes.content}>{renderContent()}</Box>
        <Button
          variant="contained"
          startIcon={<Add />}
          onClick={() => handleNewProjectOpen()}
        >
          Add project
        </Button>
      </CardContent>
      <NewFhircapProject
        open={newProjectOpen}
        onSuccess={handleNewProjectSuccess}
        onCancel={handleNewProjectCancel}
      />
      <ApiError error={error} />
    </Card>
  );
}
