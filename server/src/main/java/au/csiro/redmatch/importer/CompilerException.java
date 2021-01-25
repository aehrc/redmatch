/*
 * Copyright © 2018-2021, Commonwealth Scientific and Industrial Research
 * Organisation (CSIRO) ABN 41 687 119 230. Licensed under the CSIRO Open Source
 * Software Licence Agreement.
 */
package au.csiro.redmatch.importer;

import au.csiro.redmatch.exceptions.AbstractRuntimeException;

/**
 * Thrown when there is a compilation issue.
 * 
 * @author Alejandro Metke Jimenez
 *
 */
public class CompilerException extends AbstractRuntimeException {

  private static final long serialVersionUID = 1L;

  public CompilerException(String message, Throwable cause, boolean enableSuppression,
      boolean writableStackTrace) {
    super(message, cause, enableSuppression, writableStackTrace);
  }

  public CompilerException(String message, Throwable cause) {
    super(message, cause);
  }

  public CompilerException(String message) {
    super(message);
  }

}
