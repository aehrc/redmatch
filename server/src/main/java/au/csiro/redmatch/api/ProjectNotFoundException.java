/*
 * Copyright © 2018-2021, Commonwealth Scientific and Industrial Research
 * Organisation (CSIRO) ABN 41 687 119 230. Licensed under the CSIRO Open Source
 * Software Licence Agreement.
 */
package au.csiro.redmatch.api;

import au.csiro.redmatch.exceptions.AbstractRuntimeException;

/**
 * Thrown when a project cannot be found.
 * 
 * @author Alejandro Metke Jimenez
 *
 */
public class ProjectNotFoundException extends AbstractRuntimeException {

  private static final long serialVersionUID = 1L;

  public ProjectNotFoundException(String message, Throwable cause, boolean enableSuppression,
      boolean writableStackTrace) {
    super(message, cause, enableSuppression, writableStackTrace);
  }

  public ProjectNotFoundException(String message, Throwable cause) {
    super(message, cause);
  }

  public ProjectNotFoundException(String message) {
    super(message);
  }

}
