/*
 * Copyright © 2018-2021, Commonwealth Scientific and Industrial Research
 * Organisation (CSIRO) ABN 41 687 119 230. Licensed under the CSIRO Open Source
 * Software Licence Agreement.
 */
package au.csiro.redmatch.model;

/**
 * Used to indicate if a project has been registered or was already registered.
 * 
 * @author Alejandro Metke Jimenez
 *
 */
public class OperationResponse {

  public enum Status {
    CREATED, UPDATED, NOT_MODIFIED
  }

  private final String projectId;

  private final Status status;
 
  /**
   * Creates a new project registration response.
   * 
   * @param projectId The id of the project.
   * @param status The status of the project.
   */
  public OperationResponse(String projectId, Status status) {
    super();
    this.projectId = projectId;
    this.status = status;
  }

  public String getProjectId() {
    return projectId;
  }

  public Status getStatus() {
    return status;
  }

}
