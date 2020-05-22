/**
 * Copyright CSIRO Australian e-Health Research Centre (http://aehrc.com). All rights reserved. Use is subject to
 * license terms and conditions.
 */

package au.csiro.redmatch.exceptions;

import java.util.UUID;

/**
 * Parent of all our exceptions.
 *
 * @author Alejandro Metke Jimenez
 *
 */
public abstract class AbstractRuntimeException extends RuntimeException {

  private static final long serialVersionUID = 1L;

  public AbstractRuntimeException(String message, Throwable cause, boolean enableSuppression,
      boolean writableStackTrace) {
    super("[" + UUID.randomUUID().toString() + "]: " + message, cause, enableSuppression,
        writableStackTrace);
  }

  public AbstractRuntimeException(String message, Throwable cause) {
    super("[" + UUID.randomUUID().toString() + "]: " + message, cause);
  }

  public AbstractRuntimeException(String message) {
    super("[" + UUID.randomUUID().toString() + "]: " + message);
  }

}
