/**
 * Copyright CSIRO Australian e-Health Research Centre (http://aehrc.com). All rights reserved. Use is subject to 
 * license terms and conditions.
 */

package au.csiro.redmatch.exporter;

import au.csiro.redmatch.exceptions.AbstractRuntimeException;

/**
 * Thrown when there is a problem applying a mapping rule.
 * 
 * @author Alejandro Metke Jimenez
 *
 */
public class RuleApplicationException extends AbstractRuntimeException {

  private static final long serialVersionUID = 1L;

  public RuleApplicationException(String message, Throwable cause, boolean enableSuppression,
      boolean writableStackTrace) {
    super(message, cause, enableSuppression, writableStackTrace);
  }

  public RuleApplicationException(String message, Throwable cause) {
    super(message, cause);
  }

  public RuleApplicationException(String message) {
    super(message);
  }

}
