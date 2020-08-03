import React, { ReactNode, useContext, useState } from "react";
import { createStyles, makeStyles } from "@material-ui/core/styles";
import {
  AppBar,
  Box,
  Breadcrumbs,
  Card,
  Link,
  Tab,
  Tabs,
  Typography
} from "@material-ui/core";
import { Link as RouterLink } from "react-router-dom";
import { useQuery } from "react-query";
import RedmatchApi, { RedmatchProject } from "../api/RedmatchApi";
import { Config } from "./App";
import { ApiError } from "./ApiError";
import ProjectInfo from "./ProjectInfo";
import Rules from "./Rules";


const useStyles = makeStyles(theme =>
  createStyles({
    link: {
      color: theme.palette.primary.main,
      textDecoration: "none",
      "&:hover": {
        textDecoration: "underline"
      }
    },
    root: {
      display: "flex",
      flexDirection: "column"
    },
    breadcrumbs: {
      marginBottom: "16px"
    },
    content: {
      flexGrow: 1,
      display: "flex",
      flexDirection: "column"
    },
    tabContent: {
      flexGrow: 1,
      padding: "16px"
    }
  })
);

interface Props {
  className?: string;
  reportId: string;
}

interface TabPanelProps {
  className?: string;
  children?: ReactNode;
  index: any;
  value: any;
}

export default function ProjectDetail(props: Props) {
  const classes = useStyles(),
  { redmatchUrl } = useContext(Config),
  { className, reportId } = props,
  [activeTab, setActiveTab] = useState<number>(0),
  { status, data: project, error, refetch } = 
    useQuery<RedmatchProject, [string, string]>(
      ["RedmatchProject", reportId], RedmatchApi(redmatchUrl).getProject
    );


  const handleRulesSave = () => {
    return refetch();
  };

  function renderProjectDetail() {
    if (!project) {
      return (
        <Typography className={classes.content} variant="body1">
          No data
        </Typography>
      );
    }
    return (
      <Card className={classes.content}>
        <AppBar position="static" color="inherit">
          <Tabs value={activeTab}>
            <Tab label="Info" onClick={() => setActiveTab(0)} />
            <Tab label="Rules" onClick={() => setActiveTab(1)} />
          </Tabs>
        </AppBar>
        <TabPanel className={classes.tabContent} index={0} value={activeTab}>
          <ProjectInfo project={project} />
        </TabPanel>
        <TabPanel className={classes.tabContent} index={1} value={activeTab}>
          <Rules project={project} onSuccess={handleRulesSave}/>
        </TabPanel>
      </Card>
    );
  }

  return (
    <Box className={`${className} ${classes.root}`}>
      <Breadcrumbs className={classes.breadcrumbs}>
        <Link
          variant="body1"
          component={RouterLink}
          to="/"
        >Home</Link>
        <Typography variant="body1">Projects</Typography>
        <Typography variant="body1">{reportId}</Typography>
      </Breadcrumbs>
      {status === "loading" ? (
        <Typography className={classes.content} variant="body1">
          Loading project...
        </Typography>
      ) : (
        renderProjectDetail()
      )}
      <ApiError error={error} />
    </Box>
  );
}

function TabPanel(props: TabPanelProps) {
  const { className, children, index, value } = props;
  return index === value ? <Box className={className}>{children}</Box> : null;
}
