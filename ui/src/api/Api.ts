/*
 * Copyright © 2018-2021, Commonwealth Scientific and Industrial Research
 * Organisation (CSIRO) ABN 41 687 119 230. Licensed under the CSIRO Open Source
 * Software Licence Agreement.
 */
import http, { AxiosInstance, Canceler, CancelTokenSource } from "axios";
import { merge } from "lodash";


export interface CancelablePromise<T> extends Promise<T> {
  cancel: Canceler;
}

export function get<T>(
  axiosInstance: React.MutableRefObject<AxiosInstance | undefined>, 
  url: string, 
  params: any = {}): CancelablePromise<T> {
  return cancelableFetch(source => {
    let resolvedConfig = {
      cancelToken: source.token,
      headers: {
        Accept: "application/json"
      },
    };
    resolvedConfig = merge(resolvedConfig, params);

    if (axiosInstance.current) {
      return axiosInstance.current
        .get<T>(`${url}`, resolvedConfig)
        .then(response => response.data);
    } else {
      throw new Error('Undefined Axios current instance.');
    }
  });
}

export function post<T>(
  axiosInstance: React.MutableRefObject<AxiosInstance | undefined>,
  url: string,
  body: any,
  config?: any
): CancelablePromise<T> {
  return cancelableFetch(
    async source => {
      let resolvedConfig = {
        cancelToken: source.token,
        headers: {
          Accept: "application/json",
          "Content-Type": "application/json",
        },
      };
      if (config) resolvedConfig = merge(resolvedConfig, config);
      if (axiosInstance.current) {
        const response = await axiosInstance.current.post<T>(url, body, resolvedConfig);
        return response.data;
      } else {
        throw new Error('Undefined Axios current instance.');
      }
    }
  );
}

function cancelableFetch<T>(
  fetch: (source: CancelTokenSource) => Promise<T>
): CancelablePromise<T> {
  const source = http.CancelToken.source();
  const promise = fetch(source) as CancelablePromise<T>;
  promise.cancel = source.cancel;
  return promise;
}
