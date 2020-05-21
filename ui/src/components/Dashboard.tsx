import { Grid } from "@material-ui/core";
import FhircapProjects from "./FhircapProjects";
import React from "react";

interface Props {
  className?: string;
}

export default function Dashboard(props: Props) {
  const { className } = props;

  return (
    <Grid className={className} container spacing={3}>
      <Grid item xs={12} md={6}>
        <FhircapProjects />
      </Grid>
    </Grid>
  );
}
