/*
 * Copyright © 2018-2021, Commonwealth Scientific and Industrial Research
 * Organisation (CSIRO) ABN 41 687 119 230. Licensed under the CSIRO Open Source
 * Software Licence Agreement.
 */
package au.csiro.redmatch.api;


/**
 * Thrown when the mappings being uploaded don't match the current mappings.
 * 
 * @author Alejandro Metke
 *
 */
public class InvalidMappingsException extends RuntimeException {

  private static final long serialVersionUID = 1L;

  public InvalidMappingsException() {

  }

  public InvalidMappingsException(String message) {
    super(message);
  }

  public InvalidMappingsException(Throwable cause) {
    super(cause);
  }

  public InvalidMappingsException(String message, Throwable cause) {
    super(message, cause);
  }

  public InvalidMappingsException(String message, Throwable cause, boolean enableSuppression,
      boolean writableStackTrace) {
    super(message, cause, enableSuppression, writableStackTrace);
  }

}
