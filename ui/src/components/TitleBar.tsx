/*
 * Copyright © 2020, Commonwealth Scientific and Industrial Research
 * Organisation (CSIRO) ABN 41 687 119 230. All rights reserved.
 */

import React from "react";
import { AppBar, Toolbar, Typography } from "@material-ui/core";
import logo from './redmatch_logo.png';

interface Props {
  className?: string;
}

export default function TitleBar(props: Props) {
  const { className } = props;

  return (
    <AppBar className={className} position="static">
      <Toolbar>
        <img src={logo} alt="Logo" />
        <Typography variant="h4" component="h1">
          redmatch
        </Typography>
      </Toolbar>
    </AppBar>
  );
}
