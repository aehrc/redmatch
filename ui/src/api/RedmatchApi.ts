/*
 * Copyright © 2020, Commonwealth Scientific and Industrial Research
 * Organisation (CSIRO) ABN 41 687 119 230. All rights reserved.
 */

import { MutationFunction, QueryFunction } from "react-query";
import { get, post } from "./Api";

export interface UnsavedRedmatchProject {
  name: string;
  reportId: string;
  redcapUrl: string;
  token: string;
}

export interface RedmatchProject extends UnsavedRedmatchProject {
  id: string;
  rulesDocument: string;
  issues: Issue[];
}

export interface Issue {
  id: string;
  rowStart: number;
  colStart: number;
  rowEnd: number;
  colEnd: number;
  text: string;
  annotationType: string;
}

export interface RedmatchApi {
  getProjects: QueryFunction<RedmatchProject[], [string]>;
  getProject: QueryFunction<RedmatchProject, [string, string]>;
  createProject: MutationFunction<RedmatchProject, UnsavedRedmatchProject>;
  // updateProject: MutationFunction<RedmatchProject, RedmatchProject>;
  updateRules: MutationFunction<RedmatchProject, [string, string]>;
}

export default (redmatchUrl: string): RedmatchApi => {
  return {
    getProjects: async function() {
      return get<RedmatchProject[]>(`${redmatchUrl}/project`);
    },
    getProject: async function(_, id: string) {
      return get<RedmatchProject>(`${redmatchUrl}/project/${id}`);
    },
    createProject: async function(unsavedRedmatchProject) {
      return post<RedmatchProject>(
        `${redmatchUrl}/project`,
        unsavedRedmatchProject
      );
    },
    updateRules: async function(params: string[]) {
      return post<RedmatchProject>(
        `${redmatchUrl}/project/${params[0]}/$update-rules`,
        `${params[1]}`
      );
    }
  };
};
