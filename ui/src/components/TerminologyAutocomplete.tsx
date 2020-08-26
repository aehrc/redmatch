import axios from 'axios';
import React from 'react';
import TextField from '@material-ui/core/TextField';
import Autocomplete, { createFilterOptions } from '@material-ui/lab/Autocomplete';
import CircularProgress from '@material-ui/core/CircularProgress';
import { ICoding, IValueSet } from '@ahryman40k/ts-fhir-types/lib/R4';
import http, { AxiosRequestConfig, Canceler, CancelTokenSource } from "axios";
import * as _ from "lodash";

interface Props {
  url: string;
  valueSet: string;
  onChange: (newCoding: ICoding | null) => void;
}

export default function TerminologyAutocomplete(props: Props) {
  const { url, valueSet, onChange } = props;
  const [loading, setLoading] = React.useState(false);
  const [options, setOptions] = React.useState<ICoding[]>([]);
  
  /*
   * Replace options every time the user changes the input string.
   */
  const onInputChange = (event: object, value: string, _reason: string) => {
    console.log('onInputChange, event = ' + event);
    if (value === '') {
      setOptions([]);
    } else {
      if (valueSet && valueSet !== '') {
        http.post(
          url + '/ValueSet/$expand',
          `{
            "resourceType" : "Parameters",
            "parameter" : [
              {
                "name" : "url",
                "valueUri" : "${valueSet}"
              },
              {
                "name" : "filter",
                "valueString" : "${value}"
              },
              {
                "name" : "count",
                "valueInteger" : 20
              }
            ]
          }`,
          {
            headers: {
              "Accept": "application/json",
              "Content-Type": "application/json"
            }
          }
        ).then((response) => {
          console.log(response);
          if (response.data) {
            let vs: IValueSet  = response.data as IValueSet;
            if (vs.expansion && vs.expansion.contains) {
              const codings: ICoding[] = vs.expansion.contains.map((e) => {
                const coding: ICoding = {
                  system: e.system,
                  code: e.code,
                  display: e.display
                };
                return coding;
              });
              setOptions(codings);
            }
          }
        });
      } else {
        console.log('No value set selected.');
      }
    }
  }
  
  /*
   * Pass through - filtering and sorting are done server side.
   */
  const filterOptions = (options: ICoding[], _state: object) => {
    return options;
  }

  return (
    <div style={{ width: 300 }}>
      <Autocomplete
        //#region id="free-solo-demo"
        freeSolo
        getOptionSelected={(option, value) => option.system == value.system && option.code === value.code}
        getOptionLabel={(option) => option.display !== undefined ? option.display : 'NA'}
        options={options}
        filterOptions={filterOptions}
        onInputChange={onInputChange}
        onChange={(_, value: ICoding | null) => {
          console.log('Changing value: ' + JSON.stringify(value));
          onChange(value);
        }} 
        renderInput={(params) => (
          <TextField {...params} label="Code" margin="normal" variant="outlined" />
        )}
      />
    </div>
  );
}