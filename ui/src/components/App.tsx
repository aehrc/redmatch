/*
 * Copyright © 2018-2021, Commonwealth Scientific and Industrial Research
 * Organisation (CSIRO) ABN 41 687 119 230. Licensed under the CSIRO Open Source
 * Software Licence Agreement.
 */
import React from "react";
import { Box, createMuiTheme, ThemeProvider, AppBar, Link, Toolbar, Typography, Button } from "@material-ui/core";
import { useKeycloak } from '@react-keycloak/web'
import { makeStyles, Theme, withStyles } from "@material-ui/core/styles";
import { BrowserRouter as Router, Switch } from "react-router-dom";
import Dashboard from "./Dashboard";
import ProjectDetail from "./ProjectDetail";
import { QueryClient, QueryClientProvider } from "react-query";
import logo from './redmatch_logo.png';
import PrivateRoute from "./PrivateRoute";
import { grey } from "@material-ui/core/colors";
import { ReactQueryDevtools } from 'react-query/devtools';

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

const ColorButton = withStyles((theme: Theme) => ({
  root: {
    color: theme.palette.getContrastText(grey[500]),
    backgroundColor: grey[500],
    '&:hover': {
      backgroundColor: grey[700],
    },
  },
}))(Button);

const client = new QueryClient();

export default function App() {
  const classes = useStyles();
  const { initialized, keycloak } = useKeycloak();

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
            {!!keycloak?.authenticated && (
              <ColorButton onClick={() => keycloak.logout()} color="primary">
                Logout
              </ColorButton>
            )}
          </Toolbar>
        </AppBar>
        <Box className={classes.main} component="main">
          <Router>
            <Switch>
              <PrivateRoute path="/project/:id">
                <ProjectDetail className={classes.content} />
              </PrivateRoute>
              <PrivateRoute path="/">
                <Dashboard className={classes.content} />
              </PrivateRoute>
            </Switch>
          </Router>
        </Box>
      </ThemeProvider>
      <ReactQueryDevtools initialIsOpen={false} />
    </QueryClientProvider>
  );
}
