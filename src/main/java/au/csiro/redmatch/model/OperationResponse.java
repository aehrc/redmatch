/**
 * Copyright CSIRO Australian e-Health Research Centre (http://aehrc.com). All rights reserved. Use is subject to 
 * license terms and conditions.
 */

package au.csiro.redmatch.model;

/**
 * Used to indicate if a project has been registered or was already registered.
 * 
 * @author Alejandro Metke Jimenez
 *
 */
public class OperationResponse {

  public enum RegistrationStatus {
    CREATED, UPDATED
  }

  private final String projectId;

  private final RegistrationStatus status;
 
  /**
   * Creates a new project registration response.
   * 
   * @param projectId The id of the project.
   * @param status The status of the project.
   */
  public OperationResponse(String projectId, RegistrationStatus status) {
    super();
    this.projectId = projectId;
    this.status = status;
  }

  public String getProjectId() {
    return projectId;
  }

  public RegistrationStatus getStatus() {
    return status;
  }

}
