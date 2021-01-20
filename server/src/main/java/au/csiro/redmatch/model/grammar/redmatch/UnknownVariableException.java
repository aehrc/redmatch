/*
 * Copyright © 2018-2021, Commonwealth Scientific and Industrial Research
 * Organisation (CSIRO) ABN 41 687 119 230. Licensed under the CSIRO Open Source
 * Software Licence Agreement.
 */
package au.csiro.redmatch.model.grammar.redmatch;


/**
 * 
 * Thrown when attempting to replace a variable with a value but no such variable
 * is in scope.
 * 
 * @author Alejandro Metke
 *
 */
public class UnknownVariableException extends RuntimeException {

  private static final long serialVersionUID = 1L;

  public UnknownVariableException() {
    super();
  }

  public UnknownVariableException(String message, Throwable cause, boolean enableSuppression,
      boolean writableStackTrace) {
    super(message, cause, enableSuppression, writableStackTrace);
  }

  public UnknownVariableException(String message, Throwable cause) {
    super(message, cause);
  }

  public UnknownVariableException(String message) {
    super(message);
  }

  public UnknownVariableException(Throwable cause) {
    super(cause);
  }
  
}
