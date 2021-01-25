/*
 * Copyright © 2018-2021, Commonwealth Scientific and Industrial Research
 * Organisation (CSIRO) ABN 41 687 119 230. Licensed under the CSIRO Open Source
 * Software Licence Agreement.
 */
import env from "@beam-australia/react-env";
import { MutationFunction, QueryFunction, QueryFunctionContext } from "react-query";
import { useAxios } from "../utils/hooks";
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
  metadata: Metadata;
  mappings: Mapping[];
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

export interface Metadata {
  fields: Field[];
}

export interface Field {
  id: string;
  fieldType: string;
  textValidationType: string;
  fieldId: string;
  fieldLabel: string;
}

export interface Mapping {
  id: string;
  redcapFieldId: string;
  redcapLabel: string;
  redcapFieldType: string;
  text: string;
  targetSystem: string;
  targetCode: string;
  targetDisplay: string;
  valueSetUrl: string;
  valueSetName: string;
  inactive: boolean;
}

export interface RedmatchApi {
  getProjects: QueryFunction<RedmatchProject[]>;
  getProject: QueryFunction<RedmatchProject>;
  createProject: MutationFunction<RedmatchProject, UnsavedRedmatchProject>;
  updateRules: MutationFunction<RedmatchProject, [string, string]>;
  updateMappings: MutationFunction<RedmatchProject, [string, Mapping[]]>;
  updateMetadata: MutationFunction<RedmatchProject, [string]>;
}

export default (): RedmatchApi => {
  const redmatchUrl = env('REDMATCH_URL');
  const axiosInstance = useAxios(redmatchUrl);
  return {
    getProjects: async function() {
      return get<RedmatchProject[]>(axiosInstance, `/project`, null);
    },
    getProject: function(context: QueryFunctionContext<string>) {
      return get<RedmatchProject>(axiosInstance, `/project/${context.queryKey[1]}`, null);
    },
    createProject: function(unsavedRedmatchProject: UnsavedRedmatchProject) {
      return post<RedmatchProject>(
        axiosInstance,
        `/project`,
        unsavedRedmatchProject,  
        null
      );
    },
    updateRules: async function(params: string[]) {
      return post<RedmatchProject>(
        axiosInstance,
        `/project/${params[0]}/$update-rules`,
        `${params[1]}`,  
        null
      );
    },
    updateMappings: async function(params: any[]) {
      return post<RedmatchProject>(
        axiosInstance,
        `/project/${params[0]}/$update-mappings`,
        params[1],
        null
      );
    },
    updateMetadata: async function(params: any[]) {
      return post<RedmatchProject>(
        axiosInstance,
        `/project/${params[0]}/$update`,
        null,
        null);
    }
  };
};
