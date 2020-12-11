/*
 * Copyright © 2020, Commonwealth Scientific and Industrial Research
 * Organisation (CSIRO) ABN 41 687 119 230. All rights reserved.
 */

import React from "react";
import { Box, createMuiTheme, ThemeProvider, AppBar, Link, Toolbar, Typography } from "@material-ui/core";
import { useKeycloak } from '@react-keycloak/web'
import { makeStyles } from "@material-ui/core/styles";
import { BrowserRouter as Router, Route, Switch } from "react-router-dom";
import Dashboard from "./Dashboard";
import ProjectDetail from "./ProjectDetail";
import { QueryCache, QueryClient, QueryClientProvider } from "react-query";
import logo from './redmatch_logo.png';
import LoginPage from "./Login";
import PrivateRoute from "./PrivateRoute";

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
  toolbar: {
    flex: 1
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
  const { initialized } = useKeycloak();

  if (!initialized) {
    return <div>Loading...</div>
  }

  return (
    <QueryClientProvider client={client}>
      <ThemeProvider theme={theme}>
        <AppBar className={classes.main} position="static">
          <Toolbar>
            <Link href="/project">
              <img src={logo} alt="Logo" />
            </Link>
            <Typography variant="h4" component="h1" className={classes.toolbar}>
              redmatch
            </Typography>
          </Toolbar>
        </AppBar>
        <Box className={classes.main} component="main">
          <Router>
            <Switch>
              <Route path="/login" component={LoginPage} />
              <Route path="/project/:id">
                <ProjectDetail className={classes.content} />
              </Route>
              <Route path="/home">
                <Dashboard className={classes.content} />
              </Route>
            </Switch>
          </Router>
        </Box>
      </ThemeProvider>
    </QueryClientProvider>
  );
}
