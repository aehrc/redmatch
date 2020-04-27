/**
 * Copyright CSIRO Australian e-Health Research Centre (http://aehrc.com). All rights reserved. Use is subject to 
 * license terms and conditions.
 */

package au.csiro.redmatch.web;

import au.csiro.redmatch.exceptions.AbstractRuntimeException;

/**
 * Thrown when a Redmatch project is invalid.
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
