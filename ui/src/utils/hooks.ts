/*
 * Copyright © 2018-2021, Commonwealth Scientific and Industrial Research
 * Organisation (CSIRO) ABN 41 687 119 230. Licensed under the CSIRO Open Source
 * Software Licence Agreement.
 */
import { useEffect, useRef } from 'react';
import axios from 'axios';
import type { AxiosInstance } from 'axios';

import { useKeycloak } from '@react-keycloak/web';

export const useAxios = (baseURL: string) => {
  const axiosInstance = useRef<AxiosInstance>();
  const { keycloak, initialized } = useKeycloak();
  const kcToken = keycloak?.token ?? '';

  useEffect(() => {
    axiosInstance.current = axios.create({
      baseURL,
      headers: {
        Authorization: initialized ? `Bearer ${kcToken}` : undefined,
      },
    });

    axiosInstance.current.interceptors.response.use(
      response => response,
      error => {
        console.log('error.response.status = ' + error.response.status);
        if (error.response.status === 401) {
          keycloak.login();
        }
        throw error;
      });

    return () => {
      axiosInstance.current = undefined;
    };
  }, [baseURL, initialized, kcToken, keycloak]);

  return axiosInstance;
};
