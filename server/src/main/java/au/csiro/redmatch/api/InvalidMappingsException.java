/**
 * Copyright CSIRO Australian e-Health Research Centre (http://aehrc.com). All rights reserved. Use 
 * is subject to license terms and conditions.
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
