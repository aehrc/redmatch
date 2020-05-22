/**
 * Copyright CSIRO Australian e-Health Research Centre (http://aehrc.com). All rights reserved. Use is subject to 
 * license terms and conditions.
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
