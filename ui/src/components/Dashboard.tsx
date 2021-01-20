/*
 * Copyright © 2018-2021, Commonwealth Scientific and Industrial Research
 * Organisation (CSIRO) ABN 41 687 119 230. Licensed under the CSIRO Open Source
 * Software Licence Agreement.
 */
import { Grid } from "@material-ui/core";
import RedmatchProjects from "./RedmatchProjects";
import React from "react";

interface Props {
  className?: string;
}

export default function Dashboard(props: Props) {
  const { className } = props;

  return (
    <Grid className={className} container spacing={3}>
      <Grid item xs={12} md={6}>
        <RedmatchProjects />
      </Grid>
    </Grid>
  );
}
