/*
 * Copyright © 2018-2021, Commonwealth Scientific and Industrial Research
 * Organisation (CSIRO) ABN 41 687 119 230. Licensed under the CSIRO Open Source
 * Software Licence Agreement.
 */
package au.csiro.redmatch.api;


/**
 * Thrown when a client tries to create a project that already exists.
 * 
 * @author Alejandro Metke
 *
 */
public class ProjectAlreadyExistsException extends RuntimeException {

  private static final long serialVersionUID = 1L;

  public ProjectAlreadyExistsException(String message) {
    super(message);
  }

  public ProjectAlreadyExistsException(Throwable cause) {
    super(cause);
  }

  public ProjectAlreadyExistsException(String message, Throwable cause) {
    super(message, cause);
  }

  public ProjectAlreadyExistsException(String message, Throwable cause, boolean enableSuppression,
      boolean writableStackTrace) {
    super(message, cause, enableSuppression, writableStackTrace);
  }

}
