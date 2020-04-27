/**
 * Copyright CSIRO Australian e-Health Research Centre (http://aehrc.com). All rights reserved. Use is subject to 
 * license terms and conditions.
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
