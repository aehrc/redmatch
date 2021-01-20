/*
 * Copyright © 2018-2021, Commonwealth Scientific and Industrial Research
 * Organisation (CSIRO) ABN 41 687 119 230. Licensed under the CSIRO Open Source
 * Software Licence Agreement.
 */
import axios from 'axios';
import React, { useState } from 'react';
import TextField from '@material-ui/core/TextField';
import Autocomplete from '@material-ui/lab/Autocomplete';
import { ICoding, IValueSet } from '@ahryman40k/ts-fhir-types/lib/R4';
import http, { CancelTokenSource } from "axios";
import * as _ from "lodash";

interface Props {
  url: string;
  valueSetUrl: string;
  coding: ICoding | null;
  onChange: (newCoding: string| ICoding | null) => void;
  onError: (error: Error | null | undefined) => void;
}

export default function CodeSearch(props: Props) {
  const { url, valueSetUrl, coding, onChange, onError } = props;
  const [, setExpansionQuery] = useState(_.debounce(() => { return { cancel: () => {} }; }, 300));
  const [options, setOptions] = useState<ICoding[]>([]);

  /*
   * Replace options every time the user changes the input string.
   */
  const onInputChange = (_event: object, value: string, _reason: string) => {
    if (value === '' || (coding && value === coding.display)) {
      return;
    }
    const search = _.debounce(sendQuery, 300);
    setExpansionQuery(prevSearch => {
      if (prevSearch && prevSearch.cancel) {
        prevSearch.cancel();
      }
      return search;
    });

    search(value);
  }

  /*
   * Sends a query and handles cancellations.
   */
  const sendQuery = async (value: string) => {
    const resp = await fetchData(value);

    if (resp.cancelPrevQuery) return;

    if (resp.result) {
      const vs = resp.result;
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
    } else {
      setOptions([]);
      onError(resp.error);
    }
  };

  interface ServerResponse {
    data: IValueSet
  }

  interface FetchDataResponse {
    cancelPrevQuery: boolean,
    result: IValueSet,
    error: any
  }
  
  /*
   * The actual expansion query.
   */
  let tokenSource: CancelTokenSource;
  const  fetchData = async (keyword: string) => {
    let res : FetchDataResponse = { cancelPrevQuery: false, result: {resourceType: 'ValueSet'}, error: {} };
    try {
      if (typeof tokenSource !== typeof undefined) {
        tokenSource.cancel('Operation canceled due to new request.');
      }
  
      // save the new request for cancellation
      tokenSource = axios.CancelToken.source();
  
      const { data } = await http.post<IValueSet>(
        url + '/ValueSet/$expand',
        `{
          "resourceType" : "Parameters",
          "parameter" : [
            {
              "name" : "url",
              "valueUri" : "${valueSetUrl}"
            },
            {
              "name" : "filter",
              "valueString" : "${keyword}"
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
          },
          cancelToken: tokenSource.token
        }
      );
      res.result = data;
      return res;
    } catch (err) {
      res.error = err;
      if (http.isCancel(err)) {
        res.cancelPrevQuery = true;
      }
      return  res;
    }
  };
  
  /*
   * Pass through - filtering and sorting are done server side.
   */
  const filterOptions = (options: ICoding[], _state: object) => {
    return options;
  }

  return (
    <div style={{ width: 300 }}>
      <Autocomplete
        freeSolo
        getOptionSelected={(option, value) => option.system === value.system && option.code === value.code}
        getOptionLabel={(option) => option.display !== undefined ? option.display : 'NA'}
        options={options}
        filterOptions={filterOptions}
        onInputChange={onInputChange}
        onChange={(_e, value: string | ICoding | null) => {
          onChange(value);
        }} 
        renderInput={(params) => (
          <TextField {...params} label="Search" margin="normal" variant="outlined" />
        )}
        value={coding}
      />
    </div>
  );
}