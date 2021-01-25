/*
 * Copyright © 2018-2021, Commonwealth Scientific and Industrial Research
 * Organisation (CSIRO) ABN 41 687 119 230. Licensed under the CSIRO Open Source
 * Software Licence Agreement.
 */
package au.csiro.redmatch.exceptions;

/**
 * Generic exception for Redmatch.
 * 
 * @author Alejandro Metke Jimenez
 *
 */
public class RedmatchException extends AbstractRuntimeException {

  private static final long serialVersionUID = 1L;

  public RedmatchException(String message) {
    super(message);
  }

  public RedmatchException(String message, Throwable cause) {
    super(message, cause);
  }

  public RedmatchException(String message, Throwable cause, boolean enableSuppression,
      boolean writableStackTrace) {
    super(message, cause, enableSuppression, writableStackTrace);
  }

}
