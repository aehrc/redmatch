/*
 * Copyright © 2018-2021, Commonwealth Scientific and Industrial Research
 * Organisation (CSIRO) ABN 41 687 119 230. Licensed under the CSIRO Open Source
 * Software Licence Agreement.
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
