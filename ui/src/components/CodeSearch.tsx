import axios from 'axios';
import React, { useCallback, useState, useEffect } from 'react';
import TextField from '@material-ui/core/TextField';
import Autocomplete, { createFilterOptions } from '@material-ui/lab/Autocomplete';
import CircularProgress from '@material-ui/core/CircularProgress';
import { ICoding, IValueSet } from '@ahryman40k/ts-fhir-types/lib/R4';
import http, { AxiosRequestConfig, Canceler, CancelTokenSource } from "axios";
import * as _ from "lodash";

interface Props {
  url: string;
  valueSetUrl: string;
  coding: ICoding | null;
  onChange: (newCoding: ICoding | null) => void;
}

export default function CodeSearch(props: Props) {
  const { url, valueSetUrl, coding, onChange } = props;
  const [filter, setFilter] = useState('');
  const [expansionQuery, setExpansionQuery] = useState(_.debounce(() => { return { cancel: () => {} }; }, 300));
  const [options, setOptions] = useState<ICoding[]>([]);
  const [errorMssg, setErrorMssg] = useState('');

  const [loading, setLoading] = useState(false);

  useEffect(() => {
    let isMounted = true; // note this flag denote mount status

    return () => { isMounted = false }; // use effect cleanup to set flag false, if unmounted
  });

  /*
   * Replace options every time the user changes the input string.
   */
  const onInputChange = (_event: object, value: string, _reason: string) => {
    if (value === '' || (coding && value === coding.display)) {
      return;
    }
    setFilter(value);
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
      setErrorMssg('');
    } else {
      setOptions([]);
      setErrorMssg(resp.error);
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
        //#region id="free-solo-demo"
        freeSolo
        getOptionSelected={(option, value) => option.system == value.system && option.code === value.code}
        getOptionLabel={(option) => option.display !== undefined ? option.display : 'NA'}
        options={options}
        filterOptions={filterOptions}
        onInputChange={onInputChange}
        onChange={(_, value: ICoding | null) => {
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