/*
 * Copyright © 2020, Commonwealth Scientific and Industrial Research
 * Organisation (CSIRO) ABN 41 687 119 230. All rights reserved.
 */

import React from "react";
import TitleBar from "./TitleBar";
import { Box, createMuiTheme, ThemeProvider } from "@material-ui/core";
import { makeStyles } from "@material-ui/core/styles";
import { BrowserRouter as Router, Route, Switch } from "react-router-dom";
import Dashboard from "./Dashboard";
import ProjectDetail from "./ProjectDetail";
import { QueryCache, QueryClient, QueryClientProvider } from "react-query";

const theme = createMuiTheme({
  palette: {
    primary: { main: "#9a0036" }
  }
});

const useStyles = makeStyles({
  "@global": {
    "html, body, #app": {
      margin: 0,
      padding: 0,
      height: "100%"
    },
    body: {
      background: theme.palette.grey["200"]
    },
    "#app": {
      display: "flex",
      flexDirection: "column"
    }
  },
  main: {
    flexGrow: 1,
    display: "flex",
    flexDirection: "column"
  },
  content: {
    flexGrow: 1,
    padding: "24px"
  }
});

const cache = new QueryCache();
const client = new QueryClient({ cache });

export default function App() {
  const classes = useStyles();

  return (
    <QueryClientProvider client={client}>
      <ThemeProvider theme={theme}>
        <TitleBar />
        <Box className={classes.main} component="main">
          <Router>
            <Switch>
              <Route
                path="/project/:id"
                render={({ match }) => (
                  <ProjectDetail
                    className={classes.content}
                    reportId={match.params.id}
                  />
                )}
              />
              <Route path="/">
                <Dashboard className={classes.content} />
              </Route>
            </Switch>
          </Router>
        </Box>
      </ThemeProvider>
    </QueryClientProvider>
  );
}
