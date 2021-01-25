/*
 * Copyright © 2018-2021, Commonwealth Scientific and Industrial Research
 * Organisation (CSIRO) ABN 41 687 119 230. Licensed under the CSIRO Open Source
 * Software Licence Agreement.
 */
package au.csiro.redmatch.api;

import au.csiro.redmatch.exceptions.AbstractRuntimeException;

/**
 * Thrown when a project is invalid, for example, if it is missing required fields.
 * 
 * @author Alejandro Metke
 *
 */
public class InvalidProjectException extends AbstractRuntimeException {

  private static final long serialVersionUID = 1L;

  public InvalidProjectException(String message, Throwable cause, boolean enableSuppression,
      boolean writableStackTrace) {
    super(message, cause, enableSuppression, writableStackTrace);
  }

  public InvalidProjectException(String message, Throwable cause) {
    super(message, cause);
  }

  public InvalidProjectException(String message) {
    super(message);
  }

}
