import { Box, Toolbar, IconButton, Typography, Button, CircularProgress } from "@material-ui/core";
import React, { useContext, useState } from "react";
import { RedmatchProject, Mapping } from "../api/RedmatchApi";
import { Table, TableBody, TableCell, TableRow, TableHead } from "@material-ui/core";
import { IBundle, ICodeSystem, ICoding } from "@ahryman40k/ts-fhir-types/lib/R4";
import { Config } from "./App";
import TextField from '@material-ui/core/TextField';
import Autocomplete from '@material-ui/lab/Autocomplete';
import http, { AxiosResponse } from "axios";
import TerminologyAutocomplete from "./TerminologyAutocomplete";
import { CodeSystem } from "./ProjectDetail";

interface Props {
  project: RedmatchProject;
  updateStatus: string;
  onSave: (newmappings: Mapping[]) => void;
}

export default function Mappings(props: Props) {
  const { project, updateStatus, onSave } = props;
  const { terminologyUrl } = useContext(Config);
  // The mappings
  const [mappings, setMappings] = useState<Mapping[]>(project.mappings);
  // The options available in the code systems autocomplete for each mapping
  const [options, setOptions] = useState<CodeSystem[]>([]);
  // The selected code systems for each mapping
  const [codeSystems, setCodeSystems] = useState<(CodeSystem | null)[]>(() : (CodeSystem | null)[] => {
    let data : (CodeSystem | null)[] = [];
    // Initialise selected code systems so autocompletes are controlled
    mappings.forEach((_m) => {
      data.push(null);
    });
    return data;
  });

  // Update code system selections
  React.useEffect(() => {
    // Load options
    http.get<IBundle>(`${terminologyUrl}/CodeSystem`)
      .then((response: AxiosResponse) => {
        if (response.data && response.data.entry) {
          const cs : CodeSystem[] = response.data.entry
            .filter((e: any) => {
              if (e.resource) {
                const ccs : ICodeSystem  = e.resource as ICodeSystem;
                return ccs.valueSet !== 'http://csiro.au/redmatch-fhir?vs';
              } else {
                return false;
              }
            })
            .map((e: any) => {
              if (e.resource) {
                const ccs : ICodeSystem  = e.resource as ICodeSystem;
                const c: CodeSystem = {
                  url: ccs.url ? ccs.url : '',
                  name: ccs.name ? ccs.name : '',
                  vs: ccs.valueSet ? ccs.valueSet : ''
                };
                return c;
              } else {
                const c: CodeSystem = {
                  url: '',
                  name: '',
                  vs: ''
                };
                return c;
              };
            }); 
          console.log('Code systems options: ' + JSON.stringify(cs));
          setOptions(cs);
          
          // Set current selection
          let data : (CodeSystem | null)[] = [];
          mappings.forEach((m) => {
            // Find corresponding option
            let o = cs.find((option) => {
              option.url === m.targetSystem
            });

            console.log('o is ' + o);
            if (o) {
              data.push(o);
            } else {
              console.log('Could not find option for code system ' + m.targetSystem);
              data.push(null);
            }
          });
          setCodeSystems(data);
        }
      });
  }, []);


  const getOptionSelected = (option: CodeSystem | null, value: CodeSystem | null) => {
    console.log("option: " + option + " value: " + value);
    if (option == null && value == null) {
      return true;
    } else if (option && value && option.url === value.url) {
      return true;
    } else {
      return false;
    }
  };


  function renderContent() {
    if (!mappings || mappings.length < 1) {
      return (
        <Typography variant="body1">No mappings are needed.</Typography>
      );
    } else {
      return (
        <Box>
          <Toolbar>
            <Button
              type="submit"
              onClick={() => onSave(mappings)}
              color="primary"
              endIcon={
                updateStatus === "loading" ? (
                  <CircularProgress size={20} color="inherit" />
                ) : null
              }
            >
              Save
            </Button>
          </Toolbar>
          <Table>
            <TableHead>
              <TableRow>
                <TableCell>REDCap Field Id</TableCell>
                <TableCell>Type</TableCell>
                <TableCell>Label</TableCell>
                <TableCell>Text</TableCell>
                <TableCell>System</TableCell>
                <TableCell>Code</TableCell>
                <TableCell>Display</TableCell>
              </TableRow>
            </TableHead>
            <TableBody>
              {mappings.map((mapping, i) => {
                let vs = '';
                let cs = codeSystems[i];
                if (cs !== null) {
                  vs = cs.vs;
                }
                
                return (
                  <TableRow key={i}>
                    <TableCell>{mapping.redcapFieldId}</TableCell>
                    <TableCell>{mapping.redcapFieldType}</TableCell>
                    <TableCell>{mapping.redcapLabel}</TableCell>
                    <TableCell>{mapping.text}</TableCell>
                    <TableCell>
                      <Autocomplete
                        //id="system-combo-box-demo"
                        options={options}
                        getOptionLabel={(option) => !Array.isArray(option) ? (option !== null ? option.name : '') : ''}
                        onChange={(_, value: CodeSystem | null) => {
                          console.log('Changing value: ' + JSON.stringify(value));
                          setCodeSystems(prevArray => {
                            // Create new array and copy - otherwise will not re-render.
                            const newArr = prevArray.map((cs, j) => {
                              if (i === j) {
                                return value;
                              } else {
                                return cs;
                              }
                            });
                            return newArr;
                          });
                          setMappings(prevArray => {
                            const newArr = prevArray.map((m, j) => {
                              if (i === j && value) {
                                if (m.targetSystem === value.url) {
                                  return m;
                                } else {
                                  // Clone mapping, replace targetSystem, and clear targetCode and targetDisplay
                                  var clone = { ...m };
                                  clone.targetSystem = value.url;
                                  clone.targetCode = '';
                                  clone.targetDisplay = '';
                                  return clone;
                                }
                              } else {
                                return m;
                              }
                            });
                            return newArr;
                          });
                        }} 
                        style={{ width: 300 }}
                        value={codeSystems[i]}
                        getOptionSelected={getOptionSelected}
                        renderInput={(params) => <TextField {...params} label="Code System" variant="outlined"/>}
                      />
                    </TableCell>
                    <TableCell>{mapping.targetCode}
                    </TableCell>
                    <TableCell>
                      <TerminologyAutocomplete
                        url={terminologyUrl}
                        valueSet={vs}
                        onChange={(newCoding: ICoding | null) => {
                          setMappings(prevArray => {
                            const newArr: Mapping[] = prevArray.map((m, j) => {
                              if (i === j) {
                                const cd = newCoding;
                                if (cd !== null) {
                                  if (m.targetCode === cd.code) {
                                    return m;
                                  } else {
                                    var clone = { ...m };
                                    clone.targetCode = cd.code != null ? cd.code : '';
                                    clone.targetDisplay = cd.display != null ? cd.display : '';
                                    return clone;
                                  }
                                }
                                return m;
                              } else {
                                return m;
                              }
                            });
                            return newArr;
                          });
                        }}
                      />
                    </TableCell>
                  </TableRow>
                );
              })}
            </TableBody>
          </Table>
        </Box>
      );
    }
  }

  return (
    <Box>{renderContent()}</Box>
  );
}
