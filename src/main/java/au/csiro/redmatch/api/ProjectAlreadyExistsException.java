/**
 * Copyright CSIRO Australian e-Health Research Centre (http://aehrc.com). All rights reserved. Use 
 * is subject to license terms and conditions.
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
