import { Box, Toolbar, IconButton, Typography, Button, CircularProgress } from "@material-ui/core";
import DehazeIcon from '@material-ui/icons/Dehaze';
import React, { useContext, useState, useEffect } from "react";
import { RedmatchProject, Mapping } from "../api/RedmatchApi";
import { Table, TableBody, TableCell, TableRow, TableHead } from "@material-ui/core";
import { IBundle, ICodeSystem, ICoding, IValueSet } from "@ahryman40k/ts-fhir-types/lib/R4";
import { Config } from "./App";
import TextField from '@material-ui/core/TextField';
import Autocomplete from '@material-ui/lab/Autocomplete';
import http, { AxiosResponse } from "axios";
import CodeSearch from "./CodeSearch";
import ValueSetConfig from "./ValueSetConfig";

interface Props {
  project: RedmatchProject;
  status: string;
  updateStatus: string;
  onSave: (newmappings: Mapping[]) => void;
}

export const getOptionSelected = (option: IValueSet | null, value: IValueSet | null) => {
  if (option == null && value == null) {
    return true;
  } else if (option && value && option.url === value.url) {
    return true;
  } else {
    return false;
  }
};

export default function Mappings(props: Props) {
  const { project, status, updateStatus, onSave } = props;
  const { terminologyUrl } = useContext(Config);
  const [valueSetConfigOpen, setValueSetConfigOpen] = useState(false)
  const [valueSetStatus, setValueSetStatus] = useState<string>('loading');
  // The mappings
  const [mappings, setMappings] = useState<Mapping[]>(project.mappings);
  // The options available in the value sets autocomplete for each mapping
  const [options, setOptions] = useState<IValueSet[]>([]);
  // The selected value sets for each mapping
  const [valueSets, setValueSets] = useState<(IValueSet | null)[]>(() : (IValueSet | null)[] => {
    let data : (IValueSet | null)[] = [];
    // Initialise selected value sets so autocompletes are controlled
    mappings.forEach((m) => {
      if(m.valueSetUrl) {
        let v : IValueSet = {
          resourceType: 'ValueSet',
          url: m.valueSetUrl,
          name: m.valueSetName
        };
        data.push(v);
      } else {
        data.push(null);
      }
    });
    return data;
  });
  // The selected codings
  const [codings, setCodings] = useState<(ICoding | null)[]>(() : (ICoding | null)[] => {
    let data : (ICoding | null)[] = [];
    mappings.forEach((m) => {
      if (m.targetCode) {
        let c : ICoding = {
          system: m.targetSystem,
          code: m.targetCode,
          display: m.targetDisplay
        };
        data.push(c);
      } else {
        data.push(null);
      }
    });
    return data;
  });

  useEffect(() => {
    const implicit = http.get<IBundle>(`${terminologyUrl}/CodeSystem`);
    const explicit = http.get<IBundle>(`${terminologyUrl}/ValueSet?_elements=url,name`);

    http.all([implicit, explicit]).then(http.spread(function (imp, exp) {
      let o = [];

      // Process implicit value sets
      if (imp.data && imp.data.entry) {
        const cs : IValueSet[] = imp.data.entry
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
            const v: IValueSet = {
              resourceType: 'ValueSet',
              url: ccs.valueSet ? ccs.valueSet : '',
              name: ccs.name ? ccs.name + ' Implicit Value Set' : ''
            };
            return v;
          } else {
            const v: IValueSet = {
              resourceType: 'ValueSet',
              url: '',
              name: ''
            };
            return v;
          };}); 
        o.push(...cs);
      }

      // Process explicit value sets
      if (exp.data && exp.data.entry) {
        const cs : IValueSet[] = exp.data.entry
          .map((e: any) => {
            if (e.resource) {
              const ccs : IValueSet = e.resource as IValueSet;
              const v: IValueSet = {
                resourceType: 'ValueSet',
                url: ccs.url,
                name: ccs.name
              };
              return v;
            } else {
              const v: IValueSet = {
                resourceType: 'ValueSet',
                url: '',
                name: ''
              };
              return v;
            };
          }); 
        o.push(...cs);
      }

      setOptions(o);
      setValueSetStatus('loaded');
    }));
  }, []);

  const handleValueSetConfigOpen = () => setValueSetConfigOpen(true);

  const handleValueSetConfigCancel = () => setValueSetConfigOpen(false);

  const handleValueSetConfigSuccess = (valueSet : IValueSet) => {
    setValueSetConfigOpen(false);
    // Only run if the user selected a new default value set
    if (valueSet.url) {
      const newValueSets = valueSets.map((vs, i) => {
        let vsUrl = mappings[i].valueSetUrl;
        if(!vsUrl || vsUrl.length === 0) {
          return valueSet;
        } else {
          return vs;
        }
      });
      setValueSets(newValueSets);

      const newMappings = mappings.map((mapping, i) => {
        let vsUrl = mappings[i].valueSetUrl;
        if(!vsUrl || vsUrl.length === 0) {
          var clone = { ...mapping };
          clone.valueSetUrl = valueSet.url ? valueSet.url : '';
          clone.valueSetName = valueSet.name ? valueSet.name : '' ;
          return clone;
        } else {
          return mapping;
        }
      });
      setMappings(newMappings);
    }
  };

  function renderContent() {
    if (status === 'loading' || valueSetStatus == 'loading') {
      return <Typography variant="body1">Loading mappings...</Typography>;
    } else if (!mappings || mappings.filter(x => x.active).length === 0) {
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
            <IconButton 
              aria-label="delete"
              onClick={() => handleValueSetConfigOpen()}>
              <DehazeIcon />
            </IconButton>
          </Toolbar>
          <Table>
            <TableHead>
              <TableRow>
                <TableCell>REDCap Field Id</TableCell>
                <TableCell>Type</TableCell>
                <TableCell>Label</TableCell>
                <TableCell>Text</TableCell>
                <TableCell>Value Set</TableCell>
                <TableCell>Search</TableCell>
                <TableCell>System</TableCell>
                <TableCell>Code</TableCell>
              </TableRow>
            </TableHead>
            <TableBody>
              {mappings.filter(x => x.active).map((mapping, i) => {                
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
                        getOptionLabel={(option) => option ? (option.name ? option.name : '') : ''}
                        onChange={(_, value: IValueSet | null) => {
                          setValueSets(prevArray => {
                            // Create new array and copy - otherwise will not re-render
                            const newArr = prevArray.map((vs, j) => {
                              if (i === j) {
                                return value;
                              } else {
                                return vs;
                              }
                            });
                            return newArr;
                          });

                          // We need to update the value set selection in the mapping
                          setMappings(prevArray => {
                            // Create new array and copy - otherwise will not re-render
                            const newArr = prevArray.map((m, j) => {
                              if (i === j && value) {
                                if (m.valueSetUrl === value.url) {
                                  return m;
                                } else {
                                  // Clone mapping, replace valueSet, and clear targetSystem, targetCode and targetDisplay
                                  var clone = { ...m };
                                  clone.valueSetUrl = value.url ? value.url : '';
                                  clone.valueSetName = value.name ? value.name : '';
                                  clone.targetSystem = '';
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
                        value={valueSets[i]}
                        getOptionSelected={getOptionSelected}
                        renderInput={(params) => <TextField {...params} label="Value Set" variant="outlined"/>}
                      />
                    </TableCell>
                    <TableCell>
                      <CodeSearch
                        url={terminologyUrl}
                        valueSetUrl={mapping.valueSetUrl}
                        coding={codings[i]}
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
                                    clone.targetSystem = cd.system != null ? cd.system : '';
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
                    <TableCell>{mapping.targetSystem}</TableCell>
                    <TableCell>{mapping.targetCode}</TableCell>
                  </TableRow>
                );
              })}
            </TableBody>
          </Table>
          <ValueSetConfig
            open={valueSetConfigOpen}
            options={options}
            onSuccess={handleValueSetConfigSuccess}
            onCancel={handleValueSetConfigCancel}
          />
        </Box>
      );
    }
  }

  return (
    <Box>{renderContent()}</Box>
  );
}
