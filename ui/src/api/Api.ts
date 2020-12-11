/*
 * Copyright © 2020, Commonwealth Scientific and Industrial Research
 * Organisation (CSIRO) ABN 41 687 119 230. All rights reserved.
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
    if (axiosInstance.current) {
      return axiosInstance.current
        .get<T>(`${url}`, {
          params,
          cancelToken: source.token
        })
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
        cancelToken: source.token
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
