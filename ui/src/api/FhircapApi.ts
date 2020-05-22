/*
 * Copyright © 2020, Commonwealth Scientific and Industrial Research
 * Organisation (CSIRO) ABN 41 687 119 230. All rights reserved.
 */

import { MutationFunction, QueryFunction } from "react-query";
import { get, post } from "./Api";

export interface UnsavedFhircapProject {
  name: string;
  reportId: string;
  redcapUrl: string;
  token: string;
}

export interface FhircapProject extends UnsavedFhircapProject {
  id: string;
  rulesDocument: string;
}

export interface FhircapApi {
  getProjects: QueryFunction<FhircapProject[], [string]>;
  getProject: QueryFunction<FhircapProject, [string, string]>;
  createProject: MutationFunction<FhircapProject, UnsavedFhircapProject>;
}

export default (fhircapUrl: string): FhircapApi => {
  return {
    getProjects: async function() {
      return get<FhircapProject[]>(`${fhircapUrl}/project`);
    },
    getProject: async function(_, id: string) {
      return get<FhircapProject>(`${fhircapUrl}/project/${id}`);
    },
    createProject: async function(unsavedFhircapProject) {
      return post<FhircapProject>(
        `${fhircapUrl}/project`,
        unsavedFhircapProject
      );
    }
  };
};
