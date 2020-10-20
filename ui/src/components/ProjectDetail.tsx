import React, { ReactNode, useState } from "react";
import { createStyles, makeStyles } from "@material-ui/core/styles";
import {
  AppBar,
  Box,
  Breadcrumbs,
  Button,
  Card,
  Dialog,
  DialogActions,
  DialogContent,
  DialogContentText,
  DialogTitle,
  Link,
  Tab,
  Tabs,
  Typography
} from "@material-ui/core";
import { Link as RouterLink } from "react-router-dom";
import { useQuery, useQueryClient } from "react-query";
import RedmatchApi, { RedmatchProject, Mapping } from "../api/RedmatchApi";
import env from "@beam-australia/react-env";
import { ApiError } from "./ApiError";
import ProjectInfo from "./ProjectInfo";
import ProjectMetadata from "./ProjectMetadata";
import Rules from "./Rules";
import Mappings from "./Mappings";
import { useMutation } from "react-query";
import Export from "./Export";

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
  const classes = useStyles();
  const redmatchUrl = env('REDMATCH_URL');
  const { className, reportId } = props;
  const [activeTab, setActiveTab] = useState<number>(0);
  const { status, data: project, error } = 
    useQuery<RedmatchProject, Error>(
      ["RedmatchProject", reportId], // the query key
      RedmatchApi(redmatchUrl).getProject // the query function
    );
  // Used to warn the user if navigating away of tab with unsaved changes
  const [unsavedMappings, setUnsavedMappings] = useState<boolean>(false);
  const [unsavedRules, setUnsavedRules] = useState<boolean>(false);
  // Used to store the tab number a user is trying to click on. Only used 
  // when there is unsaved data and the user needs to be warned.
  const [targetTab, setTargetTab] = useState<number>(0);
  // Used to show and hide the dialog to warn users about unsaved data
  const [open, setOpen] = React.useState(false);
  // Query client
  const client = useQueryClient();
  
  const handleCancel = () => {
    setOpen(false);
  };

  const handleContinue = () => {
    setOpen(false);
    setActiveTab(targetTab);
  };

  const handleOnSaveRules = (newRules: string) => {
    // Rules were updated so run mutation
    updateRules([reportId, newRules]);
  }

  const handleOnSaveNeededMappings = (saveNeeded: boolean) => {
    setUnsavedMappings(saveNeeded);
  }

  const handleOnSaveNeededRules = (saveNeeded: boolean) => {
    setUnsavedRules(saveNeeded);
  }
  
  const handleOnSaveMappings = (newMappings: Mapping[]) => {
    updateMappings([reportId, newMappings]);
  }

  const handleUpdate = () => {
    updateMetadata([reportId]);
  }

  const { mutateAsync: updateMetadata, status: updateStatusMetadata, error: updateErrorMetadata } = 
    useMutation<RedmatchProject, Error, [string]>(
      RedmatchApi(redmatchUrl).updateMetadata, {
        onSettled: () => {
          client.invalidateQueries('RedmatchProject');
        }
      }
    );

  const { mutateAsync: updateRules, status: updateStatusRules, error: updateErrorRules } = 
    useMutation<RedmatchProject, Error, [string, string]>(
      RedmatchApi(redmatchUrl).updateRules, {
        onMutate: (params: string[]) => {
          client.cancelQueries('RedmatchProject');
          const previousProject = client.getQueryData('RedmatchProject');
      
          // Optimistically update to the new value
          // Need to clone old project and set new rules
          const newProject: RedmatchProject = 
            Object.assign(
              {
                id:'', 
                reportId: '', 
                redcapUrl: '', 
                token: '', 
                name: '', 
                rulesDocument: '', 
                metadata: {
                  fields: []
                },
                mappings: [],
                issues: []
              }, 
              previousProject);
          newProject.rulesDocument = params[1];
          client.setQueryData('RedmatchProject', newProject);
      
          // Return the snapshotted value
          return () => client.setQueryData('RedmatchProject', previousProject);
        },
        onError: (_err: Error, rollback: any) => rollback(),
        onSettled: () => {
          client.invalidateQueries('RedmatchProject')
        }
      }
    );

  const { mutateAsync: updateMappings, status: updateStatusMappings, error: updateErrorMappings } = 
    useMutation<RedmatchProject, Error, [string, Mapping[]]>(
      RedmatchApi(redmatchUrl).updateMappings, {
        onMutate: (params: any[]) => {
          client.cancelQueries('RedmatchProject');
          const previousProject = client.getQueryData('RedmatchProject');
      
          // Optimistically update to the new value
          // Need to clone old project and set new mappings
          const newProject: RedmatchProject = 
            Object.assign(
              {
                id:'', 
                reportId: '', 
                redcapUrl: '', 
                token: '', 
                name: '', 
                rulesDocument: '', 
                metadata: {
                  fields: []
                },
                mappings: [],
                issues: []
              }, 
              previousProject);
          
          newProject.mappings = params[1];
          client.setQueryData('RedmatchProject', newProject);
      
          // Return the snapshotted value
          return () => client.setQueryData('RedmatchProject', previousProject);
        },
        onError: (_err: Error, rollback: any) => rollback(),
        onSettled: () => {
          client.invalidateQueries('RedmatchProject')
        }
      }
    );

  function switchTab(tab: number) {
    if (activeTab === 3 && unsavedMappings && tab !== 3) {
      setTargetTab(tab);
      setOpen(true);
    } else if (activeTab === 2 && unsavedRules && tab !== 2) {
      setTargetTab(tab);
      setOpen(true);
    } else {
      setActiveTab(tab);
    }
  }

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
            <Tab label="Info" onClick={() => switchTab(0)} />
            <Tab label="Metadata" onClick={() => switchTab(1)} />
            <Tab label="Rules" onClick={() => switchTab(2)} />
            <Tab label="Mappings" onClick={() => switchTab(3)} />
            <Tab label="Export" onClick={() => switchTab(4)} />
          </Tabs>
        </AppBar>
        <TabPanel className={classes.tabContent} index={0} value={activeTab}>
          <ProjectInfo project={project} />
        </TabPanel>
        <TabPanel className={classes.tabContent} index={1} value={activeTab}>
          <ProjectMetadata metadata={project.metadata} onUpdate={handleUpdate} status={status} updateStatus={updateStatusMetadata}/>
        </TabPanel>
        <TabPanel className={classes.tabContent} index={2} value={activeTab}>
          <Rules project={project} onSave={handleOnSaveRules} onSaveNeeded={handleOnSaveNeededRules} status={status} updateStatus={updateStatusRules}/>
        </TabPanel>
        <TabPanel className={classes.tabContent} index={3} value={activeTab}>
          <Mappings project={project} onSave={handleOnSaveMappings} onSaveNeeded={handleOnSaveNeededMappings} status={status} updateStatus={updateStatusMappings}/>
        </TabPanel>
        <TabPanel className={classes.tabContent} index={4} value={activeTab}>
          <Export projectId={project.id}/>
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
      <Dialog
        open={open}
        aria-labelledby="alert-dialog-title"
        aria-describedby="alert-dialog-description"
      >
        <DialogTitle id="alert-dialog-title">{"You have unsaved data!"}</DialogTitle>
        <DialogContent>
          <DialogContentText id="alert-dialog-description">
            Data will be lost if you navigate to another tab without saving.
          </DialogContentText>
        </DialogContent>
        <DialogActions>
          <Button onClick={handleCancel} color="primary">
            Cancel
          </Button>
          <Button onClick={handleContinue} color="primary" autoFocus>
            Continue
          </Button>
        </DialogActions>
      </Dialog>
      <ApiError error={error} />
      <ApiError error={updateErrorRules} />
      <ApiError error={updateErrorMetadata} />
      <ApiError error={updateErrorMappings} />
    </Box>
  );
}

function TabPanel(props: TabPanelProps) {
  const { className, children, index, value } = props;
  return index === value ? <Box className={className}>{children}</Box> : null;
}
