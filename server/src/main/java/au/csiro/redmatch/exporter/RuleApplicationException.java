/*
 * Copyright © 2018-2021, Commonwealth Scientific and Industrial Research
 * Organisation (CSIRO) ABN 41 687 119 230. Licensed under the CSIRO Open Source
 * Software Licence Agreement.
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
