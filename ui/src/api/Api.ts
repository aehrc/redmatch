/*
 * Copyright © 2020, Commonwealth Scientific and Industrial Research
 * Organisation (CSIRO) ABN 41 687 119 230. All rights reserved.
 */

import http, { AxiosRequestConfig, Canceler, CancelTokenSource } from "axios";
import { merge } from "lodash";

export interface CancelablePromise<T> extends Promise<T> {
  cancel: Canceler;
}

const standardGetConfig: AxiosRequestConfig = {
  headers: {
    Accept: "application/json"
  }
};

const standardPostConfig: AxiosRequestConfig = {
  headers: {
    ...standardGetConfig.headers,
    "Content-Type": "application/json"
  }
};

export function get<T>(url: string, params: any = {}): CancelablePromise<T> {
  return cancelableFetch(source => {
    return http
      .get<T>(url, {
        ...standardGetConfig,
        params,
        cancelToken: source.token
      })
      .then(response => response.data);
    }
  );
}

export function post<T>(
  url: string,
  body: any,
  config?: any
): CancelablePromise<T> {
  return cancelableFetch(
    async source => {
      let resolvedConfig = {
        ...standardPostConfig,
        cancelToken: source.token
      };
      if (config) resolvedConfig = merge(resolvedConfig, config);
      const response = await http
        .post<T>(url, body, resolvedConfig);
      return response.data;
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
