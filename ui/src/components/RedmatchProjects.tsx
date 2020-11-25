/*
 * Copyright © 2020, Commonwealth Scientific and Industrial Research
 * Organisation (CSIRO) ABN 41 687 119 230. All rights reserved.
 */

import React, { Fragment, useState } from "react";
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
import RedmatchApi, { RedmatchProject } from "../api/RedmatchApi";
import env from "@beam-australia/react-env";
import { ApiError } from "./ApiError";
import NewRedmatchProject from "./NewRedmatchProject";
import { Link } from "react-router-dom";

const useStyles = makeStyles({
  content: {
    padding: "16px 0 32px"
  },
  list: {
    margin: "-24px -16px"
  }
});

export default function RedmatchProjects() {
  const redmatchUrl = env('REDMATCH_URL');
  const classes = useStyles();
  const [newProjectOpen, setNewProjectOpen] = useState(false);
  const { status, data, error, refetch } = useQuery<RedmatchProject[], Error>(
    "RedmatchProjects",
    RedmatchApi(redmatchUrl).getProjects
  );

  function renderSecondaryText(redmatchProject: RedmatchProject) {
    return (
      <Fragment>
        <SubdirectoryArrowRight fontSize="inherit" /> {redmatchProject.redcapUrl}
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
          {data.map((redmatchProject, i) => {
            return (
              <ListItem key={i}>
                <ListItemIcon>
                  <Transform />
                </ListItemIcon>
                <ListItemText
                  primary={redmatchProject.name}
                  secondary={renderSecondaryText(redmatchProject)}
                />
                <ListItemSecondaryAction>
                  <Link to={`/project/${redmatchProject.id}`}>
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
          Redmatch projects
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
      <NewRedmatchProject
        open={newProjectOpen}
        onSuccess={handleNewProjectSuccess}
        onCancel={handleNewProjectCancel}
      />
      <ApiError error={error} />
    </Card>
  );
}
