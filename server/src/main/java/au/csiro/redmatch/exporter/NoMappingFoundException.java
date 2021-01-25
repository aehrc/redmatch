/*
 * Copyright © 2018-2021, Commonwealth Scientific and Industrial Research
 * Organisation (CSIRO) ABN 41 687 119 230. Licensed under the CSIRO Open Source
 * Software Licence Agreement.
 */
package au.csiro.redmatch.exporter;


/**
 * Thrown when a mapping to a standard code is not found.
 * 
 * @author Alejandro Metke
 *
 */
public class NoMappingFoundException extends RuntimeException {

  private static final long serialVersionUID = 1L;

  public NoMappingFoundException() {
    
  }

  public NoMappingFoundException(String message) {
    super(message);
  }

  public NoMappingFoundException(Throwable cause) {
    super(cause);
  }

  public NoMappingFoundException(String message, Throwable cause) {
    super(message, cause);
  }

  /**
   * @param message
   * @param cause
   * @param enableSuppression
   * @param writableStackTrace
   */
  public NoMappingFoundException(String message, Throwable cause, boolean enableSuppression,
      boolean writableStackTrace) {
    super(message, cause, enableSuppression, writableStackTrace);
  }

}
